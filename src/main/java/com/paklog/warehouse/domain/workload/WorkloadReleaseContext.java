package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.WorkloadPlan;

import java.util.List;

public class WorkloadReleaseContext {
    private final IWorkloadReleaseStrategy strategy;

    public WorkloadReleaseContext(IWorkloadReleaseStrategy strategy) {
        this.strategy = strategy;
    }

    public WorkloadPlan planWork(List<FulfillmentOrder> orders) {
        return strategy.planWork(orders);
    }
}