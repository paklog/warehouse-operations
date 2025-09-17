package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.picklist.PickInstruction;
import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.picklist.PickListStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BarcodeScanValidationTest {
    private PickList pickList;
    private PickInstruction validInstruction;

    @BeforeEach
    void setUp() {
        // Create a pick list with a valid instruction
        pickList = new PickList(OrderId.generate());
        
        // Create a mock valid pick instruction
        validInstruction = Mockito.mock(PickInstruction.class);
        when(validInstruction.getSku()).thenReturn(SkuCode.of("SKU-001"));
        when(validInstruction.getQuantity()).thenReturn(Quantity.of(5));
        when(validInstruction.getBinLocation()).thenReturn(BinLocation.of("A1-B2-C3"));
        when(validInstruction.isCompleted()).thenReturn(false);
        
        // Add the instruction to the pick list
        pickList.addInstruction(validInstruction);
    }

    @Test
    void testSuccessfulItemPick() {
        // Attempt to pick a valid item
        assertDoesNotThrow(() -> 
            pickList.pickItem(
                SkuCode.of("SKU-001"), 
                Quantity.of(5), 
                BinLocation.of("A1-B2-C3")
            )
        );
        
        // Verify the instruction is marked as completed
        assertTrue(validInstruction.isCompleted());
    }

    @Test
    void testPickWithIncorrectSku() {
        // Attempt to pick with an incorrect SKU
        assertThrows(IllegalArgumentException.class, () -> 
            pickList.pickItem(
                SkuCode.of("SKU-002"), 
                Quantity.of(5), 
                BinLocation.of("A1-B2-C3")
            )
        );
    }

    @Test
    void testPickWithIncorrectQuantity() {
        // Attempt to pick with an incorrect quantity
        assertThrows(IllegalArgumentException.class, () -> 
            pickList.pickItem(
                SkuCode.of("SKU-001"), 
                Quantity.of(3), 
                BinLocation.of("A1-B2-C3")
            )
        );
    }

    @Test
    void testPickAlreadyCompletedInstruction() {
        // First, complete the instruction
        pickList.pickItem(
            SkuCode.of("SKU-001"), 
            Quantity.of(5), 
            BinLocation.of("A1-B2-C3")
        );
        
        // Attempt to pick the same instruction again
        assertThrows(IllegalArgumentException.class, () -> 
            pickList.pickItem(
                SkuCode.of("SKU-001"), 
                Quantity.of(5), 
                BinLocation.of("A1-B2-C3")
            )
        );
    }

    @Test
    void testPickListCompletionStatus() {
        // Pick the instruction
        pickList.pickItem(
            SkuCode.of("SKU-001"), 
            Quantity.of(5), 
            BinLocation.of("A1-B2-C3")
        );
        
        // Verify the pick list status is completed
        assertEquals(PickListStatus.COMPLETED, pickList.getStatus());
    }

    @Test
    void testNullInputValidation() {
        // Test null inputs
        assertThrows(NullPointerException.class, () -> 
            pickList.pickItem(null, null, null)
        );
    }
}