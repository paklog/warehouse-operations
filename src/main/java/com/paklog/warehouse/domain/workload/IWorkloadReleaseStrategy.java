package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.WorkloadPlan;

import java.util.List;

public interface IWorkloadReleaseStrategy {
    WorkloadPlan planWork(List<FulfillmentOrder> orders);
    List<DomainEvent> getDomainEvents();
}