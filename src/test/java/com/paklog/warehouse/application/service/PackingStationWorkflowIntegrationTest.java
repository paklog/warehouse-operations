package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.packing.Package;
import com.paklog.warehouse.domain.packing.PackageStatus;
import com.paklog.warehouse.domain.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Packing Station Workflow Integration Tests")
class PackingStationWorkflowIntegrationTest {

    private PackingStationService packingStationService;

    @Mock
    private PickListRepository pickListRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        packingStationService = new PackingStationService(pickListRepository);
    }

    @Nested
    @DisplayName("Package Creation and Packing")
    class PackageCreationAndPacking {
        @Test
        @DisplayName("Should create and pack a package successfully")
        void shouldCreateAndPackPackageSuccessfully() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            PickList pickList = new PickList(order.getOrderId());
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);

            // Act
            Package pkg = packingStationService.createPackage(order);
            pkg.addPackedItem(new PackedItem(SkuCode.of("SKU-001"), 2));
            packingStationService.confirmPackage(pkg);

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CONFIRMED);
            assertThat(pkg.getPackedItems()).hasSize(1);
            assertThat(pkg.getTotalQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw exception when creating package with mismatched items")
        void shouldThrowExceptionForMismatchedItems() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            PickList pickList = new PickList(order.getOrderId());
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);

            // Act & Assert
            Package pkg = packingStationService.createPackage(order);
            
            assertThatThrownBy(() -> {
                pkg.addPackedItem(new PackedItem(SkuCode.of("SKU-002"), 1));
                packingStationService.confirmPackage(pkg);
            }).isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("Packed items do not match order items");
        }
    }

    @Nested
    @DisplayName("Order Validation")
    class OrderValidation {
        @Test
        @DisplayName("Should validate package against order items")
        void shouldValidatePackageAgainstOrderItems() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            PickList pickList = new PickList(order.getOrderId());
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);

            // Act
            Package pkg = packingStationService.createPackage(order);
            pkg.addPackedItem(new PackedItem(SkuCode.of("SKU-001"), 2));

            // Assert
            assertThat(packingStationService.validatePackedItems(pkg, order)).isTrue();
        }

        @Test
        @DisplayName("Should fail validation for incorrect quantity")
        void shouldFailValidationForIncorrectQuantity() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            PickList pickList = new PickList(order.getOrderId());
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);

            // Act
            Package pkg = packingStationService.createPackage(order);
            pkg.addPackedItem(new PackedItem(SkuCode.of("SKU-001"), 1));

            // Assert
            assertThat(packingStationService.validatePackedItems(pkg, order)).isFalse();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        @Test
        @DisplayName("Should throw exception for null order")
        void shouldThrowExceptionForNullOrder() {
            // Act & Assert
            assertThatThrownBy(() -> packingStationService.createPackage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for package without pick list")
        void shouldThrowExceptionForPackageWithoutPickList() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder("standard", "SKU-001", 2);
            when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> packingStationService.createPackage(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No pick list found for order");
        }
    }

    // Helper method to create fulfillment orders
    private FulfillmentOrder createFulfillmentOrder(String orderType, String skuCode, int quantity) {
        return new FulfillmentOrder(
            OrderId.generate(), 
            orderType, 
            new Address(), 
            List.of(new OrderItem(SkuCode.of(skuCode), Quantity.of(quantity)))
        );
    }
}