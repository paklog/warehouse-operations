# Implementation Examples

## Common Patterns and Best Practices

### 1. Creating and Processing Pick Lists

#### Basic Pick List Workflow
```java
@Service
@Transactional
public class PickListWorkflowService {

    private final PickListRepository pickListRepository;
    private final DomainEventPublisher eventPublisher;
    private final PickerAssignmentService pickerAssignmentService;

    // Create optimized pick list from order
    public PickList createPickList(OrderId orderId, List<OrderItem> items) {
        PickList pickList = new PickList(orderId);

        // Add instructions in optimal order
        List<PickInstruction> optimizedInstructions = optimizePickingRoute(items);
        optimizedInstructions.forEach(pickList::addInstruction);

        // Assign to best available picker
        String bestPicker = pickerAssignmentService.findBestAvailablePicker(
            pickList.getInstructions()
        );
        pickList.assignToPicker(bestPicker);

        // Persist and publish events
        pickListRepository.save(pickList);
        eventPublisher.publishAll(pickList.getDomainEvents());
        pickList.clearDomainEvents();

        return pickList;
    }

    // Process item pick with validation
    public void processItemPick(PickListId pickListId, SkuCode sku,
                               Quantity quantity, BinLocation location) {
        PickList pickList = pickListRepository.findById(pickListId);
        if (pickList == null) {
            throw new PickListNotFoundException(pickListId);
        }

        // Validate picker assignment
        if (!pickList.hasAssignedPicker()) {
            throw new PickListNotAssignedException(pickListId);
        }

        // Validate location
        if (!locationService.isValidLocation(location)) {
            throw new InvalidLocationException(location);
        }

        // Process the pick
        pickList.pickItem(sku, quantity, location);

        // Save and publish events
        pickListRepository.save(pickList);
        eventPublisher.publishAll(pickList.getDomainEvents());
        pickList.clearDomainEvents();

        // If completed, trigger downstream processes
        if (pickList.isComplete()) {
            publishPickListCompletedEvent(pickList);
        }
    }

    private List<PickInstruction> optimizePickingRoute(List<OrderItem> items) {
        return items.stream()
            .map(item -> {
                BinLocation optimalLocation = locationService.findOptimalLocation(item.getSkuCode());
                return new PickInstruction(item.getSkuCode(), item.getQuantity(), optimalLocation);
            })
            .sorted(Comparator.comparing(instruction ->
                locationService.getLocationDistance(instruction.getBinLocation())))
            .collect(Collectors.toList());
    }
}
```

#### Advanced Pick List Management
```java
@Component
public class AdvancedPickListService {

    public void processBatchPicking(List<PickListId> pickListIds, String pickerId) {
        List<PickList> pickLists = pickListRepository.findAllById(pickListIds);

        // Validate all pick lists can be batch processed
        validateBatchPickingEligibility(pickLists, pickerId);

        // Create consolidated picking route
        List<PickInstruction> consolidatedRoute = createConsolidatedRoute(pickLists);

        // Process each instruction
        for (PickInstruction instruction : consolidatedRoute) {
            // Find which pick list this instruction belongs to
            PickList targetPickList = findPickListForInstruction(pickLists, instruction);

            targetPickList.pickItem(
                instruction.getSku(),
                instruction.getQuantity(),
                instruction.getBinLocation()
            );
        }

        // Save all pick lists and publish events
        pickLists.forEach(pickList -> {
            pickListRepository.save(pickList);
            eventPublisher.publishAll(pickList.getDomainEvents());
            pickList.clearDomainEvents();
        });
    }

    public PickListMetrics calculatePickListMetrics(PickListId pickListId) {
        PickList pickList = pickListRepository.findById(pickListId);

        return PickListMetrics.builder()
            .pickListId(pickListId)
            .totalInstructions(pickList.getInstructions().size())
            .completedInstructions(pickList.getCompletedInstructionCount())
            .completionPercentage(pickList.getCompletionPercentage())
            .estimatedTimeRemaining(calculateEstimatedTime(pickList))
            .totalDistance(calculateTotalDistance(pickList))
            .averagePickTime(calculateAveragePickTime(pickList))
            .build();
    }
}
```

