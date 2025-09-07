package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.WorkloadPlan;

import java.util.ArrayList;
import java.util.List;

public class ContinuousStrategy implements IWorkloadReleaseStrategy {
    private final List<DomainEvent> events = new ArrayList<>();

    @Override
    public WorkloadPlan planWork(List<FulfillmentOrder> orders) {
        // Placeholder implementation
        return new WorkloadPlan(events);
    }

    @Override
    public List<DomainEvent> getDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>();
        copy.addAll(events);
        return copy;
    }
}