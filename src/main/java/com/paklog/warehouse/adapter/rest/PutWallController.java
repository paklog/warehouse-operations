package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.application.service.PutWallApplicationService;
import com.paklog.warehouse.domain.putwall.*;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/putwalls")
@Tag(name = "Put Walls", description = "REST API for managing put wall operations")
public class PutWallController {

    private final PutWallApplicationService putWallService;

    public PutWallController(PutWallApplicationService putWallService) {
        this.putWallService = putWallService;
    }

    @PostMapping
    @Operation(summary = "Create a new put wall", description = "Creates a new put wall with specified slots and location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Put wall created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<PutWallResponse> createPutWall(@RequestBody @Valid CreatePutWallRequest request) {
        List<PutWallSlotId> slotIds = request.getSlotIds().stream()
            .map(PutWallSlotId::of)
            .collect(Collectors.toList());

        PutWallId putWallId = putWallService.createPutWall(slotIds, request.getLocation());

        Optional<PutWall> putWall = putWallService.getPutWall(putWallId);
        if (putWall.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(PutWallResponse.fromDomain(putWall.get()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/{putWallId}/assignments")
    @Operation(summary = "Assign order to slot", description = "Assigns an order to an available slot in the put wall")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order assigned successfully"),
        @ApiResponse(responseCode = "404", description = "Put wall not found"),
        @ApiResponse(responseCode = "409", description = "No available slots")
    })
    public ResponseEntity<SlotAssignmentResponse> assignOrderToSlot(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId,
            @RequestBody @Valid AssignOrderRequest request) {

        try {
            AssignOrderToSlotCommand command = new AssignOrderToSlotCommand(
                PutWallId.of(putWallId),
                OrderId.of(request.getOrderId()),
                mapToRequiredItems(request.getRequiredItems())
            );

            Optional<PutWallSlotId> assignedSlot = putWallService.assignOrderToSlot(command);

            if (assignedSlot.isPresent()) {
                return ResponseEntity.ok(new SlotAssignmentResponse(
                    assignedSlot.get().getValue(),
                    request.getOrderId(),
                    "Order assigned successfully"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SlotAssignmentResponse(null, request.getOrderId(), "No available slots"));
            }
        } catch (PutWallException.PutWallNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (PutWallException.PutWallCapacityExceededException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new SlotAssignmentResponse(null, request.getOrderId(), "No available slots"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{putWallId}/scan")
    @Operation(summary = "Scan item for sortation", description = "Scans an item to determine target slot")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sortation target determined"),
        @ApiResponse(responseCode = "404", description = "Put wall not found or no target slot found")
    })
    public ResponseEntity<SortationResponse> scanItemForSortation(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId,
            @RequestBody @Valid ScanItemRequest request) {

        try {
            ScanItemForSortationCommand command = new ScanItemForSortationCommand(
                PutWallId.of(putWallId),
                SkuCode.of(request.getSkuCode()),
                Quantity.of(request.getQuantity())
            );

            PutWallService.SortationResult result = putWallService.scanItemForSortation(command);

            if (result.isFound()) {
                return ResponseEntity.ok(new SortationResponse(
                    result.getTargetSlotId().getValue(),
                    result.getOrderId().toString(),
                    result.getQuantityNeeded().getValue(),
                    "Target slot found"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SortationResponse(null, null, 0, result.getReason()));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{putWallId}/slots/{slotId}/items")
    @Operation(summary = "Confirm item placement", description = "Confirms placement of an item in a specific slot")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item placed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid placement"),
        @ApiResponse(responseCode = "404", description = "Put wall or slot not found")
    })
    public ResponseEntity<String> confirmPutInSlot(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId,
            @PathVariable @Parameter(description = "Slot ID") String slotId,
            @RequestBody @Valid PlaceItemRequest request) {

        try {
            ConfirmPutInSlotCommand command = new ConfirmPutInSlotCommand(
                PutWallId.of(putWallId),
                PutWallSlotId.of(slotId),
                SkuCode.of(request.getSkuCode()),
                Quantity.of(request.getQuantity())
            );

            putWallService.confirmPutInSlot(command);
            return ResponseEntity.ok("Item placed successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{putWallId}/slots/{slotId}")
    @Operation(summary = "Release slot", description = "Releases a completed slot for new order assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slot released successfully"),
        @ApiResponse(responseCode = "400", description = "Slot cannot be released"),
        @ApiResponse(responseCode = "404", description = "Put wall or slot not found")
    })
    public ResponseEntity<String> releaseSlot(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId,
            @PathVariable @Parameter(description = "Slot ID") String slotId) {

        try {
            putWallService.releaseSlot(PutWallId.of(putWallId), PutWallSlotId.of(slotId));
            return ResponseEntity.ok("Slot released successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{putWallId}")
    @Operation(summary = "Get put wall details", description = "Retrieves detailed information about a put wall")
    public ResponseEntity<PutWallResponse> getPutWall(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId) {

        Optional<PutWall> putWall = putWallService.getPutWall(PutWallId.of(putWallId));

        return putWall.map(wall -> ResponseEntity.ok(PutWallResponse.fromDomain(wall)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{putWallId}/ready-slots")
    @Operation(summary = "Get ready for pack slots", description = "Retrieves slots that are ready for packing")
    public ResponseEntity<List<String>> getReadyForPackSlots(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId) {

        try {
            List<PutWallSlotId> readySlots = putWallService.getReadyForPackSlots(PutWallId.of(putWallId));
            List<String> slotIds = readySlots.stream()
                .map(PutWallSlotId::getValue)
                .collect(Collectors.toList());

            return ResponseEntity.ok(slotIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all put walls", description = "Retrieves all put walls")
    public ResponseEntity<List<PutWallResponse>> getAllPutWalls() {
        List<PutWall> putWalls = putWallService.getAllPutWalls();
        List<PutWallResponse> responses = putWalls.stream()
            .map(PutWallResponse::fromDomain)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private Map<SkuCode, Quantity> mapToRequiredItems(Map<String, Integer> requiredItems) {
        return requiredItems.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> SkuCode.of(entry.getKey()),
                entry -> Quantity.of(entry.getValue())
            ));
    }

    // Request/Response DTOs
    public static class CreatePutWallRequest {
        @NotEmpty(message = "Slot IDs cannot be empty")
        private List<@NotBlank String> slotIds;

        @NotBlank(message = "Location is required")
        private String location;

        // Getters and setters
        public List<String> getSlotIds() { return slotIds; }
        public void setSlotIds(List<String> slotIds) { this.slotIds = slotIds; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class AssignOrderRequest {
        @NotBlank(message = "Order ID is required")
        private String orderId;

        @NotEmpty(message = "Required items cannot be empty")
        private Map<@NotBlank String, @Positive Integer> requiredItems;

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public Map<String, Integer> getRequiredItems() { return requiredItems; }
        public void setRequiredItems(Map<String, Integer> requiredItems) { this.requiredItems = requiredItems; }
    }

    public static class ScanItemRequest {
        @NotBlank(message = "SKU code is required")
        private String skuCode;

        @Positive(message = "Quantity must be positive")
        private int quantity;

        // Getters and setters
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class PlaceItemRequest {
        @NotBlank(message = "SKU code is required")
        private String skuCode;

        @Positive(message = "Quantity must be positive")
        private int quantity;

        // Getters and setters
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class SlotAssignmentResponse {
        private final String slotId;
        private final String orderId;
        private final String message;

        public SlotAssignmentResponse(String slotId, String orderId, String message) {
            this.slotId = slotId;
            this.orderId = orderId;
            this.message = message;
        }

        // Getters
        public String getSlotId() { return slotId; }
        public String getOrderId() { return orderId; }
        public String getMessage() { return message; }
    }

    public static class SortationResponse {
        private final String targetSlotId;
        private final String orderId;
        private final int quantityNeeded;
        private final String message;

        public SortationResponse(String targetSlotId, String orderId, int quantityNeeded, String message) {
            this.targetSlotId = targetSlotId;
            this.orderId = orderId;
            this.quantityNeeded = quantityNeeded;
            this.message = message;
        }

        // Getters
        public String getTargetSlotId() { return targetSlotId; }
        public String getOrderId() { return orderId; }
        public int getQuantityNeeded() { return quantityNeeded; }
        public String getMessage() { return message; }
    }
}