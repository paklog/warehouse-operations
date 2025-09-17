package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListId;
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
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2-C3");
            
            PickList pickList = PickListTestHelper.createPickListWithInstruction(skuCode, quantity, binLocation);
            PickListId pickListId = pickList.getId();
            ConfirmItemPick command = new ConfirmItemPick(pickListId, skuCode, quantity, binLocation);

            when(pickListRepository.findById(pickListId)).thenReturn(pickList);

            // Act
            confirmItemPickHandler.handle(command);

            // Assert
            verify(pickListRepository).findById(pickListId);
            verify(pickListRepository).save(pickList);
        }

        @Test
        @DisplayName("Should throw exception when pick list not found")
        void shouldThrowExceptionWhenPickListNotFound() {
            // Arrange
            PickListId pickListId = PickListId.generate();
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2-C3");
            
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
            // Arrange - use a different SKU that doesn't exist in pick list to trigger exception
            SkuCode existingSku = SkuCode.of("SKU-001");
            SkuCode nonExistentSku = SkuCode.of("SKU-999");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2-C3");
            
            PickList pickList = PickListTestHelper.createPickListWithInstruction(existingSku, quantity, binLocation);
            PickListId pickListId = pickList.getId();
            
            // Command with non-existent SKU should trigger exception
            ConfirmItemPick command = new ConfirmItemPick(pickListId, nonExistentSku, quantity, binLocation);

            when(pickListRepository.findById(pickListId)).thenReturn(pickList);

            // Act & Assert
            assertThatThrownBy(() -> confirmItemPickHandler.handle(command))
                .isInstanceOf(RuntimeException.class);

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
            SkuCode skuCode = SkuCode.of("SKU-001");
            Quantity quantity = Quantity.of(2);
            BinLocation binLocation = BinLocation.of("A1-B2-C3");
            
            PickList pickList = PickListTestHelper.createPickListWithInstruction(skuCode, quantity, binLocation);
            PickListId pickListId = pickList.getId();
            ConfirmItemPick command = new ConfirmItemPick(pickListId, skuCode, quantity, binLocation);
            RuntimeException exception = new RuntimeException("Save failed");

            when(pickListRepository.findById(pickListId)).thenReturn(pickList);
            doThrow(exception).when(pickListRepository).save(pickList);

            // Act & Assert
            assertThatThrownBy(() -> confirmItemPickHandler.handle(command))
                .isEqualTo(exception);
        }
    }
}