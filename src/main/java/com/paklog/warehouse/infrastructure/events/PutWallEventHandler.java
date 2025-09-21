package com.paklog.warehouse.infrastructure.events;

import com.paklog.warehouse.domain.putwall.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PutWallEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PutWallEventHandler.class);
    private final PutWallMetricsService metricsService;

    public PutWallEventHandler(PutWallMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @EventListener
    @Async
    public void handleOrderAssignedToSlotEvent(OrderAssignedToSlotEvent event) {
        logger.info("Order {} assigned to slot {} in put wall {}",
            event.getOrderId(), event.getSlotId(), event.getPutWallId());

        metricsService.recordOrderAssignment(event.getPutWallId(), event.getOrderId().toString());

        // Could trigger light activation system here
        logger.debug("Activating slot light for slot {} in put wall {}",
            event.getSlotId(), event.getPutWallId());
    }

    @EventListener
    @Async
    public void handleItemPlacedInSlotEvent(ItemPlacedInSlotEvent event) {
        logger.info("Item {} (qty: {}) placed in slot {} for order {} in put wall {}",
            event.getSkuCode(), event.getQuantity().getValue(), event.getSlotId(),
            event.getOrderId(), event.getPutWallId());

        metricsService.recordItemPlacement(event.getPutWallId());

        // Could trigger inventory update or WMS notification here
        logger.debug("Recording item placement for inventory tracking");
    }

    @EventListener
    @Async
    public void handleOrderConsolidatedInSlotEvent(OrderConsolidatedInSlotEvent event) {
        logger.info("Order {} consolidated and ready for pack in slot {} of put wall {}",
            event.getOrderId(), event.getSlotId(), event.getPutWallId());

        metricsService.recordOrderCompletion(event.getPutWallId(), event.getOrderId().toString());

        // Could trigger pack side notification here
        logger.debug("Notifying pack side that slot {} is ready for packing", event.getSlotId());

        // Could activate pack side light
        logger.debug("Activating pack side light for slot {}", event.getSlotId());
    }

    @EventListener
    @Async
    public void handleSlotReleasedEvent(SlotReleasedEvent event) {
        logger.info("Slot {} released in put wall {} after completing order {}",
            event.getSlotId(), event.getPutWallId(), event.getReleasedOrderId());

        // Could deactivate lights and reset slot indicators
        logger.debug("Deactivating all lights for slot {} in put wall {}",
            event.getSlotId(), event.getPutWallId());

        // Could trigger slot availability notification for WMS
        logger.debug("Notifying WMS that slot {} is available for new assignment", event.getSlotId());
    }
}