# Epic 2: The Workload Orchestrator Engine
**Goal:** To build the "brain" of the warehouse. This involves creating the central Workload Orchestrator service and the flexible strategy pattern that allows it to decide how and when to release work to the floor.

| Story ID | User Story | Acceptance Criteria |
|----------|------------|----------------------|
| **WO-03** | As the System, I want to implement a Workload Orchestrator domain service so that I have a central point for making intelligent decisions about releasing work. | - A `WorkloadOrchestrator` class is created.<br>- It is capable of receiving `FulfillmentOrderValidated` events.<br>- It contains logic to delegate the handling of these orders to a configured strategy. |
| **WO-04** | As a Developer, I want to define a `IWorkloadReleaseStrategy` interface so that we can create multiple, interchangeable strategies for releasing work (e.g., Wave, Continuous). | - An interface (`IWorkloadReleaseStrategy`) is defined with a method like `planWork(orders)`.<br>- The `WorkloadOrchestrator` is coded to depend on this interface, not a concrete implementation. |
| **WO-05** | As a Developer, I want to implement a basic "Continuous Strategy" so that the orchestrator has a default, functional behavior for immediate, end-to-end testing. | - A `ContinuousStrategy` class is created that implements `IWorkloadReleaseStrategy`.<br>- When an order is received, this strategy immediately creates and releases a PickList for that single order.<br>- The orchestrator can be configured to use this strategy. |

---

# Epic 3: Implement the "Wave Strategy"
**Goal:** To implement the classic wave planning capability, allowing the business to batch orders together for scheduled, high-efficiency processing runs.

| Story ID | User Story | Acceptance Criteria |
|----------|------------|----------------------|
| **WO-06** | As a Developer, I want to implement the Wave aggregate so that the system can model a scheduled batch of work with its own lifecycle. | - A `Wave` aggregate is created with states: *Planned, Released, Closed*.<br>- The aggregate contains a collection of the orders or PickLists assigned to it.<br>- The aggregate enforces the rule that a Released wave cannot be modified. |
| **WO-07** | As a Warehouse Manager, I want the system to use a "Wave Strategy" to group orders based on carrier cut-off times so that we can ensure all of today's shipments are picked and packed before the truck leaves. | - A `WaveStrategy` class is created that implements `IWorkloadReleaseStrategy`.<br>- The strategy accumulates incoming orders until a trigger (e.g., a scheduled time) occurs.<br>- When triggered, it creates a Wave aggregate in a *Planned* state, containing all the relevant orders. |
| **WO-08** | As a Warehouse Manager, I want to be able to "Release a Wave" so that I can officially start the physical picking work for that batch on the warehouse floor. | - An API endpoint or internal command (`ReleaseWave`) is created.<br>- When triggered, it finds the Wave aggregate, changes its status to *Released*, and generates all the necessary PickLists.<br>- A `WaveReleased` domain event is published internally. |

---

# Epic 4: The Physical Workflow: Picking to Packing
**Goal:** To build the features that support the physical movement and verification of goods, from generating optimized routes for pickers to the final creation of a shippable package.

| Story ID | User Story | Acceptance Criteria |
|----------|------------|----------------------|
| **WO-09** | As the System, when work is released, I want to calculate an optimized pick route so that the picker's travel time through the warehouse is minimized. | - When a PickList is generated, a routing algorithm is applied to sequence the pick instructions.<br>- The sequence is based on the physical bin locations to create the shortest possible path. |
| **WO-10** | As a Picker, I want to view my assigned PickList on my handheld device and be guided to each location so that I can efficiently retrieve items. | - A simple UI is developed for a mobile device/scanner.<br>- The UI displays the current pick instruction (location, item, quantity).<br>- The picker can confirm a pick and be automatically shown the next instruction in the optimized sequence. |
| **WO-11** | As a Picker, I want to scan each item's barcode before placing it in my tote so that the system can verify I have picked the correct product and prevent errors. | - The handheld UI can activate the barcode scanner.<br>- The system validates the scanned SKU against the current pick instruction.<br>- An `ItemPicked` event is recorded for real-time inventory accuracy. |
| **WO-12** | As a Packer, after receiving all items for an order, I want to scan them into a shipping box so that the system can confirm the order is complete and ready for shipment. | - A "Packing Station" UI is created.<br>- The packer scans each item belonging to a consolidated order.<br>- Upon completion, the system creates a `Package` aggregate and triggers the final event. |

---

# Epic 5: Service Integration and Communication
**Goal:** To ensure the Warehouse Operations service communicates reliably with the outside world, consuming upstream events that trigger its work and publishing downstream events that signal its completion.

| Story ID | User Story | Acceptance Criteria |
|----------|------------|----------------------|
| **WO-13** | As the System, I want to consume `FulfillmentOrderValidated` events from Kafka so that I know when a new order is ready for warehouse processing. | - A Kafka consumer is implemented in the service.<br>- The consumer is subscribed to the `fulfillment.order_management.v1.events` topic.<br>- On receiving a `FulfillmentOrderValidated` event, it passes the order details to the Workload Orchestrator. |
| **WO-14** | As the System, when a packer confirms a package is complete, I want to publish a `PackagePacked` event so that the Shipment & Transportation context knows it can begin its work. | - When a `Package` aggregate is finalized, a `PackagePacked` event is generated.<br>- The event contains the `order_id`, `package_id`, final weight, and dimensions.<br>- The event is published to a dedicated Kafka topic (e.g., `fulfillment.warehouse.v1.events`). |
| **WO-15** | As a Developer, I want to implement the Transactional Outbox pattern for publishing events so that we can guarantee that events are sent if and only if the business transaction was successfully committed to the database. | - An outbox table is added to the service's database.<br>- Saving an aggregate (e.g., Package) and writing its corresponding event to the outbox table occur in the same database transaction.<br>- A separate worker process reliably reads from the outbox and publishes events to Kafka, ensuring no events are lost. |
