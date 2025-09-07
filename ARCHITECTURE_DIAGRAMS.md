# Warehouse Operations Service - Architecture Diagrams

## System Context Diagram

```mermaid
graph TB
    OM[Order Management Service] -->|FulfillmentOrderValidated| WO[Warehouse Operations Service]
    WO -->|PackagePacked| ST[Shipment & Transportation Service]
    
    WO --> MongoDB[(MongoDB)]
    WO --> Kafka[Kafka Cluster]
    
    subgraph "Warehouse Operations Service"
        WO --> WOrch[Workload Orchestrator]
        WO --> WaveM[Wave Management]
        WO --> PickM[Pick Management] 
        WO --> PackM[Pack Management]
    end
    
    subgraph "External Interfaces"
        HD[Handheld Devices] --> WO
        PS[Packing Stations] --> WO
    end
```

## Hexagonal Architecture View

```mermaid
graph TB
    subgraph "Infrastructure Layer"
        Web[REST Controllers]
        Kafka[Kafka Adapters]
        Mongo[MongoDB Repositories]
        Config[Configuration]
    end
    
    subgraph "Application Layer"
        AppSvc[Application Services]
        CmdH[Command Handlers]
        QueryH[Query Handlers]
        EventH[Event Handlers]
    end
    
    subgraph "Domain Layer"
        subgraph "Workload Context"
            WO[WorkloadOrchestrator]
            Strat[Release Strategies]
        end
        subgraph "Wave Context"
            Wave[Wave Aggregate]
            WaveRepo[Wave Repository Interface]
        end
        subgraph "Pick Context"
            PickList[PickList Aggregate]
            PickRepo[PickList Repository Interface]
        end
        subgraph "Pack Context"
            Package[Package Aggregate]
            PackRepo[Package Repository Interface]
        end
        subgraph "Shared Kernel"
            VO[Value Objects]
            Events[Domain Events]
            AR[Aggregate Root]
        end
    end
    
    Web --> AppSvc
    Kafka --> EventH
    AppSvc --> WO
    AppSvc --> Wave
    AppSvc --> PickList
    AppSvc --> Package
    
    WaveRepo -.->|implements| Mongo
    PickRepo -.->|implements| Mongo
    PackRepo -.->|implements| Mongo
```

## Domain Model - Detailed View

```mermaid
classDiagram
    class WorkloadOrchestrator {
        -IWorkloadReleaseStrategy strategy
        +handleOrderValidated(event)
        +planWork(orders)
    }
    
    class IWorkloadReleaseStrategy {
        <<interface>>
        +planWork(orders) WorkloadPlan
    }
    
    class ContinuousStrategy {
        +planWork(orders) WorkloadPlan
    }
    
    class WaveStrategy {
        -carrierCutoffTime
        -pendingOrders
        +planWork(orders) WorkloadPlan
        +accumulate(orders)
        +shouldTriggerWave()
    }
    
    class Wave {
        -WaveId id
        -WaveStatus status
        -List~OrderId~ orderIds
        -Instant createdAt
        -Instant releasedAt
        +release()
        +close()
        +addOrder(orderId)
        +canRelease() boolean
    }
    
    class WaveStatus {
        <<enumeration>>
        PLANNED
        RELEASED
        CLOSED
    }
    
    class PickList {
        -PickListId id
        -PickListStatus status
        -String pickerId
        -List~PickInstruction~ instructions
        -WaveId waveId
        +assignToPicker(pickerId)
        +confirmPick(sku, quantity)
        +getNextInstruction()
        +isComplete() boolean
    }
    
    class PickInstruction {
        -int sequence
        -SkuCode sku
        -Quantity quantityToPick
        -BinLocation location
        -boolean completed
        +markCompleted()
    }
    
    class Package {
        -PackageId id
        -OrderId orderId
        -List~PackedItem~ items
        -Weight weight
        -Dimensions dimensions
        -Instant packedAt
        +addItem(item)
        +calculateWeight()
        +isComplete() boolean
    }
    
    WorkloadOrchestrator --> IWorkloadReleaseStrategy
    IWorkloadReleaseStrategy <|-- ContinuousStrategy
    IWorkloadReleaseStrategy <|-- WaveStrategy
    WaveStrategy --> Wave
    Wave --> WaveStatus
    ContinuousStrategy --> PickList
    Wave --> PickList
    PickList --> PickInstruction
    PickList --> Package
```

## Event Flow Diagram

```mermaid
sequenceDiagram
    participant OM as Order Management
    participant Kafka as Kafka
    participant WO as Workload Orchestrator
    participant Strat as Release Strategy
    participant Wave as Wave Aggregate
    participant PL as PickList
    participant Pack as Package
    participant ST as Shipment Service
    
    OM->>Kafka: FulfillmentOrderValidated
    Kafka->>WO: consume event
    WO->>Strat: planWork(orders)
    
    alt Continuous Strategy
        Strat->>PL: create immediately
        PL->>WO: PickListCreated
    else Wave Strategy
        Strat->>Wave: accumulate orders
        Note over Wave: Wait for trigger
        Wave->>Wave: release()
        Wave->>PL: create multiple PickLists
        Wave->>WO: WaveReleased
    end
    
    Note over PL: Picking process
    PL->>PL: confirmPick(sku, qty)
    PL->>Pack: create when complete
    Pack->>Pack: confirmPacking()
    Pack->>Kafka: PackagePacked event
    Kafka->>ST: consume event
```

## Transactional Outbox Pattern

