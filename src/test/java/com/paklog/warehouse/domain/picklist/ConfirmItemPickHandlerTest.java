package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.PickListId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
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
@DisplayName("ConfirmItemPickHandler Tests")
class ConfirmItemPickHandlerTest {

    @Mock
    private PickListRepository pickListRepository;

    private ConfirmItemPickHandler confirmItemPickHandler;

    @BeforeEach
    void setUp() {
        confirmItemPickHandler = new ConfirmItemPickHandler(pickListRepository);
    }

    @Nested
    @DisplayName("Command Handling")
    class CommandHandling {

        @Test
        @DisplayName("Should handle confirm item pick command successfully")
        void shouldHandleConfirmItemPickCommandSuccessfully() {
            // Arrange
            PickListId pickListId = PickListId.generate();
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2");
            
            ConfirmItemPick command = new ConfirmItemPick(pickListId, skuCode, quantity, binLocation);
            PickList pickList = mock(PickList.class);

            when(pickListRepository.findById(pickListId)).thenReturn(pickList);

            // Act
            confirmItemPickHandler.handle(command);

            // Assert
            verify(pickListRepository).findById(pickListId);
            verify(pickList).pickItem(skuCode, quantity, binLocation);
            verify(pickListRepository).save(pickList);
        }

        @Test
        @DisplayName("Should throw exception when pick list not found")
        void shouldThrowExceptionWhenPickListNotFound() {
            // Arrange
            PickListId pickListId = PickListId.generate();
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2");
            
            ConfirmItemPick command = new ConfirmItemPick(pickListId, skuCode, quantity, binLocation);

            when(pickListRepository.findById(pickListId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> confirmItemPickHandler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pick list not found: " + pickListId);

            verify(pickListRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception from pick item method")
        void shouldPropagateExceptionFromPickItemMethod() {
            // Arrange
            PickListId pickListId = PickListId.generate();
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2");
            
            ConfirmItemPick command = new ConfirmItemPick(pickListId, skuCode, quantity, binLocation);
            PickList pickList = mock(PickList.class);
            RuntimeException exception = new RuntimeException("Pick item failed");

            when(pickListRepository.findById(pickListId)).thenReturn(pickList);
            doThrow(exception).when(pickList).pickItem(skuCode, quantity, binLocation);

            // Act & Assert
            assertThatThrownBy(() -> confirmItemPickHandler.handle(command))
                .isEqualTo(exception);

            verify(pickListRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle repository save exception")
        void shouldHandleRepositorySaveException() {
            // Arrange
            PickListId pickListId = PickListId.generate();
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2");
            
            ConfirmItemPick command = new ConfirmItemPick(pickListId, skuCode, quantity, binLocation);
            PickList pickList = mock(PickList.class);
            RuntimeException exception = new RuntimeException("Save failed");

            when(pickListRepository.findById(pickListId)).thenReturn(pickList);
            doThrow(exception).when(pickListRepository).save(pickList);

            // Act & Assert
            assertThatThrownBy(() -> confirmItemPickHandler.handle(command))
                .isEqualTo(exception);

            verify(pickList).pickItem(skuCode, quantity, binLocation);
        }
    }
}