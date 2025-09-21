package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.putwall.*;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PutWallApplicationService {

    private final PutWallRepository putWallRepository;
    private final PutWallService putWallService;
    private final DomainEventPublisher eventPublisher;

    public PutWallApplicationService(PutWallRepository putWallRepository,
                                   PutWallService putWallService,
                                   DomainEventPublisher eventPublisher) {
        this.putWallRepository = putWallRepository;
        this.putWallService = putWallService;
        this.eventPublisher = eventPublisher;
    }

    public PutWallId createPutWall(List<PutWallSlotId> slotIds, String location) {
        PutWallId putWallId = PutWallId.generate();
        PutWall putWall = new PutWall(putWallId, slotIds, location);

        putWallRepository.save(putWall);
        publishEvents(putWall);

        return putWallId;
    }

    public Optional<PutWallSlotId> assignOrderToSlot(AssignOrderToSlotCommand command) {
        PutWall putWall = getPutWallById(command.getPutWallId());

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(
            command.getOrderId(),
            command.getRequiredItems()
        );

        if (assignedSlot.isPresent()) {
            putWallRepository.save(putWall);
            publishEvents(putWall);
        }

        return assignedSlot;
    }

    public PutWallService.SortationResult scanItemForSortation(ScanItemForSortationCommand command) {
        PutWall putWall = getPutWallById(command.getPutWallId());

        return putWallService.determineSortationTarget(putWall, command.getSkuCode());
    }

    public void confirmPutInSlot(ConfirmPutInSlotCommand command) {
        PutWall putWall = getPutWallById(command.getPutWallId());

        putWallService.validateItemPlacement(
            putWall,
            command.getSlotId(),
            command.getSkuCode(),
            command.getQuantity()
        );

        putWall.placeItemInSlot(
            command.getSlotId(),
            command.getSkuCode(),
            command.getQuantity()
        );

        putWallRepository.save(putWall);
        publishEvents(putWall);
    }

    public void releaseSlot(PutWallId putWallId, PutWallSlotId slotId) {
        PutWall putWall = getPutWallById(putWallId);

        putWall.releaseSlot(slotId);

        putWallRepository.save(putWall);
        publishEvents(putWall);
    }

    @Transactional(readOnly = true)
    public Optional<PutWall> getPutWall(PutWallId putWallId) {
        return putWallRepository.findById(putWallId);
    }

    @Transactional(readOnly = true)
    public List<PutWall> getAllPutWalls() {
        return putWallRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PutWall> getPutWallsByLocation(String location) {
        return putWallRepository.findByLocation(location);
    }

    @Transactional(readOnly = true)
    public List<PutWallSlotId> getReadyForPackSlots(PutWallId putWallId) {
        PutWall putWall = getPutWallById(putWallId);
        return putWall.getReadyForPackSlots();
    }

    @Transactional(readOnly = true)
    public Optional<PutWallSlotId> findSlotForOrder(PutWallId putWallId, OrderId orderId) {
        PutWall putWall = getPutWallById(putWallId);
        return putWall.findSlotForOrder(orderId);
    }

    @Transactional(readOnly = true)
    public List<PutWall> getAvailablePutWalls(int minCapacity) {
        return putWallRepository.findByAvailableCapacityGreaterThan(minCapacity);
    }

    private PutWall getPutWallById(PutWallId putWallId) {
        return putWallRepository.findById(putWallId)
            .orElseThrow(() -> new PutWallException.PutWallNotFoundException(putWallId));
    }

    private void publishEvents(PutWall putWall) {
        putWall.getDomainEvents().forEach(eventPublisher::publish);
        putWall.clearDomainEvents();
    }
}