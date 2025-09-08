package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.packaging.Package;
import com.paklog.warehouse.domain.packaging.PackageRepository;
import com.paklog.warehouse.domain.packaging.PackageStatus;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.picklist.PickList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackingStationService Tests")
class PackingStationServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private PickListRepository pickListRepository;

    private PackingStationService packingStationService;

    @BeforeEach
    void setUp() {
        packingStationService = new PackingStationService(packageRepository, pickListRepository);
    }

    @Nested
    @DisplayName("Package Creation")
    class PackageCreation {

        @Test
        @DisplayName("Should create package successfully when pick list exists")
        void shouldCreatePackageSuccessfullyWhenPickListExists() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);
            OrderId orderId = OrderId.generate();
            PickList pickList = mock(PickList.class);

            when(order.getOrderId()).thenReturn(orderId);
            when(pickListRepository.findByOrderId(orderId)).thenReturn(pickList);

            // Act
            Package result = packingStationService.createPackage(order);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PackageStatus.PENDING);
            verify(packageRepository).save(result);
            verify(pickListRepository).findByOrderId(orderId);
        }

        @Test
        @DisplayName("Should throw exception when pick list does not exist")
        void shouldThrowExceptionWhenPickListDoesNotExist() {
            // Arrange
            FulfillmentOrder order = mock(FulfillmentOrder.class);
            OrderId orderId = OrderId.generate();

            when(order.getOrderId()).thenReturn(orderId);
            when(pickListRepository.findByOrderId(orderId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> packingStationService.createPackage(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No pick list found for order: " + orderId);

            verify(packageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Package Confirmation")
    class PackageConfirmation {

        @Test
        @DisplayName("Should confirm package successfully")
        void shouldConfirmPackageSuccessfully() {
            // Arrange
            Package pkg = mock(Package.class);

            // Act
            packingStationService.confirmPackage(pkg);

            // Assert
            verify(pkg).confirmPacking();
            verify(packageRepository).save(pkg);
        }

        @Test
        @DisplayName("Should handle exception during package confirmation")
        void shouldHandleExceptionDuringPackageConfirmation() {
            // Arrange
            Package pkg = mock(Package.class);
            RuntimeException exception = new RuntimeException("Confirmation failed");
            doThrow(exception).when(pkg).confirmPacking();

            // Act & Assert
            assertThatThrownBy(() -> packingStationService.confirmPackage(pkg))
                .isEqualTo(exception);

            verify(packageRepository, never()).save(pkg);
        }
    }

    @Nested
    @DisplayName("Package Retrieval")
    class PackageRetrieval {

        @Test
        @DisplayName("Should find package by order ID successfully")
        void shouldFindPackageByOrderIdSuccessfully() {
            // Arrange
            OrderId orderId = OrderId.generate();
            Package expectedPackage = mock(Package.class);

            when(packageRepository.findByOrderId(orderId)).thenReturn(expectedPackage);

            // Act
            Package result = packingStationService.findPackageByOrderId(orderId);

            // Assert
            assertThat(result).isEqualTo(expectedPackage);
            verify(packageRepository).findByOrderId(orderId);
        }

        @Test
        @DisplayName("Should return null when package not found")
        void shouldReturnNullWhenPackageNotFound() {
            // Arrange
            OrderId orderId = OrderId.generate();

            when(packageRepository.findByOrderId(orderId)).thenReturn(null);

            // Act
            Package result = packingStationService.findPackageByOrderId(orderId);

            // Assert
            assertThat(result).isNull();
            verify(packageRepository).findByOrderId(orderId);
        }
    }
}