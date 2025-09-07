package com.paklog.warehouse.domain.packing;

import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.SkuCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Package Domain Tests")
class PackageTest {

    @Nested
    @DisplayName("Package Creation")
    class PackageCreation {
        @Test
        @DisplayName("Should create a valid package")
        void shouldCreateValidPackage() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));

            // Act
            Package pkg = new Package(packageId, packedItems);

            // Assert
            assertThat(pkg).isNotNull();
            assertThat(pkg.getPackageId()).isEqualTo(packageId);
            assertThat(pkg.getPackedItems()).hasSize(1);
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when creating package with null packageId")
        void shouldThrowExceptionForNullPackageId() {
            // Arrange
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));

            // Act & Assert
            assertThatThrownBy(() -> new Package(null, packedItems))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Package ID cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when creating package with empty packed items")
        void shouldThrowExceptionForEmptyPackedItems() {
            // Arrange
            UUID packageId = UUID.randomUUID();

            // Act & Assert
            assertThatThrownBy(() -> new Package(packageId, new ArrayList<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Package must contain at least one packed item");
        }

        @Test
        @DisplayName("Should create package with fulfillment order and pick list")
        void shouldCreatePackageWithOrderAndPickList() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);
            PickList pickList = mock(PickList.class);

            // Act
            Package pkg = new Package(order, pickList);

            // Assert
            assertThat(pkg).isNotNull();
            assertThat(pkg.getPackageId()).isNotNull();
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.PENDING);
            assertThat(pkg.getPackedItems()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception for null fulfillment order")
        void shouldThrowExceptionForNullOrder() {
            // Arrange
            PickList pickList = mock(PickList.class);

            // Act & Assert
            assertThatThrownBy(() -> new Package(null, pickList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fulfillment order cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for null pick list")
        void shouldThrowExceptionForNullPickList() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);

            // Act & Assert
            assertThatThrownBy(() -> new Package(order, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PickList cannot be null");
        }
    }

    @Nested
    @DisplayName("Packed Items Management")
    class PackedItemsManagement {
        @Test
        @DisplayName("Should add packed items to package")
        void shouldAddPackedItems() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> initialItems = new ArrayList<>();
            initialItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            Package pkg = new Package(packageId, initialItems);

            // Act
            PackedItem newItem = new PackedItem(SkuCode.of("SKU456"), 1);
            pkg.addPackedItem(newItem);

            // Assert
            assertThat(pkg.getPackedItems()).hasSize(2);
            assertThat(pkg.getPackedItems()).contains(newItem);
        }

        @Test
        @DisplayName("Should not add duplicate packed items")
        void shouldNotAddDuplicatePackedItems() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            SkuCode skuCode = SkuCode.of("SKU123");
            List<PackedItem> initialItems = new ArrayList<>();
            initialItems.add(new PackedItem(skuCode, 2));
            Package pkg = new Package(packageId, initialItems);

            // Act & Assert
            assertThatThrownBy(() -> pkg.addPackedItem(new PackedItem(skuCode, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Packed item with this SKU already exists in the package");
        }

        @Test
        @DisplayName("Should calculate total quantity of packed items")
        void shouldCalculateTotalQuantity() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            packedItems.add(new PackedItem(SkuCode.of("SKU456"), 3));
            Package pkg = new Package(packageId, packedItems);

            // Act & Assert
            assertThat(pkg.getTotalQuantity()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Package State Management")
    class PackageStateManagement {

        @Test
        @DisplayName("Should confirm packing successfully")
        void shouldConfirmPackingSuccessfully() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            Package pkg = new Package(packageId, packedItems);

            // Act
            pkg.confirmPacking();

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should throw exception when confirming already confirmed package")
        void shouldThrowExceptionWhenConfirmingAlreadyConfirmedPackage() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            Package pkg = new Package(packageId, packedItems);
            pkg.confirmPacking(); // First confirmation

            // Act & Assert
            assertThatThrownBy(() -> pkg.confirmPacking())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Package is already confirmed");
        }
    }

    @Nested
    @DisplayName("Package Equality and Hash Code")
    class PackageEqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when same package ID and contents")
        void shouldBeEqualWhenSamePackageIdAndContents() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            
            Package pkg1 = new Package(packageId, new ArrayList<>(packedItems));
            Package pkg2 = new Package(packageId, new ArrayList<>(packedItems));

            // Assert
            assertThat(pkg1).isEqualTo(pkg2);
            assertThat(pkg1.hashCode()).isEqualTo(pkg2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different package IDs")
        void shouldNotBeEqualWhenDifferentPackageIds() {
            // Arrange
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            
            Package pkg1 = new Package(UUID.randomUUID(), packedItems);
            Package pkg2 = new Package(UUID.randomUUID(), packedItems);

            // Assert
            assertThat(pkg1).isNotEqualTo(pkg2);
        }

        @Test
        @DisplayName("Should handle null and different class comparisons")
        void shouldHandleNullAndDifferentClassComparisons() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            Package pkg = new Package(packageId, packedItems);

            // Assert
            assertThat(pkg).isNotEqualTo(null);
            assertThat(pkg).isNotEqualTo("Not a package");
        }
    }

    @Nested
    @DisplayName("Package String Representation")
    class PackageStringRepresentation {

        @Test
        @DisplayName("Should provide meaningful string representation")
        void shouldProvideMeaningfulStringRepresentation() {
            // Arrange
            UUID packageId = UUID.randomUUID();
            List<PackedItem> packedItems = new ArrayList<>();
            packedItems.add(new PackedItem(SkuCode.of("SKU123"), 2));
            Package pkg = new Package(packageId, packedItems);

            // Act
            String result = pkg.toString();

            // Assert
            assertThat(result)
                .contains("Package{")
                .contains(packageId.toString())
                .contains("PENDING");
        }
    }
}