package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.packaging.Package;
import com.paklog.warehouse.domain.packaging.PackageRepository;
import com.paklog.warehouse.domain.packaging.PackageStatus;
import com.paklog.warehouse.domain.packaging.PackedItem;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Packing Station Workflow Integration Tests")
@ActiveProfiles("test")
class PackingStationWorkflowIntegrationTest {

    private PackingStationService packingStationService;

    @Mock
    private PackageRepository packageRepository;
    
    @Mock
    private PickListRepository pickListRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        packingStationService = new PackingStationService(packageRepository, pickListRepository);
    }

    @Nested
    @DisplayName("Package Creation and Packing")
    class PackageCreationAndPacking {
        @Test
        @DisplayName("Should create a package successfully")
        void shouldCreatePackageSuccessfully() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            PickList pickList = new PickList(order.getOrderId());
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);

            // Act
            Package pkg = packingStationService.createPackage(order);

            // Assert
            assertThat(pkg).isNotNull();
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PENDING);
        }

        @Test
        @DisplayName("Should confirm package successfully")
        void shouldConfirmPackageSuccessfully() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            PickList pickList = new PickList(order.getOrderId());
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);

            // Act
            Package pkg = packingStationService.createPackage(order);
            packingStationService.confirmPackage(pkg);

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CONFIRMED);
        }
    }

    // Helper methods
    private FulfillmentOrder createFulfillmentOrder(String orderType, String skuCode, int quantity) {
        OrderId orderId = OrderId.generate();
        Address address = new Address();
        
        OrderItem item = new OrderItem(SkuCode.of(skuCode), Quantity.of(quantity));
        List<OrderItem> items = List.of(item);
        
        return new FulfillmentOrder(orderId, orderType, address, items);
    }
}