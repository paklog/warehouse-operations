package com.paklog.warehouse.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FulfillmentOrder {
    private final OrderId orderId;
    private final String orderType;
    private final Address shippingAddress;
    private final List<OrderItem> items;

    public FulfillmentOrder(OrderId orderId, String orderType, Address shippingAddress, List<OrderItem> items) {
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.orderType = Objects.requireNonNull(orderType, "Order type cannot be null");
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "Shipping address cannot be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Order items cannot be null"));
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FulfillmentOrder that = (FulfillmentOrder) o;
        return orderId.equals(that.orderId) && 
               orderType.equals(that.orderType) && 
               shippingAddress.equals(that.shippingAddress) && 
               items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, orderType, shippingAddress, items);
    }

    @Override
    public String toString() {
        return "FulfillmentOrder{" +
               "orderId=" + orderId +
               ", orderType='" + orderType + '\'' +
               ", shippingAddress=" + shippingAddress +
               ", items=" + items +
               '}';
    }
}