### 2. Package Management Patterns

#### Package Creation and Confirmation
```java
@Service
public class PackageManagementService {

    // Create package with automatic item allocation
    public Package createPackageFromPickList(PickList completedPickList) {
        if (!completedPickList.isComplete()) {
            throw new IllegalArgumentException("Cannot create package from incomplete pick list");
        }

        List<PackedItem> packedItems = completedPickList.getInstructions().stream()
            .filter(PickInstruction::isCompleted)
            .map(instruction -> new PackedItem(
                instruction.getSku(),
                instruction.getQuantity().getValue()
            ))
            .collect(Collectors.toList());

        Package package = new Package(UUID.randomUUID(), packedItems);

        packageRepository.save(package);
        eventPublisher.publish(new PackageCreatedEvent(
            package.getPackageId(),
            completedPickList.getOrderId(),
            packedItems
        ));

        return package;
    }

    // Smart package confirmation with validation
    public void confirmPackageWithValidation(UUID packageId, PackageConfirmationRequest request) {
        Package package = packageRepository.findById(packageId)
            .orElseThrow(() -> new PackageNotFoundException(packageId));

        // Validate package contents match expected
        validatePackageContents(package, request.getExpectedItems());

        // Validate shipping information
        validateShippingInformation(request.getShippingInfo());

        // Validate package weight and dimensions
        if (request.getActualWeight() != null) {
            validatePackageWeight(package, request.getActualWeight());
        }

        // Confirm the package
        package.confirmPacking();

        // Add shipping label if provided
        if (request.getShippingLabel() != null) {
            package.attachShippingLabel(request.getShippingLabel());
        }

        packageRepository.save(package);
        eventPublisher.publish(new PackageConfirmedEvent(
            packageId,
            request.getActualWeight(),
            request.getShippingInfo()
        ));
    }

    // Package splitting for oversized orders
    public List<Package> splitPackage(Package originalPackage, PackageSplitStrategy strategy) {
        if (originalPackage.getStatus() != PackageStatus.PENDING) {
            throw new IllegalStateException("Can only split pending packages");
        }

        List<List<PackedItem>> splitGroups = strategy.splitItems(originalPackage.getPackedItems());

        List<Package> splitPackages = splitGroups.stream()
            .map(items -> new Package(UUID.randomUUID(), items))
            .collect(Collectors.toList());

        // Mark original as split
        originalPackage.markAsSplit();

        // Save all packages
        splitPackages.forEach(packageRepository::save);
        packageRepository.save(originalPackage);

        // Publish events
        eventPublisher.publish(new PackageSplitEvent(
            originalPackage.getPackageId(),
            splitPackages.stream().map(Package::getPackageId).collect(Collectors.toList())
        ));

        return splitPackages;
    }
}
```

### 3. Quality Control Workflows

