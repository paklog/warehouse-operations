package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.picklist.PickListStatus;

import java.util.List;
import java.util.Optional;

public interface PickListQueryService {
    PickList findById(PickListId pickListId);
    List<PickList> findByPickerId(String pickerId);
    List<PickList> findByStatus(PickListStatus status);
    List<PickList> findByPickerIdAndStatus(String pickerId, PickListStatus status);
    Optional<PickList> findNextPickListForPicker(String pickerId);
}