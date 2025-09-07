package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.WorkloadPlan;
import com.paklog.warehouse.domain.shared.PickListCreatedEvent;
import com.paklog.warehouse.domain.shared.PickListId;

import java.util.ArrayList;
import java.util.List;

public class WorkloadOrchestrator {
    private final IWorkloadReleaseStrategy releaseStrategy;
    private final List<DomainEvent> events = new ArrayList<>();

    public WorkloadOrchestrator(IWorkloadReleaseStrategy releaseStrategy) {
        this.releaseStrategy = releaseStrategy;
    }

    public void releaseOrder(FulfillmentOrder order) {
        WorkloadPlan plan = releaseStrategy.planWork(List.of(order));
        events.addAll(plan.getEvents());
        
        // Create a PickList event for the order
        PickListId pickListId = PickListId.generate();
        PickListCreatedEvent pickListEvent = new PickListCreatedEvent(pickListId, order.getOrderId());
        events.add(pickListEvent);
    }

    public List<DomainEvent> getDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>();
        copy.addAll(events);
        return copy;
    }
}