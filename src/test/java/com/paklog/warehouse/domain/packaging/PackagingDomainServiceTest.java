package com.paklog.warehouse.domain.packaging;

import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.OrderItem;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackagingDomainService Tests")
class PackagingDomainServiceTest {

    private PackagingDomainService packagingDomainService;

    @BeforeEach
    void setUp() {
        packagingDomainService = new PackagingDomainService();
    }

    @Nested
    @DisplayName("Order Packaging Validation")
    class OrderPackagingValidation {

        @Test
        @DisplayName("Should validate successful packaging")
        void shouldValidateSuccessfulPackaging() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);
            PickList pickList = mock(PickList.class);

            when(pickList.isComplete()).thenReturn(true);

            // Act
            boolean result = packagingDomainService.canPackageOrder(order, pickList);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject null order")
        void shouldRejectNullOrder() {
            // Arrange
            PickList pickList = mock(PickList.class);

            // Act
            boolean result = packagingDomainService.canPackageOrder(null, pickList);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null pick list")
        void shouldRejectNullPickList() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);

            // Act
            boolean result = packagingDomainService.canPackageOrder(order, null);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject incomplete pick list")
        void shouldRejectIncompletePickList() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);
            PickList pickList = mock(PickList.class);

            when(pickList.isComplete()).thenReturn(false);

            // Act
            boolean result = packagingDomainService.canPackageOrder(order, pickList);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Package Content Validation")
    class PackageContentValidation {

        @Test
        @DisplayName("Should validate valid package contents")
        void shouldValidateValidPackageContents() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> validItems = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), 2),
                new PackedItem(SkuCode.of("SKU-002"), 3)
            );
            Package pkg = new Package(packageId, validItems);

            // Act
            PackagingDomainService.PackageValidationResult result = packagingDomainService.validatePackageContents(pkg);

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.getViolations()).isEmpty();
        }

        @Test
        @DisplayName("Should reject package with invalid quantities")
        void shouldRejectPackageWithInvalidQuantities() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> invalidItems = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), -1),
                new PackedItem(SkuCode.of("SKU-002"), 0)
            );
            Package pkg = new Package(packageId, invalidItems);

            // Act
            PackagingDomainService.PackageValidationResult result = packagingDomainService.validatePackageContents(pkg);

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).hasSize(2);
            assertThat(result.getViolations()).allMatch(violation -> violation.contains("Invalid quantity"));
        }

        @Test
        @DisplayName("Should reject empty package")
        void shouldRejectEmptyPackage() {
            // Arrange - Need to use reflection or create a mock since constructor prevents empty packages
            Package pkg = mock(Package.class);
            when(pkg.getPackedItems()).thenReturn(Collections.emptyList());

            // Act
            PackagingDomainService.PackageValidationResult result = packagingDomainService.validatePackageContents(pkg);

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).contains("Package cannot be empty");
        }

        @Test
        @DisplayName("Should reject package exceeding item limit")
        void shouldRejectPackageExceedingItemLimit() {
            // Arrange - Create package with more than 50 items (the limit)
            Package pkg = mock(Package.class);
            List<PackedItem> manyItems = Collections.nCopies(51, 
                new PackedItem(SkuCode.of("SKU-001"), 1));
            when(pkg.getPackedItems()).thenReturn(manyItems);

            // Act
            PackagingDomainService.PackageValidationResult result = packagingDomainService.validatePackageContents(pkg);

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(violation -> 
                violation.contains("exceeds maximum items limit"));
        }

        @Test
        @DisplayName("Should detect duplicate SKUs")
        void shouldDetectDuplicateSkus() {
            // Arrange
            Package pkg = mock(Package.class);
            List<PackedItem> duplicateItems = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), 2),
                new PackedItem(SkuCode.of("SKU-001"), 3)
            );
            when(pkg.getPackedItems()).thenReturn(duplicateItems);

            // Act
            PackagingDomainService.PackageValidationResult result = packagingDomainService.validatePackageContents(pkg);

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).contains("Package contains duplicate SKU codes");
        }
    }

    @Nested
    @DisplayName("Shipment Readiness")
    class ShipmentReadiness {

        @Test
        @DisplayName("Should confirm package is ready for shipment")
        void shouldConfirmPackageIsReadyForShipment() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> validItems = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), 2)
            );
            Package pkg = new Package(packageId, validItems);
            pkg.confirmPacking();

            // Act
            boolean result = packagingDomainService.isReadyForShipment(pkg);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject unconfirmed package")
        void shouldRejectUnconfirmedPackage() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> validItems = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), 2)
            );
            Package pkg = new Package(packageId, validItems);

            // Act
            boolean result = packagingDomainService.isReadyForShipment(pkg);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject package with validation errors")
        void shouldRejectPackageWithValidationErrors() {
            // Arrange
            Package pkg = mock(Package.class);
            when(pkg.getStatus()).thenReturn(PackageStatus.CONFIRMED);
            when(pkg.getPackedItems()).thenReturn(Collections.emptyList());

            // Act
            boolean result = packagingDomainService.isReadyForShipment(pkg);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Packed Items Creation")
    class PackedItemsCreation {

        @Test
        @DisplayName("Should create packed items from order items")
        void shouldCreatePackedItemsFromOrderItems() {
            // Arrange
            List<OrderItem> orderItems = Arrays.asList(
                new OrderItem(SkuCode.of("SKU-001"), Quantity.of(2)),
                new OrderItem(SkuCode.of("SKU-002"), Quantity.of(3))
            );

            // Act
            List<PackedItem> result = packagingDomainService.createPackedItemsFromOrder(orderItems);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSkuCode()).isEqualTo(SkuCode.of("SKU-001"));
            assertThat(result.get(0).getQuantity()).isEqualTo(2);
            assertThat(result.get(1).getSkuCode()).isEqualTo(SkuCode.of("SKU-002"));
            assertThat(result.get(1).getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle empty order items list")
        void shouldHandleEmptyOrderItemsList() {
            // Act
            List<PackedItem> result = packagingDomainService.createPackedItemsFromOrder(Collections.emptyList());

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Packaging Metrics")
    class PackagingMetrics {

        @Test
        @DisplayName("Should calculate packaging metrics correctly")
        void shouldCalculatePackagingMetricsCorrectly() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> items = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), 4),
                new PackedItem(SkuCode.of("SKU-002"), 2)
            );
            Package pkg = new Package(packageId, items);

            // Act
            PackagingDomainService.PackagingMetrics metrics = packagingDomainService.calculatePackagingMetrics(pkg);

            // Assert
            assertThat(metrics.getTotalItems()).isEqualTo(6);
            assertThat(metrics.getUniqueSkus()).isEqualTo(2);
            assertThat(metrics.getPackingDensity()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should handle single item package")
        void shouldHandleSingleItemPackage() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> items = Arrays.asList(
                new PackedItem(SkuCode.of("SKU-001"), 5)
            );
            Package pkg = new Package(packageId, items);

            // Act
            PackagingDomainService.PackagingMetrics metrics = packagingDomainService.calculatePackagingMetrics(pkg);

            // Assert
            assertThat(metrics.getTotalItems()).isEqualTo(5);
            assertThat(metrics.getUniqueSkus()).isEqualTo(1);
            assertThat(metrics.getPackingDensity()).isEqualTo(5.0);
        }
    }
}