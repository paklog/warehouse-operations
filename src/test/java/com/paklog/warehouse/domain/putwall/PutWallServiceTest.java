package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PutWallServiceTest {

    private PutWallService putWallService;
    private PutWall putWall;

    @BeforeEach
    void setUp() {
        putWallService = new PutWallService();

        PutWallId putWallId = PutWallId.generate();
        List<PutWallSlotId> slotIds = List.of(
            PutWallSlotId.of("A1"),
            PutWallSlotId.of("A2"),
            PutWallSlotId.of("A3")
        );
        putWall = new PutWall(putWallId, slotIds, "Test Location");
    }

    @Test
    void shouldFindSortationTargetForRequiredSku() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(3));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // Test sortation determination
        PutWallService.SortationResult result = putWallService.determineSortationTarget(putWall, skuCode);

        assertTrue(result.isFound());
        assertEquals(assignedSlot.get(), result.getTargetSlotId());
        assertEquals(orderId, result.getOrderId());
        assertEquals(Quantity.of(3), result.getQuantityNeeded());
    }

    @Test
    void shouldReturnNotFoundForUnrequiredSku() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode requiredSku = SkuCode.of("SKU001");
        SkuCode unrequiredSku = SkuCode.of("SKU999");
        Map<SkuCode, Quantity> requiredItems = Map.of(requiredSku, Quantity.of(1));

        putWall.assignOrderToSlot(orderId, requiredItems);

        // Test sortation for unrequired SKU
        PutWallService.SortationResult result = putWallService.determineSortationTarget(putWall, unrequiredSku);

        assertFalse(result.isFound());
        assertNull(result.getTargetSlotId());
        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("No active slot requires SKU"));
    }

    @Test
    void shouldCalculateRemainingQuantityNeeded() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(5));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // Place some items
        putWall.placeItemInSlot(assignedSlot.get(), skuCode, Quantity.of(2));

        // Test sortation determination
        PutWallService.SortationResult result = putWallService.determineSortationTarget(putWall, skuCode);

        assertTrue(result.isFound());
        assertEquals(Quantity.of(3), result.getQuantityNeeded()); // 5 required - 2 placed = 3 remaining
    }

    @Test
    void shouldNotReturnCompletedSku() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(2));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // Place all required items
        putWall.placeItemInSlot(assignedSlot.get(), skuCode, Quantity.of(2));

        // Test sortation determination
        PutWallService.SortationResult result = putWallService.determineSortationTarget(putWall, skuCode);

        assertFalse(result.isFound());
        assertNotNull(result.getReason());
    }

    @Test
    void shouldValidateCorrectItemPlacement() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(3));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // This should not throw an exception
        assertDoesNotThrow(() -> putWallService.validateItemPlacement(
            putWall, assignedSlot.get(), skuCode, Quantity.of(2)
        ));
    }

    @Test
    void shouldThrowExceptionForInvalidItemPlacement() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(2));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // Try to place more than required
        assertThrows(IllegalArgumentException.class, () ->
            putWallService.validateItemPlacement(putWall, assignedSlot.get(), skuCode, Quantity.of(3))
        );
    }

    @Test
    void shouldThrowExceptionForUnrequiredSkuPlacement() {
        // Assign an order to a slot
        OrderId orderId = OrderId.generate();
        SkuCode requiredSku = SkuCode.of("SKU001");
        SkuCode unrequiredSku = SkuCode.of("SKU999");
        Map<SkuCode, Quantity> requiredItems = Map.of(requiredSku, Quantity.of(1));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // Try to place unrequired SKU
        assertThrows(IllegalArgumentException.class, () ->
            putWallService.validateItemPlacement(putWall, assignedSlot.get(), unrequiredSku, Quantity.of(1))
        );
    }

    @Test
    void shouldThrowExceptionForNonProgressSlotPlacement() {
        PutWallSlotId freeSlotId = PutWallSlotId.of("A1");
        SkuCode skuCode = SkuCode.of("SKU001");

        assertThrows(IllegalStateException.class, () ->
            putWallService.validateItemPlacement(putWall, freeSlotId, skuCode, Quantity.of(1))
        );
    }

    @Test
    void shouldThrowExceptionForNullInputs() {
        assertThrows(IllegalArgumentException.class, () ->
            putWallService.determineSortationTarget(null, SkuCode.of("SKU001"))
        );

        assertThrows(IllegalArgumentException.class, () ->
            putWallService.determineSortationTarget(putWall, null)
        );

        assertThrows(IllegalArgumentException.class, () ->
            putWallService.validateItemPlacement(null, PutWallSlotId.of("A1"), SkuCode.of("SKU001"), Quantity.of(1))
        );
    }

    @Test
    void shouldFindFirstAvailableSlotWithRequiredSku() {
        // Assign orders to multiple slots
        OrderId orderId1 = OrderId.generate();
        OrderId orderId2 = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");

        Map<SkuCode, Quantity> requiredItems1 = Map.of(
            SkuCode.of("SKU002"), Quantity.of(1)
        );
        Map<SkuCode, Quantity> requiredItems2 = Map.of(
            skuCode, Quantity.of(2)
        );

        putWall.assignOrderToSlot(orderId1, requiredItems1);
        Optional<PutWallSlotId> assignedSlot2 = putWall.assignOrderToSlot(orderId2, requiredItems2);
        assertTrue(assignedSlot2.isPresent());

        // Test sortation should find the slot with the required SKU
        PutWallService.SortationResult result = putWallService.determineSortationTarget(putWall, skuCode);

        assertTrue(result.isFound());
        assertEquals(assignedSlot2.get(), result.getTargetSlotId());
        assertEquals(orderId2, result.getOrderId());
    }
}