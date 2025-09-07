package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

public class ItemPickedEvent implements DomainEvent {
    private final PickListId pickListId;
    private final SkuCode sku;
    private final Quantity quantity;
    private final BinLocation binLocation;
    private final String pickerId;

    public ItemPickedEvent(PickListId pickListId, SkuCode sku, Quantity quantity, BinLocation binLocation, String pickerId) {
        this.pickListId = pickListId;
        this.sku = sku;
        this.quantity = quantity;
        this.binLocation = binLocation;
        this.pickerId = pickerId;
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

    public String getPickerId() {
        return pickerId;
    }
}