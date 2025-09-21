package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PutWallSlotTest {

    private PutWallSlot slot;
    private PutWallSlotId slotId;

    @BeforeEach
    void setUp() {
        slotId = PutWallSlotId.of("A1");
        slot = new PutWallSlot(slotId);
    }

    @Test
    void shouldCreateFreeSlot() {
        assertEquals(slotId, slot.getSlotId());
        assertEquals(PutWallSlotStatus.FREE, slot.getStatus());
        assertTrue(slot.isFree());
        assertFalse(slot.isReadyForPack());
        assertNull(slot.getAssignedOrderId());
        assertTrue(slot.getItemsRequired().isEmpty());
        assertTrue(slot.getItemsPlaced().isEmpty());
    }

    @Test
    void shouldAssignOrderToSlot() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(
            SkuCode.of("SKU001"), Quantity.of(2),
            SkuCode.of("SKU002"), Quantity.of(1)
        );

        slot.assignToOrder(orderId, requiredItems);

        assertEquals(orderId, slot.getAssignedOrderId());
        assertEquals(PutWallSlotStatus.IN_PROGRESS, slot.getStatus());
        assertFalse(slot.isFree());
        assertEquals(2, slot.getItemsRequired().size());
        assertEquals(Quantity.of(2), slot.getItemsRequired().get(SkuCode.of("SKU001")));
        assertEquals(Quantity.of(1), slot.getItemsRequired().get(SkuCode.of("SKU002")));
    }

    @Test
    void shouldThrowExceptionWhenAssigningToNonFreeSlot() {
        OrderId orderId1 = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems1 = Map.of(SkuCode.of("SKU001"), Quantity.of(1));

        slot.assignToOrder(orderId1, requiredItems1);

        OrderId orderId2 = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems2 = Map.of(SkuCode.of("SKU002"), Quantity.of(1));

        assertThrows(IllegalStateException.class,
            () -> slot.assignToOrder(orderId2, requiredItems2));
    }

    @Test
    void shouldPlaceValidItems() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(3));

        slot.assignToOrder(orderId, requiredItems);

        slot.placeItem(skuCode, Quantity.of(2));

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

        slot.assignToOrder(orderId, requiredItems);

        slot.placeItem(skuCode1, Quantity.of(2));
        assertFalse(slot.isOrderComplete());

        slot.placeItem(skuCode2, Quantity.of(1));
        assertTrue(slot.isOrderComplete());
        assertEquals(PutWallSlotStatus.COMPLETE, slot.getStatus());
    }

    @Test
    void shouldAllowPartialItemPlacement() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(5));

        slot.assignToOrder(orderId, requiredItems);

        slot.placeItem(skuCode, Quantity.of(2));
        assertEquals(Quantity.of(2), slot.getItemsPlaced().get(skuCode));

        slot.placeItem(skuCode, Quantity.of(3));
        assertEquals(Quantity.of(5), slot.getItemsPlaced().get(skuCode));
        assertTrue(slot.isOrderComplete());
    }

    @Test
    void shouldThrowExceptionWhenPlacingTooManyItems() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(2));

        slot.assignToOrder(orderId, requiredItems);

        assertThrows(IllegalArgumentException.class,
            () -> slot.placeItem(skuCode, Quantity.of(3)));
    }

    @Test
    void shouldThrowExceptionWhenPlacingUnrequiredSku() {
        OrderId orderId = OrderId.generate();
        SkuCode requiredSku = SkuCode.of("SKU001");
        SkuCode unrequiredSku = SkuCode.of("SKU999");
        Map<SkuCode, Quantity> requiredItems = Map.of(requiredSku, Quantity.of(1));

        slot.assignToOrder(orderId, requiredItems);

        assertThrows(IllegalArgumentException.class,
            () -> slot.placeItem(unrequiredSku, Quantity.of(1)));
    }

    @Test
    void shouldThrowExceptionWhenPlacingItemsInNonProgressSlot() {
        SkuCode skuCode = SkuCode.of("SKU001");

        assertThrows(IllegalStateException.class,
            () -> slot.placeItem(skuCode, Quantity.of(1)));
    }

    @Test
    void shouldMarkReadyForPack() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        slot.assignToOrder(orderId, requiredItems);
        slot.placeItem(skuCode, Quantity.of(1));

        assertEquals(PutWallSlotStatus.COMPLETE, slot.getStatus());

        slot.markReadyForPack();

        assertEquals(PutWallSlotStatus.READY_FOR_PACK, slot.getStatus());
        assertTrue(slot.isReadyForPack());
    }

    @Test
    void shouldThrowExceptionWhenMarkingIncompleteSlotReadyForPack() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        slot.assignToOrder(orderId, requiredItems);

        assertThrows(IllegalStateException.class, slot::markReadyForPack);
    }

    @Test
    void shouldReleaseSlot() {
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        slot.assignToOrder(orderId, requiredItems);
        slot.placeItem(skuCode, Quantity.of(1));
        slot.markReadyForPack();

        slot.release();

        assertEquals(PutWallSlotStatus.FREE, slot.getStatus());
        assertTrue(slot.isFree());
        assertNull(slot.getAssignedOrderId());
        assertTrue(slot.getItemsRequired().isEmpty());
        assertTrue(slot.getItemsPlaced().isEmpty());
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {
        PutWallSlot slot1 = new PutWallSlot(PutWallSlotId.of("A1"));
        PutWallSlot slot2 = new PutWallSlot(PutWallSlotId.of("A1"));
        PutWallSlot slot3 = new PutWallSlot(PutWallSlotId.of("A2"));

        assertEquals(slot1, slot2);
        assertNotEquals(slot1, slot3);
        assertEquals(slot1.hashCode(), slot2.hashCode());
        assertNotEquals(slot1.hashCode(), slot3.hashCode());
    }
}