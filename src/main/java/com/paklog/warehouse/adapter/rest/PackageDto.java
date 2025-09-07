package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.domain.packing.Package;
import com.paklog.warehouse.domain.packing.PackageStatus;
import com.paklog.warehouse.domain.shared.OrderItem;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PackageDto {
    private UUID packageId;
    private String orderId;
    private String orderType;
    private List<OrderItemDto> items;
    private PackageStatus status;

    public static PackageDto fromDomain(Package pkg) {
        PackageDto dto = new PackageDto();
        dto.setPackageId(pkg.getPackageId());
        dto.setOrderId(pkg.getOrder().getOrderId().getValue().toString());
        dto.setOrderType(pkg.getOrder().getOrderType());
        dto.setItems(
            pkg.getOrder().getItems().stream()
                .map(OrderItemDto::fromDomain)
                .collect(Collectors.toList())
        );
        dto.setStatus(pkg.getStatus());
        return dto;
    }

    // Getters and setters
    public UUID getPackageId() {
        return packageId;
    }

    public void setPackageId(UUID packageId) {
        this.packageId = packageId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    // Inner DTO for order items
    public static class OrderItemDto {
        private String skuCode;
        private int quantity;

        public static OrderItemDto fromDomain(OrderItem item) {
            OrderItemDto dto = new OrderItemDto();
            dto.setSkuCode(item.getSkuCode().getValue());
            dto.setQuantity(item.getQuantity().getValue());
            return dto;
        }

        // Getters and setters
        public String getSkuCode() {
            return skuCode;
        }

        public void setSkuCode(String skuCode) {
            this.skuCode = skuCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}