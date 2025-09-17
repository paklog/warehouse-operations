package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.*;
import java.util.ArrayList;
import java.util.List;

public class PickListTestHelper {
    
    public static PickList createPickListWithInstruction(SkuCode skuCode, Quantity quantity, BinLocation binLocation) {
        OrderId orderId = OrderId.generate();
        PickInstruction instruction = new PickInstruction(skuCode, quantity, binLocation);
        List<PickInstruction> instructions = new ArrayList<>();
        instructions.add(instruction);
        
        return new PickList(PickListId.generate(), orderId, instructions);
    }
    
    public static PickList createEmptyPickList() {
        return new PickList(OrderId.generate());
    }
    
    public static PickInstruction createPickInstruction(SkuCode skuCode, Quantity quantity, BinLocation binLocation) {
        return new PickInstruction(skuCode, quantity, binLocation);
    }
}