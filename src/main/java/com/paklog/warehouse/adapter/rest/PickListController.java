package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.application.service.PickListQueryService;
import com.paklog.warehouse.domain.picklist.ConfirmItemPick;
import com.paklog.warehouse.domain.picklist.ConfirmItemPickHandler;
import com.paklog.warehouse.domain.shared.PickListId;
import com.paklog.warehouse.domain.shared.PickListStatus;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.PickList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/picklists")
public class PickListController {
    private final PickListQueryService pickListQueryService;
    private final ConfirmItemPickHandler confirmItemPickHandler;

    public PickListController(
        PickListQueryService pickListQueryService,
        ConfirmItemPickHandler confirmItemPickHandler
    ) {
        this.pickListQueryService = pickListQueryService;
        this.confirmItemPickHandler = confirmItemPickHandler;
    }

    @GetMapping("/{pickListId}")
    public ResponseEntity<PickListDto> getPickList(@PathVariable @NotBlank String pickListId) {
        PickList pickList = pickListQueryService.findById(PickListId.of(pickListId));
        if (pickList == null) {
            throw EntityNotFoundException.pickListNotFound(pickListId);
        }
        return ResponseEntity.ok(PickListDto.fromDomain(pickList));
    }

    @GetMapping("/picker/{pickerId}")
    public ResponseEntity<List<PickListDto>> getPickListsByPicker(@PathVariable @NotBlank String pickerId) {
        List<PickList> pickLists = pickListQueryService.findByPickerId(pickerId);
        return ResponseEntity.ok(
            pickLists.stream()
                .map(PickListDto::fromDomain)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PickListDto>> getPickListsByStatus(@PathVariable PickListStatus status) {
        List<PickList> pickLists = pickListQueryService.findByStatus(status);
        return ResponseEntity.ok(
            pickLists.stream()
                .map(PickListDto::fromDomain)
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/{pickListId}/confirm-pick")
    public ResponseEntity<Void> confirmItemPick(
        @PathVariable @NotBlank String pickListId,
        @RequestBody @Valid ConfirmItemPickRequest request
    ) {
        ConfirmItemPick command = new ConfirmItemPick(
            PickListId.of(pickListId),
            SkuCode.of(request.getSkuCode()),
            Quantity.of(request.getQuantity()),
            BinLocation.of(request.getBinLocation())
        );
        
        confirmItemPickHandler.handle(command);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/picker/{pickerId}/next")
    public ResponseEntity<PickListDto> getNextPickListForPicker(@PathVariable @NotBlank String pickerId) {
        return pickListQueryService.findNextPickListForPicker(pickerId)
            .map(pickList -> ResponseEntity.ok(PickListDto.fromDomain(pickList)))
            .orElse(ResponseEntity.noContent().build());
    }

    // Inner class for request body
    public static class ConfirmItemPickRequest {
        @NotBlank(message = "SKU code is required")
        private String skuCode;
        
        @Positive(message = "Quantity must be positive")
        private int quantity;
        
        @NotBlank(message = "Bin location is required")
        private String binLocation;

        // Getters and setters
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getBinLocation() { return binLocation; }
        public void setBinLocation(String binLocation) { this.binLocation = binLocation; }
    }
}