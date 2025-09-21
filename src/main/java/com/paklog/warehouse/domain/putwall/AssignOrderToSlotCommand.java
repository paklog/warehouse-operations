package com.paklog.warehouse.domain.putwall;

import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.util.Map;
import java.util.Objects;

public class AssignOrderToSlotCommand {
    private final PutWallId putWallId;
    private final OrderId orderId;
    private final Map<SkuCode, Quantity> requiredItems;

    public AssignOrderToSlotCommand(PutWallId putWallId, OrderId orderId, Map<SkuCode, Quantity> requiredItems) {
        this.putWallId = Objects.requireNonNull(putWallId, "PutWall ID cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.requiredItems = Objects.requireNonNull(requiredItems, "Required items cannot be null");

        if (requiredItems.isEmpty()) {
            throw new IllegalArgumentException("Required items cannot be empty");
        }

        requiredItems.values().forEach(quantity -> {
            if (quantity.getValue() <= 0) {
                throw new IllegalArgumentException("All quantities must be positive");
            }
        });
    }

    public PutWallId getPutWallId() {
        return putWallId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Map<SkuCode, Quantity> getRequiredItems() {
        return requiredItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignOrderToSlotCommand that = (AssignOrderToSlotCommand) o;
        return Objects.equals(putWallId, that.putWallId) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(requiredItems, that.requiredItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(putWallId, orderId, requiredItems);
    }

    @Override
    public String toString() {
        return "AssignOrderToSlotCommand{" +
                "putWallId=" + putWallId +
                ", orderId=" + orderId +
                ", requiredItems=" + requiredItems +
                '}';
    }
}