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

class PutWallTest {

    private PutWall putWall;
    private PutWallId putWallId;
    private List<PutWallSlotId> slotIds;

    @BeforeEach
    void setUp() {
        putWallId = PutWallId.generate();
        slotIds = List.of(
            PutWallSlotId.of("A1"),
            PutWallSlotId.of("A2"),
            PutWallSlotId.of("A3")
        );
        putWall = new PutWall(putWallId, slotIds, "Warehouse Zone A");
    }

    @Test
    void shouldCreatePutWallWithSlots() {
        assertEquals(putWallId, putWall.getPutWallId());
        assertEquals("Warehouse Zone A", putWall.getLocation());
        assertEquals(3, putWall.getCapacity());
        assertEquals(3, putWall.getAvailableCapacity());
        assertFalse(putWall.isFull());
    }

    @Test
    void shouldAssignOrderToSlot() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(
            SkuCode.of("SKU001"), Quantity.of(2),
            SkuCode.of("SKU002"), Quantity.of(1)
        );

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);

        assertTrue(assignedSlot.isPresent());
        assertEquals(2, putWall.getAvailableCapacity());

        PutWallSlot slot = putWall.getSlotById(assignedSlot.get());
        assertEquals(orderId, slot.getAssignedOrderId());
        assertEquals(PutWallSlotStatus.IN_PROGRESS, slot.getStatus());
        assertEquals(2, slot.getItemsRequired().size());
    }

    @Test
    void shouldThrowExceptionWhenAssigningToFullPutWall() {
        // Fill all slots
        for (int i = 0; i < 3; i++) {
            OrderId orderId = OrderId.generate();
            Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU" + i), Quantity.of(1));
            putWall.assignOrderToSlot(orderId, requiredItems);
        }

        assertTrue(putWall.isFull());
        assertEquals(0, putWall.getAvailableCapacity());

        // Attempt to assign another order
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU999"), Quantity.of(1));

        assertThrows(PutWallException.PutWallCapacityExceededException.class,
            () -> putWall.assignOrderToSlot(orderId, requiredItems));
    }

    @Test
    void shouldPlaceItemInSlot() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(3));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        putWall.placeItemInSlot(assignedSlot.get(), skuCode, Quantity.of(2));

        PutWallSlot slot = putWall.getSlotById(assignedSlot.get());
        assertEquals(Quantity.of(2), slot.getItemsPlaced().get(skuCode));
        assertEquals(PutWallSlotStatus.IN_PROGRESS, slot.getStatus());
        assertFalse(slot.isOrderComplete());
    }

    @Test
    void shouldCompleteOrderWhenAllItemsPlaced() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode1 = SkuCode.of("SKU001");
        SkuCode skuCode2 = SkuCode.of("SKU002");
        Map<SkuCode, Quantity> requiredItems = Map.of(
            skuCode1, Quantity.of(2),
            skuCode2, Quantity.of(1)
        );

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        // Place first SKU
        putWall.placeItemInSlot(assignedSlot.get(), skuCode1, Quantity.of(2));

        PutWallSlot slot = putWall.getSlotById(assignedSlot.get());
        assertEquals(PutWallSlotStatus.IN_PROGRESS, slot.getStatus());

        // Place second SKU - should complete the order
        putWall.placeItemInSlot(assignedSlot.get(), skuCode2, Quantity.of(1));

        slot = putWall.getSlotById(assignedSlot.get());
        assertEquals(PutWallSlotStatus.READY_FOR_PACK, slot.getStatus());
        assertTrue(slot.isOrderComplete());
        assertTrue(slot.isReadyForPack());
    }

    @Test
    void shouldReleaseSlot() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        putWall.placeItemInSlot(assignedSlot.get(), skuCode, Quantity.of(1));
        assertEquals(2, putWall.getAvailableCapacity());

        putWall.releaseSlot(assignedSlot.get());

        PutWallSlot slot = putWall.getSlotById(assignedSlot.get());
        assertEquals(PutWallSlotStatus.FREE, slot.getStatus());
        assertTrue(slot.isFree());
        assertEquals(3, putWall.getAvailableCapacity());
        assertFalse(putWall.isFull());
    }

    @Test
    void shouldFindSlotForOrder() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU001"), Quantity.of(1));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        Optional<PutWallSlotId> foundSlot = putWall.findSlotForOrder(orderId);
        assertTrue(foundSlot.isPresent());
        assertEquals(assignedSlot.get(), foundSlot.get());
    }

    @Test
    void shouldGetReadyForPackSlots() {
        // Initially no slots ready for pack
        assertTrue(putWall.getReadyForPackSlots().isEmpty());

        // Assign and complete an order
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        putWall.placeItemInSlot(assignedSlot.get(), skuCode, Quantity.of(1));

        List<PutWallSlotId> readySlots = putWall.getReadyForPackSlots();
        assertEquals(1, readySlots.size());
        assertEquals(assignedSlot.get(), readySlots.get(0));
    }

    @Test
    void shouldThrowExceptionForNonExistentSlot() {
        PutWallSlotId nonExistentSlot = PutWallSlotId.of("INVALID");

        assertThrows(PutWallException.SlotNotFoundException.class,
            () -> putWall.getSlotById(nonExistentSlot));
    }

    @Test
    void shouldGenerateDomainEvents() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        // Initially no events
        assertTrue(putWall.getDomainEvents().isEmpty());

        // Assign order - should generate OrderAssignedToSlotEvent
        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        assertEquals(1, putWall.getDomainEvents().size());
        assertTrue(putWall.getDomainEvents().get(0) instanceof OrderAssignedToSlotEvent);

        // Place item - should generate ItemPlacedInSlotEvent and OrderConsolidatedInSlotEvent
        putWall.placeItemInSlot(assignedSlot.get(), skuCode, Quantity.of(1));

        assertEquals(3, putWall.getDomainEvents().size());
        assertTrue(putWall.getDomainEvents().get(1) instanceof ItemPlacedInSlotEvent);
        assertTrue(putWall.getDomainEvents().get(2) instanceof OrderConsolidatedInSlotEvent);

        // Release slot - should generate SlotReleasedEvent
        putWall.releaseSlot(assignedSlot.get());

        assertEquals(4, putWall.getDomainEvents().size());
        assertTrue(putWall.getDomainEvents().get(3) instanceof SlotReleasedEvent);
    }
}