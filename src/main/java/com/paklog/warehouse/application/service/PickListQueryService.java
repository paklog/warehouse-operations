package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.PickListId;
import com.paklog.warehouse.domain.shared.PickListStatus;

import java.util.List;
import java.util.Optional;

public interface PickListQueryService {
    PickList findById(PickListId pickListId);
    List<PickList> findByPickerId(String pickerId);
    List<PickList> findByStatus(PickListStatus status);
    List<PickList> findByPickerIdAndStatus(String pickerId, PickListStatus status);
    Optional<PickList> findNextPickListForPicker(String pickerId);
}