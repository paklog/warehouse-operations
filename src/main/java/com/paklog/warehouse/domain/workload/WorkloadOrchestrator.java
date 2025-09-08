package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.WorkloadPlan;
import com.paklog.warehouse.domain.picklist.PickListCreatedEvent;
import com.paklog.warehouse.domain.picklist.PickListId;

import java.util.ArrayList;
import java.util.List;

public class WorkloadOrchestrator {
    private final IWorkloadReleaseStrategy releaseStrategy;
    private final List<DomainEvent> events = new ArrayList<>();

    public WorkloadOrchestrator(IWorkloadReleaseStrategy releaseStrategy) {
        this.releaseStrategy = releaseStrategy;
    }

    public void releaseOrder(FulfillmentOrder order) {
        releaseOrders(List.of(order));
    }
    
    public void releaseOrders(List<FulfillmentOrder> orders) {
        WorkloadPlan plan = releaseStrategy.planWork(orders);
        events.addAll(plan.getEvents());
        
        // Note: PickList events are now created by the strategy itself
        // This allows for proper integration between Wave and PickList creation
    }

    public List<DomainEvent> getDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>();
        copy.addAll(events);
        return copy;
    }
}