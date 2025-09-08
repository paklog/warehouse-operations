package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.shared.OrderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PickListRepositoryAdapter implements PickListRepository {

    private final SpringPickListRepository springPickListRepository;

    @Autowired
    public PickListRepositoryAdapter(SpringPickListRepository springPickListRepository) {
        this.springPickListRepository = springPickListRepository;
    }

    @Override
    public void save(PickList pickList) {
        PickListDocument document = new PickListDocument(pickList);
        springPickListRepository.save(document);
    }

    @Override
    public PickList findById(PickListId pickListId) {
        Optional<PickListDocument> document = springPickListRepository.findById(pickListId.getValue().toString());
        return document.map(PickListDocument::toDomain).orElse(null);
    }

    @Override
    public PickList findByOrderId(OrderId orderId) {
        Optional<PickListDocument> document = springPickListRepository.findByOrderId(orderId.getValue().toString());
        return document.map(PickListDocument::toDomain).orElse(null);
    }
}