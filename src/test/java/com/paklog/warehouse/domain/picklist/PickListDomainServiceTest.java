package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.PickInstruction;
import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.PickListId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PickListDomainService Tests")
class PickListDomainServiceTest {

    @Mock
    private PickRouteOptimizer routeOptimizer;

    private PickListDomainService pickListDomainService;

    @BeforeEach
    void setUp() {
        pickListDomainService = new PickListDomainService(routeOptimizer);
    }

    @Nested
    @DisplayName("Pick List Assignment Validation")
    class PickListAssignmentValidation {

        @Test
        @DisplayName("Should validate successful assignment")
        void shouldValidateSuccessfulAssignment() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickListId pickListId = PickListId.generate();
            String pickerId = "picker-001";

            when(pickList.getId()).thenReturn(pickListId);
            when(pickList.getStatus()).thenReturn(PickListStatus.PENDING);

            // Act
            boolean result = pickListDomainService.canAssignToPicker(pickList, pickerId);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject null pick list")
        void shouldRejectNullPickList() {
            // Act
            boolean result = pickListDomainService.canAssignToPicker(null, "picker-001");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null picker ID")
        void shouldRejectNullPickerId() {
            // Arrange
            PickList pickList = mock(PickList.class);

            // Act
            boolean result = pickListDomainService.canAssignToPicker(pickList, null);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject empty picker ID")
        void shouldRejectEmptyPickerId() {
            // Arrange
            PickList pickList = mock(PickList.class);

            // Act
            boolean result = pickListDomainService.canAssignToPicker(pickList, "   ");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject non-pending status")
        void shouldRejectNonPendingStatus() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickListId pickListId = PickListId.generate();

            when(pickList.getId()).thenReturn(pickListId);
            when(pickList.getStatus()).thenReturn(PickListStatus.ASSIGNED);

            // Act
            boolean result = pickListDomainService.canAssignToPicker(pickList, "picker-001");

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Route Optimization")
    class RouteOptimization {

        @Test
        @DisplayName("Should optimize pick route successfully")
        void shouldOptimizePickRouteSuccessfully() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickListId pickListId = PickListId.generate();
            OrderId orderId = OrderId.generate();
            
            List<PickInstruction> originalInstructions = Arrays.asList(
                mock(PickInstruction.class),
                mock(PickInstruction.class)
            );
            List<PickInstruction> optimizedInstructions = Arrays.asList(
                originalInstructions.get(1),
                originalInstructions.get(0)
            );

            when(pickList.getId()).thenReturn(pickListId);
            when(pickList.getOrderId()).thenReturn(orderId);
            when(pickList.getInstructions()).thenReturn(originalInstructions);
            when(routeOptimizer.optimize(originalInstructions)).thenReturn(optimizedInstructions);

            // Act
            PickList result = pickListDomainService.optimizePickRoute(pickList);

            // Assert
            assertThat(result).isNotNull();
            verify(routeOptimizer).optimize(originalInstructions);
        }

        @Test
        @DisplayName("Should handle empty instructions gracefully")
        void shouldHandleEmptyInstructionsGracefully() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickListId pickListId = PickListId.generate();

            when(pickList.getId()).thenReturn(pickListId);
            when(pickList.getInstructions()).thenReturn(Collections.emptyList());

            // Act
            PickList result = pickListDomainService.optimizePickRoute(pickList);

            // Assert
            assertThat(result).isEqualTo(pickList);
            verify(routeOptimizer, never()).optimize(any());
        }

        @Test
        @DisplayName("Should handle optimization failure gracefully")
        void shouldHandleOptimizationFailureGracefully() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickListId pickListId = PickListId.generate();
            List<PickInstruction> instructions = Arrays.asList(mock(PickInstruction.class));

            when(pickList.getId()).thenReturn(pickListId);
            when(pickList.getInstructions()).thenReturn(instructions);
            when(routeOptimizer.optimize(instructions)).thenThrow(new RuntimeException("Optimization failed"));

            // Act
            PickList result = pickListDomainService.optimizePickRoute(pickList);

            // Assert
            assertThat(result).isEqualTo(pickList);
        }
    }

    @Nested
    @DisplayName("Pick Validation")
    class PickValidation {

