package com.paklog.warehouse.config;

import com.paklog.warehouse.application.service.DefaultPickListQueryService;
import com.paklog.warehouse.application.service.PackingStationService;
import com.paklog.warehouse.application.service.PickListQueryService;
import com.paklog.warehouse.domain.packaging.PackageRepository;
import com.paklog.warehouse.domain.packaging.PackagingDomainService;
import com.paklog.warehouse.domain.picklist.ConfirmItemPickHandler;
import com.paklog.warehouse.domain.picklist.PickListDomainService;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.picklist.PickRouteOptimizer;
import com.paklog.warehouse.domain.workload.ContinuousStrategy;
import com.paklog.warehouse.domain.workload.IWorkloadReleaseStrategy;
import com.paklog.warehouse.domain.workload.WorkloadOrchestrator;
import com.paklog.warehouse.domain.quality.*;
import com.paklog.warehouse.domain.location.*;
import com.paklog.warehouse.domain.licenseplate.*;
import com.paklog.warehouse.domain.work.WorkRepository;
import com.paklog.warehouse.domain.work.WorkTemplateRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PickListQueryService pickListQueryService(PickListRepository pickListRepository) {
        return new DefaultPickListQueryService(pickListRepository, java.util.Collections.emptyList());
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

    // Quality Domain Services
    @Bean
    public QualityInspectionService qualityInspectionService(QualityInspectionRepository inspectionRepository) {
        return new QualityInspectionService(inspectionRepository);
    }

    @Bean
    public QualityHoldService qualityHoldService(QualityHoldRepository holdRepository) {
        return new QualityHoldService(holdRepository);
    }

    @Bean
    public QualityWorkIntegrationService qualityWorkIntegrationService(
            QualityInspectionRepository inspectionRepository,
            WorkRepository workRepository) {
        return new QualityWorkIntegrationService(inspectionRepository, workRepository);
    }

    // Location Domain Services
    @Bean
    public LocationDirectiveService locationDirectiveService(LocationDirectiveRepository directiveRepository) {
        return new LocationDirectiveService(directiveRepository);
    }

    // License Plate Domain Services
    @Bean
    public DefaultLicensePlateGenerator licensePlateGenerator() {
        return new DefaultLicensePlateGenerator("LP", 1);
    }

    @Bean
    public LicensePlateService licensePlateService(
            LicensePlateRepository licensePlateRepository,
            LicensePlateGenerator licensePlateGenerator) {
        return new LicensePlateService(licensePlateRepository, licensePlateGenerator);
    }
}