#### Comprehensive Quality Inspection
```java
@Service
public class QualityControlService {

    public QualityInspection initiateInspection(LicensePlateId licensePlateId,
                                               QualityInspectionType type,
                                               String inspectorId) {
        LicensePlate licensePlate = licensePlateRepository.findById(licensePlateId);
        if (licensePlate == null) {
            throw new LicensePlateNotFoundException(licensePlateId);
        }

        QualityInspection inspection = new QualityInspection(
            QualityInspectionId.generate(),
            licensePlateId,
            inspectorId
        );

        // Add appropriate checks based on type
        switch (type) {
            case RECEIVING_INSPECTION:
                addReceivingChecks(inspection);
                break;
            case DAMAGE_INSPECTION:
                addDamageChecks(inspection);
                break;
            case COUNT_VERIFICATION:
                addCountChecks(inspection, licensePlate);
                break;
        }

        qualityInspectionRepository.save(inspection);
        eventPublisher.publish(new QualityInspectionInitiatedEvent(
            inspection.getId(),
            licensePlateId,
            type,
            inspectorId
        ));

        return inspection;
    }

    public void completeInspectionCheck(QualityInspectionId inspectionId,
                                       QualityCheckType checkType,
                                       QualityResult result,
                                       String notes) {
        QualityInspection inspection = qualityInspectionRepository.findById(inspectionId);

        inspection.completeCheck(checkType, result, notes);

        qualityInspectionRepository.save(inspection);

        // If inspection is complete, determine next action
        if (inspection.isComplete()) {
            handleCompletedInspection(inspection);
        }
    }

    private void handleCompletedInspection(QualityInspection inspection) {
        if (inspection.hasPassed()) {
            // Release license plate for normal processing
            LicensePlate licensePlate = licensePlateRepository.findById(inspection.getLicensePlateId());
            licensePlate.clearQualityFlags();
            licensePlateRepository.save(licensePlate);

            eventPublisher.publish(new QualityInspectionPassedEvent(
                inspection.getId(),
                inspection.getLicensePlateId()
            ));
        } else {
            // Create quality hold
            createQualityHold(inspection);
        }
    }

    private void createQualityHold(QualityInspection inspection) {
        List<QualityCheck> failedChecks = inspection.getFailedChecks();
        QualityHoldReason reason = determineHoldReason(failedChecks);

        QualityHold hold = new QualityHold(
            QualityHoldId.generate(),
            inspection.getLicensePlateId(),
            reason,
            "Failed inspection: " + inspection.getId()
        );

        qualityHoldRepository.save(hold);
        eventPublisher.publish(new QualityHoldCreatedEvent(
            hold.getId(),
            inspection.getLicensePlateId(),
            reason
        ));
    }
}
```

### 4. Mobile Device Integration

#### Mobile Workflow Service
```java
@Service
public class MobileWorkflowService {

    public MobileWorkSummaryDto getWorkSummary(String workerId) {
        // Get assigned pick lists
        List<PickList> assignedPickLists = pickListRepository.findByPickerId(workerId);

        // Get pending quality inspections
        List<QualityInspection> pendingInspections =
            qualityInspectionRepository.findPendingByInspector(workerId);

        // Calculate metrics
        WorkerMetrics metrics = calculateWorkerMetrics(workerId);

        return MobileWorkSummaryDto.builder()
            .workerId(workerId)
            .assignedPickLists(assignedPickLists.stream()
                .map(MobilePickListDto::fromDomain)
                .collect(Collectors.toList()))
            .pendingInspections(pendingInspections.stream()
                .map(MobileInspectionDto::fromDomain)
                .collect(Collectors.toList()))
            .todayMetrics(metrics)
            .nextRecommendedTask(getNextRecommendedTask(workerId))
            .build();
    }

    public MobileScanResultDto processScan(MobileScanRequest request) {
        BarcodeType barcodeType = barcodeDetectionService.detectType(request.getBarcode());

        switch (barcodeType) {
            case SKU_BARCODE:
                return processSkuScan(request);
            case LICENSE_PLATE_BARCODE:
                return processLicensePlateScan(request);
            case LOCATION_BARCODE:
                return processLocationScan(request);
            default:
                return MobileScanResultDto.invalid("Unknown barcode type");
        }
    }

    private MobileScanResultDto processSkuScan(MobileScanRequest request) {
        SkuCode skuCode = SkuCode.of(request.getBarcode());

        // Find active pick list for this worker
        PickList activePickList = pickListRepository.findActiveByPickerId(request.getWorkerId());
        if (activePickList == null) {
            return MobileScanResultDto.error("No active pick list found");
        }

        // Find instruction for this SKU
        PickInstruction instruction = activePickList.findInstructionForSku(skuCode);
        if (instruction == null) {
            return MobileScanResultDto.error("SKU not found in current pick list");
        }

        // Validate location if provided
        if (request.getLocation() != null) {
            BinLocation scannedLocation = BinLocation.of(request.getLocation());
            if (!instruction.getBinLocation().equals(scannedLocation)) {
                return MobileScanResultDto.locationMismatch(
                    instruction.getBinLocation(),
                    scannedLocation
                );
            }
        }

        return MobileScanResultDto.success(
            "SKU found",
            MobileStepDto.builder()
                .instruction(MobileInstructionDto.fromDomain(instruction))
                .expectedLocation(instruction.getBinLocation().getValue())
                .expectedQuantity(instruction.getQuantity().getValue())
                .build()
        );
    }

    public MobileStepCompletionDto completeStep(MobileStepCompletionRequest request) {
        try {
            // Validate the step can be completed
            validateStepCompletion(request);

            // Execute the step based on type
            switch (request.getStepType()) {
                case PICK_ITEM:
                    return completePickStep(request);
                case QUALITY_CHECK:
                    return completeQualityStep(request);
                case LOCATION_VERIFICATION:
                    return completeLocationStep(request);
                default:
                    throw new UnsupportedOperationException("Unknown step type: " + request.getStepType());
            }
        } catch (Exception e) {
            return MobileStepCompletionDto.error(e.getMessage());
        }
    }

    private MobileStepCompletionDto completePickStep(MobileStepCompletionRequest request) {
        PickListId pickListId = PickListId.of(request.getPickListId());
        SkuCode skuCode = SkuCode.of(request.getSkuCode());
        Quantity quantity = Quantity.of(request.getQuantity());
        BinLocation location = BinLocation.of(request.getLocation());

        // Process the pick
        pickListService.processItemPick(pickListId, skuCode, quantity, location);

        // Get updated pick list status
        PickList updatedPickList = pickListRepository.findById(pickListId);

        return MobileStepCompletionDto.success(
            "Item picked successfully",
            MobileWorkDetailDto.fromDomain(updatedPickList)
        );
    }
}
```