        @Test
        @DisplayName("Should validate successful pick")
        void shouldValidateSuccessfulPick() {
            // Arrange
            PickList pickList = mock(PickList.class);
            BinLocation binLocation = BinLocation.of("A1-B2");

            when(pickList.getPickerId()).thenReturn("picker-001");
            when(pickList.getStatus()).thenReturn(PickListStatus.ASSIGNED);

            // Act
            boolean result = pickListDomainService.isPickValid(pickList, "SKU-001", 5, binLocation);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject pick without assigned picker")
        void shouldRejectPickWithoutAssignedPicker() {
            // Arrange
            PickList pickList = mock(PickList.class);
            BinLocation binLocation = BinLocation.of("A1-B2");

            when(pickList.getPickerId()).thenReturn(null);

            // Act
            boolean result = pickListDomainService.isPickValid(pickList, "SKU-001", 5, binLocation);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject pick with non-assigned status")
        void shouldRejectPickWithNonAssignedStatus() {
            // Arrange
            PickList pickList = mock(PickList.class);
            BinLocation binLocation = BinLocation.of("A1-B2");

            when(pickList.getPickerId()).thenReturn("picker-001");
            when(pickList.getStatus()).thenReturn(PickListStatus.PENDING);

            // Act
            boolean result = pickListDomainService.isPickValid(pickList, "SKU-001", 5, binLocation);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject pick with invalid quantity")
        void shouldRejectPickWithInvalidQuantity() {
            // Arrange
            PickList pickList = mock(PickList.class);
            BinLocation binLocation = BinLocation.of("A1-B2");

            when(pickList.getPickerId()).thenReturn("picker-001");
            when(pickList.getStatus()).thenReturn(PickListStatus.ASSIGNED);

            // Act
            boolean result = pickListDomainService.isPickValid(pickList, "SKU-001", 0, binLocation);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject pick with invalid bin location")
        void shouldRejectPickWithInvalidBinLocation() {
            // Arrange
            PickList pickList = mock(PickList.class);

            when(pickList.getPickerId()).thenReturn("picker-001");
            when(pickList.getStatus()).thenReturn(PickListStatus.ASSIGNED);

            // Act
            boolean result = pickListDomainService.isPickValid(pickList, "SKU-001", 5, null);

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Progress Calculation")
    class ProgressCalculation {

        @Test
        @DisplayName("Should calculate progress correctly")
        void shouldCalculateProgressCorrectly() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickInstruction completedInstruction = mock(PickInstruction.class);
            PickInstruction pendingInstruction = mock(PickInstruction.class);

            when(completedInstruction.isCompleted()).thenReturn(true);
            when(pendingInstruction.isCompleted()).thenReturn(false);
            when(pickList.getInstructions()).thenReturn(Arrays.asList(
                completedInstruction, pendingInstruction
            ));

            // Act
            PickListDomainService.PickListProgress progress = pickListDomainService.calculateProgress(pickList);

            // Assert
            assertThat(progress.getCompleted()).isEqualTo(1);
            assertThat(progress.getTotal()).isEqualTo(2);
            assertThat(progress.getPercentage()).isEqualTo(50.0);
            assertThat(progress.isComplete()).isFalse();
        }

        @Test
        @DisplayName("Should handle empty instructions")
        void shouldHandleEmptyInstructions() {
            // Arrange
            PickList pickList = mock(PickList.class);
            when(pickList.getInstructions()).thenReturn(Collections.emptyList());

            // Act
            PickListDomainService.PickListProgress progress = pickListDomainService.calculateProgress(pickList);

            // Assert
            assertThat(progress.getCompleted()).isEqualTo(0);
            assertThat(progress.getTotal()).isEqualTo(0);
            assertThat(progress.getPercentage()).isEqualTo(100.0);
            assertThat(progress.isComplete()).isFalse();
        }

        @Test
        @DisplayName("Should calculate 100% completion correctly")
        void shouldCalculate100PercentCompletionCorrectly() {
            // Arrange
            PickList pickList = mock(PickList.class);
            PickInstruction completedInstruction1 = mock(PickInstruction.class);
            PickInstruction completedInstruction2 = mock(PickInstruction.class);

            when(completedInstruction1.isCompleted()).thenReturn(true);
            when(completedInstruction2.isCompleted()).thenReturn(true);
            when(pickList.getInstructions()).thenReturn(Arrays.asList(
                completedInstruction1, completedInstruction2
            ));

            // Act
            PickListDomainService.PickListProgress progress = pickListDomainService.calculateProgress(pickList);

            // Assert
            assertThat(progress.getCompleted()).isEqualTo(2);
            assertThat(progress.getTotal()).isEqualTo(2);
            assertThat(progress.getPercentage()).isEqualTo(100.0);
            assertThat(progress.isComplete()).isTrue();
        }
    }
}