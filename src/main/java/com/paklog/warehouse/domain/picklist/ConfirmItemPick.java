package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.BinLocation;

public class ConfirmItemPick {
    private final PickListId pickListId;
    private final SkuCode sku;
    private final Quantity quantity;
    private final BinLocation binLocation;

    public ConfirmItemPick(
        PickListId pickListId, 
        SkuCode sku, 
        Quantity quantity, 
        BinLocation binLocation
    ) {
        this.pickListId = pickListId;
        this.sku = sku;
        this.quantity = quantity;
        this.binLocation = binLocation;
    }

    public PickListId getPickListId() {
        return pickListId;
    }

    public SkuCode getSku() {
        return sku;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public BinLocation getBinLocation() {
        return binLocation;
    }
}