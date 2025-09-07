package com.paklog.warehouse.config;

import com.paklog.warehouse.application.service.DefaultPickListQueryService;
import com.paklog.warehouse.application.service.PackingStationService;
import com.paklog.warehouse.application.service.PickListQueryService;
import com.paklog.warehouse.domain.packing.PackageRepository;
import com.paklog.warehouse.domain.packing.PackagingDomainService;
import com.paklog.warehouse.domain.picklist.ConfirmItemPickHandler;
import com.paklog.warehouse.domain.picklist.PickListDomainService;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.picklist.PickRouteOptimizer;
import com.paklog.warehouse.domain.workload.ContinuousStrategy;
import com.paklog.warehouse.domain.workload.IWorkloadReleaseStrategy;
import com.paklog.warehouse.domain.workload.WorkloadOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PickListQueryService pickListQueryService(PickListRepository pickListRepository) {
        return new DefaultPickListQueryService(pickListRepository);
    }

    @Bean
    public PackingStationService packingStationService(
            PackageRepository packageRepository,
            PickListRepository pickListRepository) {
        return new PackingStationService(packageRepository, pickListRepository);
    }

    @Bean
    public ConfirmItemPickHandler confirmItemPickHandler(PickListRepository pickListRepository) {
        return new ConfirmItemPickHandler(pickListRepository);
    }

    @Bean
    public IWorkloadReleaseStrategy workloadReleaseStrategy() {
        return new ContinuousStrategy();
    }

    @Bean
    public WorkloadOrchestrator workloadOrchestrator(IWorkloadReleaseStrategy workloadReleaseStrategy) {
        return new WorkloadOrchestrator(workloadReleaseStrategy);
    }

    @Bean
    public PickListDomainService pickListDomainService(PickRouteOptimizer pickRouteOptimizer) {
        return new PickListDomainService(pickRouteOptimizer);
    }

    @Bean
    public PackagingDomainService packagingDomainService() {
        return new PackagingDomainService();
    }
}