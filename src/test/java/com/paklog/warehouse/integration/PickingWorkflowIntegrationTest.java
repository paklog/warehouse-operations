package com.paklog.warehouse.integration;

import com.paklog.warehouse.domain.picklist.ConfirmItemPick;
import com.paklog.warehouse.domain.picklist.PickList;
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

@DisplayName("Picking Workflow Integration Tests")
class PickingWorkflowIntegrationTest {

    @Mock
    private PickListRepository pickListRepository;

    @Mock
    private BinLocationService binLocationService;

    private PickList pickList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create a sample order with multiple items
        FulfillmentOrder order = createFulfillmentOrder(
            "standard", 
            new String[]{"SKU-001", "SKU-002"}, 
            new int[]{2, 3}
        );
        
        pickList = new PickList(order.getOrderId());
        when(pickListRepository.findByOrderId(order.getOrderId())).thenReturn(pickList);
    }

    @Nested
    @DisplayName("Pick List Creation")
    class PickListCreation {
        @Test
        @DisplayName("Should create pick list from fulfillment order")
        void shouldCreatePickListFromFulfillmentOrder() {
            // Arrange
            FulfillmentOrder order = createFulfillmentOrder(
                "express", 
                new String[]{"SKU-003"}, 
                new int[]{1}
            );

            // Act
            PickList createdPickList = new PickList(order.getOrderId());

            // Assert
            assertThat(createdPickList).isNotNull();
            assertThat(createdPickList.getStatus()).isEqualTo(PickListStatus.PENDING);
            assertThat(createdPickList.getInstructions()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception for null order ID")
        void shouldThrowExceptionForNullOrderId() {
            // Act & Assert
            assertThatThrownBy(() -> new PickList(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order ID cannot be null");
        }
    }

    @Nested
    @DisplayName("Item Picking Workflow")
    class ItemPickingWorkflow {
        @Test
        @DisplayName("Should confirm item pick successfully")
        void shouldConfirmItemPickSuccessfully() {
            // Arrange
            SkuCode skuCode = SkuCode.of("SKU-001");
            BinLocation binLocation = new BinLocation("A01");
            Quantity quantity = Quantity.of(2);
            String pickerId = "PICKER-001";

            // Mock bin location availability
            when(binLocationService.isLocationAvailable(binLocation)).thenReturn(true);

            // Act
            ConfirmItemPick confirmItemPick = new ConfirmItemPick(
                pickList.getId(), 
                skuCode, 
                quantity, 
                binLocation, 
                pickerId
            );

            // Assert
            assertThat(confirmItemPick.getPickListId()).isEqualTo(pickList.getId());
            assertThat(confirmItemPick.getSkuCode()).isEqualTo(skuCode);
            assertThat(confirmItemPick.getQuantity()).isEqualTo(quantity);
            assertThat(confirmItemPick.getBinLocation()).isEqualTo(binLocation);
            assertThat(confirmItemPick.getPickerId()).isEqualTo(pickerId);
        }

        @Test
        @DisplayName("Should prevent picking from unavailable bin location")
        void shouldPreventPickingFromUnavailableBinLocation() {
            // Arrange
            SkuCode skuCode = SkuCode.of("SKU-001");
            BinLocation binLocation = new BinLocation("B02");
            Quantity quantity = Quantity.of(2);
            String pickerId = "PICKER-001";

            // Mock bin location unavailability
            when(binLocationService.isLocationAvailable(binLocation)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> new ConfirmItemPick(
                pickList.getId(), 
                skuCode, 
                quantity, 
                binLocation, 
                pickerId
            )).isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("Bin location is not available");
        }
    }

    @Nested
    @DisplayName("Pick List Status Management")
    class PickListStatusManagement {
        @Test
        @DisplayName("Should update pick list status when all items are picked")
        void shouldUpdatePickListStatusWhenAllItemsPicked() {
            // Arrange
            SkuCode skuCode1 = SkuCode.of("SKU-001");
            SkuCode skuCode2 = SkuCode.of("SKU-002");
            BinLocation binLocation1 = new BinLocation("A01");
            BinLocation binLocation2 = new BinLocation("B02");
            String pickerId = "PICKER-001";

            // Mock bin location availability
            when(binLocationService.isLocationAvailable(any())).thenReturn(true);

            // Act
            ConfirmItemPick confirmItemPick1 = new ConfirmItemPick(
                pickList.getId(), 
                skuCode1, 
                Quantity.of(2), 
                binLocation1, 
                pickerId
            );

            ConfirmItemPick confirmItemPick2 = new ConfirmItemPick(
                pickList.getId(), 
                skuCode2, 
                Quantity.of(3), 
                binLocation2, 
                pickerId
            );

            // Assert
            assertThat(pickList.getStatus()).isEqualTo(PickListStatus.PENDING);
        }
    }

    // Helper method to create fulfillment orders
    private FulfillmentOrder createFulfillmentOrder(String orderType, String[] skuCodes, int[] quantities) {
        List<OrderItem> orderItems = createOrderItems(skuCodes, quantities);
        return new FulfillmentOrder(
            OrderId.generate(), 
            orderType, 
            new Address(), 
            orderItems
        );
    }

    // Helper method to create order items
    private List<OrderItem> createOrderItems(String[] skuCodes, int[] quantities) {
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        for (int i = 0; i < skuCodes.length; i++) {
            orderItems.add(
                new OrderItem(
                    SkuCode.of(skuCodes[i]), 
                    Quantity.of(quantities[i])
                )
            );
        }
        return orderItems;
    }
}