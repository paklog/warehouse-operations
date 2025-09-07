package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.shared.PickList;
import com.paklog.warehouse.domain.shared.PickListId;
import com.paklog.warehouse.domain.shared.PickListStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultPickListQueryService implements PickListQueryService {
    private final PickListRepository pickListRepository;
    private final List<PickList> pickLists;

    public DefaultPickListQueryService(
        PickListRepository pickListRepository, 
        List<PickList> pickLists
    ) {
        this.pickListRepository = pickListRepository;
        this.pickLists = pickLists;
    }

    @Override
    public PickList findById(PickListId pickListId) {
        return pickLists.stream()
            .filter(pickList -> pickList.getId().equals(pickListId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("PickList not found"));
    }

    @Override
    public List<PickList> findByPickerId(String pickerId) {
        return pickLists.stream()
            .filter(pickList -> pickerId.equals(pickList.getPickerId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<PickList> findByStatus(PickListStatus status) {
        return pickLists.stream()
            .filter(pickList -> status == pickList.getStatus())
            .collect(Collectors.toList());
    }

    @Override
    public List<PickList> findByPickerIdAndStatus(String pickerId, PickListStatus status) {
        return pickLists.stream()
            .filter(pickList -> 
                pickerId.equals(pickList.getPickerId()) && 
                pickList.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<PickList> findNextPickListForPicker(String pickerId) {
        return pickLists.stream()
            .filter(pickList -> 
                pickerId.equals(pickList.getPickerId()) && 
                pickList.getStatus() == PickListStatus.ASSIGNED)
            .findFirst();
    }
}