### 5. Event-Driven Integration Patterns

#### Domain Event Handlers
```java
@Component
public class WarehouseEventHandlers {

    // Automatic package creation when pick list completes
    @EventListener
    @Async
    public void handle(PickListCompletedEvent event) {
        try {
            PickList completedPickList = pickListRepository.findById(event.getPickListId());

            // Create package automatically
            Package package = packageService.createPackageFromPickList(completedPickList);

            log.info("Automatically created package {} for completed pick list {}",
                    package.getPackageId(), event.getPickListId());

        } catch (Exception e) {
            log.error("Failed to create package for pick list {}: {}",
                     event.getPickListId(), e.getMessage());
            // Could publish compensation event or alert
        }
    }

    // Update inventory when license plate moves
    @EventListener
    @Transactional
    public void handle(LicensePlateMovedEvent event) {
        // Update location tracking
        locationTrackingService.updateLocation(
            event.getLicensePlateId(),
            event.getFromLocation(),
            event.getToLocation(),
            event.getMovedAt()
        );

        // Update inventory availability
        inventoryService.updateLocationAvailability(
            event.getToLocation(),
            event.getLicensePlateId()
        );

        // Publish inventory updated event for external systems
        eventPublisher.publish(new InventoryLocationUpdatedEvent(
            event.getLicensePlateId(),
            event.getToLocation(),
            event.getMovedAt()
        ));
    }

    // Trigger quality inspection for high-value items
    @EventListener
    public void handle(LicensePlateInventoryAddedEvent event) {
        if (event.getValue().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            // Schedule quality inspection for high-value items
            QualityInspection inspection = qualityService.scheduleInspection(
                event.getLicensePlateId(),
                QualityInspectionType.HIGH_VALUE_VERIFICATION,
                QualityInspectionPriority.HIGH
            );

            log.info("Scheduled high-value inspection {} for license plate {}",
                    inspection.getId(), event.getLicensePlateId());
        }
    }

    // Handle wave completion
    @EventListener
    public void handle(WaveCompletedEvent event) {
        // Generate wave completion report
        WaveCompletionReport report = reportingService.generateWaveReport(event.getWaveId());

        // Send notification to warehouse managers
        notificationService.sendWaveCompletionNotification(
            report,
            getWarehouseManagers()
        );

        // Update wave metrics
        metricsService.recordWaveCompletion(
            event.getWaveId(),
            report.getTotalProcessingTime(),
            report.getEfficiencyScore()
        );
    }
}
```

### 6. Testing Patterns

