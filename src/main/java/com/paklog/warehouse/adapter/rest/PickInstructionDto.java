package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.domain.picklist.PickInstruction;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.BinLocation;

public class PickInstructionDto {
    private String skuCode;
    private int quantityToPick;
    private int pickedQuantity;
    private String binLocation;
    private boolean completed;

    public static PickInstructionDto fromDomain(PickInstruction instruction) {
        PickInstructionDto dto = new PickInstructionDto();
        dto.setSkuCode(instruction.getSku().getValue());
        dto.setQuantityToPick(instruction.getQuantity().getValue());
        dto.setPickedQuantity(instruction.getQuantity().getValue());
        dto.setBinLocation(instruction.getBinLocation().toString());
        dto.setCompleted(instruction.isCompleted());
        return dto;
    }

    // Getters and setters
    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public int getQuantityToPick() {
        return quantityToPick;
    }

    public void setQuantityToPick(int quantityToPick) {
        this.quantityToPick = quantityToPick;
    }

    public int getPickedQuantity() {
        return pickedQuantity;
    }

    public void setPickedQuantity(int pickedQuantity) {
        this.pickedQuantity = pickedQuantity;
    }

    public String getBinLocation() {
        return binLocation;
    }

    public void setBinLocation(String binLocation) {
        this.binLocation = binLocation;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}