```mermaid
graph TB
    subgraph "Business Transaction"
        BT[Business Operation] --> AR[Update Aggregate]
        BT --> OE[Save Outbox Event]
    end
    
    subgraph "Database"
        AR --> DB[(MongoDB)]
        OE --> DB
    end
    
    subgraph "Background Process"
        OEP[Outbox Event Publisher] --> DB
        OEP --> Kafka[Kafka Producer]
        OEP --> UM[Update as Processed]
    end
    
    DB -.->|Poll for unprocessed| OEP
    UM --> DB
```

## Pick Route Optimization

```mermaid
graph TB
    subgraph "Warehouse Layout"
        A1[A1] --> A2[A2] --> A3[A3]
        B1[B1] --> B2[B2] --> B3[B3]
        C1[C1] --> C2[C2] --> C3[C3]
        
        A1 --> B1
        A2 --> B2
        A3 --> B3
        B1 --> C1
        B2 --> C2
        B3 --> C3
    end
    
    subgraph "Route Optimization"
        Start[Start Point] --> First[First Pick: A2]
        First --> Second[Second Pick: B1]
        Second --> Third[Third Pick: C3]
        Third --> End[End Point]
    end
    
    subgraph "Algorithm"
        Items[Pick Items] --> Sort[Sort by Location]
        Sort --> TSP[Apply TSP Algorithm]
        TSP --> Route[Optimized Route]
    end
```

## API Integration Points

```mermaid
graph LR
    subgraph "Inbound"
        Kafka1[Kafka Consumer] --> FO[FulfillmentOrderValidated]
    end
    
    subgraph "Warehouse Operations API"
        FO --> WO[Workload Orchestrator]
        
        subgraph "REST Endpoints"
            WaveAPI[POST /waves/{id}/release]
            PickAPI[POST /pick_lists/{id}/items/confirm_pick]
            PackAPI[POST /packages/{id}/confirm]
        end
        
        WO --> WaveAPI
        WO --> PickAPI
        WO --> PackAPI
    end
    
    subgraph "Outbound"
        PackAPI --> PP[PackagePacked Event]
        PP --> Kafka2[Kafka Producer]
    end
```

## Error Handling Strategy

```mermaid
graph TB
    subgraph "Error Types"
        DE[Domain Errors] --> BRV[Business Rule Violations]
        AE[Application Errors] --> COF[Coordination Failures]
        IE[Infrastructure Errors] --> TEF[Technical Failures]
    end
    
    subgraph "Error Handling"
        BRV --> DR[Domain Response]
        COF --> AR[Application Response]
        TEF --> IR[Infrastructure Response]
        
        DR --> HTTP400[HTTP 400]
        AR --> HTTP422[HTTP 422]
        IR --> HTTP500[HTTP 500]
    end
    
    subgraph "Recovery Strategies"
        HTTP400 --> UR[User Retry]
        HTTP422 --> AS[Automatic Retry]
        HTTP500 --> DLQ[Dead Letter Queue]
    end
```

## Testing Strategy Pyramid

```mermaid
graph TB
    subgraph "Testing Levels"
        E2E[End-to-End Tests<br/>Complete workflows]
        IT[Integration Tests<br/>Component interactions]
        UT[Unit Tests<br/>Individual components]
    end
    
    subgraph "Test Types"
        UT --> DT[Domain Tests]
        UT --> AT[Application Tests]
        UT --> VT[Value Object Tests]
        
        IT --> RT[Repository Tests]
        IT --> KT[Kafka Tests]
        IT --> CT[Controller Tests]
        
        E2E --> FT[Full Workflow Tests]
        E2E --> PT[Performance Tests]
        E2E --> ET[Error Scenario Tests]
    end
    
    UT -.->|Most tests| High[High Volume]
    IT -.->|Medium tests| Medium[Medium Volume]  
    E2E -.->|Few tests| Low[Low Volume]
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Production Environment"
        LB[Load Balancer] --> App1[App Instance 1]
        LB --> App2[App Instance 2]
        LB --> App3[App Instance 3]
        
        App1 --> MongoDB[(MongoDB Replica Set)]
        App2 --> MongoDB
        App3 --> MongoDB
        
        App1 --> Kafka[Kafka Cluster]
        App2 --> Kafka
        App3 --> Kafka
    end
    
    subgraph "Monitoring"
        App1 --> Metrics[Metrics Collection]
        App2 --> Metrics
        App3 --> Metrics
        Metrics --> Dashboard[Monitoring Dashboard]
    end
    
    subgraph "External Systems"
        Kafka --> UpstreamSvc[Upstream Services]
        Kafka --> DownstreamSvc[Downstream Services]
    end
```

## Key Architectural Decisions

### 1. Aggregate Sizing Strategy

- **WorkloadOrchestrator**: Single service instance - coordinates all work distribution
- **Wave**: Contains multiple orders bounded by business rules (carrier cutoffs)
- **PickList**: Single picker assignment - atomic unit of picking work
- **Package**: Single order's packed items - complete shipping unit

### 2. Event Consistency Patterns

- **Domain Events**: For intra-service communication between aggregates
- **Integration Events**: For inter-service communication via Kafka
- **Outbox Pattern**: Ensures reliable event publishing with database consistency

### 3. Read/Write Separation

- **Command Side**: Rich domain models with business logic
- **Query Side**: Optimized projections for UI and reporting
- **Event Store**: Audit trail of all domain events

### 4. Error Recovery Mechanisms

- **Transient Failures**: Automatic retry with exponential backoff
- **Business Failures**: User notification with corrective actions
- **System Failures**: Dead letter queue with manual intervention

This architecture ensures:
- ✅ Clear separation of concerns
- ✅ Scalable event-driven design
- ✅ Reliable data consistency
- ✅ Comprehensive error handling
- ✅ Testable component structure