#### Domain Unit Tests
```java
class PickListDomainTest {

    @Test
    void shouldCompletePickListWhenAllItemsPicked() {
        // Given
        PickList pickList = new PickList(OrderId.of("ORD-123"));
        pickList.addInstruction(new PickInstruction(
            SkuCode.of("SKU-001"),
            Quantity.of(5),
            BinLocation.of("A1-B2-C3")
        ));
        pickList.assignToPicker("PICKER-001");

        // When
        pickList.pickItem(SkuCode.of("SKU-001"), Quantity.of(5), BinLocation.of("A1-B2-C3"));

        // Then
        assertThat(pickList.isComplete()).isTrue();
        assertThat(pickList.getStatus()).isEqualTo(PickListStatus.COMPLETED);

        List<DomainEvent> events = pickList.getDomainEvents();
        assertThat(events).hasSize(3); // Assigned, ItemPicked, Completed
        assertThat(events.get(2)).isInstanceOf(PickListCompletedEvent.class);
    }

    @Test
    void shouldThrowExceptionWhenPickingWithWrongQuantity() {
        // Given
        PickList pickList = createAssignedPickList();

        // When & Then
        assertThatThrownBy(() ->
            pickList.pickItem(SkuCode.of("SKU-001"), Quantity.of(3), BinLocation.of("A1-B2-C3"))
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid pick instruction");
    }
}
```

#### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PickListWorkflowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PickListRepository pickListRepository;

    @Test
    void shouldCompleteFullPickListWorkflow() {
        // Given - create pick list via API
        CreatePickListRequest createRequest = CreatePickListRequest.builder()
            .orderId("ORD-123")
            .items(Arrays.asList(
                new OrderItemRequest("SKU-001", 5),
                new OrderItemRequest("SKU-002", 3)
            ))
            .build();

        ResponseEntity<PickListDto> createResponse = restTemplate.postForEntity(
            "/api/v1/picklists", createRequest, PickListDto.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String pickListId = createResponse.getBody().getId();

        // When - assign picker
        ResponseEntity<Void> assignResponse = restTemplate.postForEntity(
            "/api/v1/picklists/" + pickListId + "/assign",
            new AssignPickerRequest("PICKER-001"),
            Void.class);

        assertThat(assignResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Then - confirm picks
        ConfirmItemPickRequest pickRequest1 = new ConfirmItemPickRequest(
            "SKU-001", 5, "A1-B2-C3");
        ResponseEntity<Void> pick1Response = restTemplate.postForEntity(
            "/api/v1/picklists/" + pickListId + "/confirm-pick",
            pickRequest1,
            Void.class);

        assertThat(pick1Response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ConfirmItemPickRequest pickRequest2 = new ConfirmItemPickRequest(
            "SKU-002", 3, "A2-B1-C4");
        ResponseEntity<Void> pick2Response = restTemplate.postForEntity(
            "/api/v1/picklists/" + pickListId + "/confirm-pick",
            pickRequest2,
            Void.class);

        assertThat(pick2Response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify final state
        ResponseEntity<PickListDto> finalResponse = restTemplate.getForEntity(
            "/api/v1/picklists/" + pickListId, PickListDto.class);

        PickListDto finalPickList = finalResponse.getBody();
        assertThat(finalPickList.getStatus()).isEqualTo("COMPLETED");
        assertThat(finalPickList.getInstructions()).allMatch(instruction -> instruction.isCompleted());
    }
}
```

#### Performance Testing
```java
@Test
void shouldHandleHighVolumePickListCreation() {
    // Given
    int numberOfPickLists = 1000;
    List<CreatePickListRequest> requests = generatePickListRequests(numberOfPickLists);

    // When
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    List<PickList> createdPickLists = requests.parallelStream()
        .map(pickListService::createPickList)
        .collect(Collectors.toList());

    stopWatch.stop();

    // Then
    assertThat(createdPickLists).hasSize(numberOfPickLists);
    assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // Less than 5 seconds

    // Verify all have events
    createdPickLists.forEach(pickList -> {
        assertThat(pickList.getDomainEvents()).isNotEmpty();
    });
}
```

This comprehensive examples guide demonstrates the main patterns and practices used throughout the warehouse operations service, providing practical code examples for common scenarios and integration patterns.