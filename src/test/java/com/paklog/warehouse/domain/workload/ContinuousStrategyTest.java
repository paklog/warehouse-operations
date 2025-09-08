package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.picklist.PickListCreatedEvent;
import com.paklog.warehouse.domain.shared.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContinuousStrategyTest {
    @Test
    void shouldCreatePickListsForFulfillmentOrders() {
        ContinuousStrategy strategy = new ContinuousStrategy();
        FulfillmentOrder order1 = new FulfillmentOrder(OrderId.generate(), "standard", new Address(), List.of(new OrderItem(SkuCode.of("SKU-001"), Quantity.of(2))));
        FulfillmentOrder order2 = new FulfillmentOrder(OrderId.generate(), "express", new Address(), List.of(new OrderItem(SkuCode.of("SKU-002"), Quantity.of(1))));

        WorkloadPlan plan = strategy.planWork(List.of(order1, order2));
        List<DomainEvent> events = plan.getEvents();

        assertNotNull(events);
        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof PickListCreatedEvent);
        assertTrue(events.get(1) instanceof PickListCreatedEvent);
    }
}