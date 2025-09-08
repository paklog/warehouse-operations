package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListStatus;

import java.util.List;
import java.util.stream.Collectors;

public class PickListDto {
    private String id;
    private String orderId;
    private PickListStatus status;
    private String pickerId;
    private List<PickInstructionDto> instructions;

    public static PickListDto fromDomain(PickList pickList) {
        PickListDto dto = new PickListDto();
        dto.setId(pickList.getId().getValue().toString());
        dto.setOrderId(pickList.getOrderId().getValue().toString());
        dto.setStatus(pickList.getStatus());
        dto.setPickerId(pickList.getPickerId());
        dto.setInstructions(
            pickList.getInstructions().stream()
                .map(PickInstructionDto::fromDomain)
                .collect(Collectors.toList())
        );
        return dto;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PickListStatus getStatus() {
        return status;
    }

    public void setStatus(PickListStatus status) {
        this.status = status;
    }

    public String getPickerId() {
        return pickerId;
    }

    public void setPickerId(String pickerId) {
        this.pickerId = pickerId;
    }

    public List<PickInstructionDto> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<PickInstructionDto> instructions) {
        this.instructions = instructions;
    }
}