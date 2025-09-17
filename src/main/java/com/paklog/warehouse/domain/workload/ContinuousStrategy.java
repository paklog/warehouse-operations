package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.picklist.PickListCreatedEvent;
import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.WorkloadPlan;

import java.util.ArrayList;
import java.util.List;

public class ContinuousStrategy implements IWorkloadReleaseStrategy {
    private final List<DomainEvent> events = new ArrayList<>();

    @Override
    public WorkloadPlan planWork(List<FulfillmentOrder> orders) {
        List<DomainEvent> planEvents = new ArrayList<>();
        
        // Create a PickListCreatedEvent for each fulfillment order
        for (FulfillmentOrder order : orders) {
            PickListCreatedEvent event = new PickListCreatedEvent(
                PickListId.generate(), 
                order.getOrderId()
            );
            planEvents.add(event);
        }
        
        return new WorkloadPlan(planEvents);
    }

    @Override
    public List<DomainEvent> getDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>();
        copy.addAll(events);
        return copy;
    }
}