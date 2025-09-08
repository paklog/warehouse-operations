package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.shared.OrderId;

public interface PickListRepository {
    void save(PickList pickList);
    PickList findById(PickListId pickListId);
    PickList findByOrderId(OrderId orderId);
}