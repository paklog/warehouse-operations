package com.paklog.warehouse.application.service;

import com.paklog.warehouse.domain.putwall.*;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.Quantity;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.infrastructure.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PutWallApplicationServiceTest {

    @Mock
    private PutWallRepository putWallRepository;

    @Mock
    private PutWallService putWallService;

    @Mock
    private DomainEventPublisher eventPublisher;

    private PutWallApplicationService applicationService;

    private PutWall putWall;
    private PutWallId putWallId;

    @BeforeEach
    void setUp() {
        applicationService = new PutWallApplicationService(putWallRepository, putWallService, eventPublisher);

        putWallId = PutWallId.generate();
        List<PutWallSlotId> slotIds = List.of(
            PutWallSlotId.of("A1"),
            PutWallSlotId.of("A2")
        );
        putWall = new PutWall(putWallId, slotIds, "Test Location");
    }

    @Test
    void shouldCreatePutWall() {
        List<PutWallSlotId> slotIds = List.of(
            PutWallSlotId.of("A1"),
            PutWallSlotId.of("A2")
        );
        String location = "Warehouse Zone A";

        PutWallId result = applicationService.createPutWall(slotIds, location);

        assertNotNull(result);
        verify(putWallRepository).save(any(PutWall.class));
        // Creating a PutWall doesn't generate domain events by default
    }

    @Test
    void shouldAssignOrderToSlot() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU001"), Quantity.of(1));

        AssignOrderToSlotCommand command = new AssignOrderToSlotCommand(putWallId, orderId, requiredItems);

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        Optional<PutWallSlotId> result = applicationService.assignOrderToSlot(command);

        assertTrue(result.isPresent());
        verify(putWallRepository).save(putWall);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenPutWallNotFoundForAssignment() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU001"), Quantity.of(1));

        AssignOrderToSlotCommand command = new AssignOrderToSlotCommand(putWallId, orderId, requiredItems);

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.empty());

        assertThrows(PutWallException.PutWallNotFoundException.class,
            () -> applicationService.assignOrderToSlot(command));

        verify(putWallRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldScanItemForSortation() {
        SkuCode skuCode = SkuCode.of("SKU001");
        ScanItemForSortationCommand command = new ScanItemForSortationCommand(
            putWallId, skuCode, Quantity.of(1)
        );

        PutWallService.SortationResult expectedResult = PutWallService.SortationResult.found(
            PutWallSlotId.of("A1"), OrderId.generate(), Quantity.of(1)
        );

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));
        when(putWallService.determineSortationTarget(putWall, skuCode)).thenReturn(expectedResult);

        PutWallService.SortationResult result = applicationService.scanItemForSortation(command);

        assertEquals(expectedResult, result);
        verify(putWallService).determineSortationTarget(putWall, skuCode);
    }

    @Test
    void shouldConfirmPutInSlot() {
        SkuCode skuCode = SkuCode.of("SKU001");
        Quantity quantity = Quantity.of(1);

        // First assign an order to the slot
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, quantity);
        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        PutWallSlotId slotId = assignedSlot.get();
        ConfirmPutInSlotCommand command = new ConfirmPutInSlotCommand(putWallId, slotId, skuCode, quantity);

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        applicationService.confirmPutInSlot(command);

        verify(putWallService).validateItemPlacement(putWall, slotId, skuCode, quantity);
        verify(putWallRepository).save(putWall);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldReleaseSlot() {
        // First assign an order and complete it
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));
        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        PutWallSlotId slotId = assignedSlot.get();
        putWall.placeItemInSlot(slotId, skuCode, Quantity.of(1));

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        applicationService.releaseSlot(putWallId, slotId);

        verify(putWallRepository).save(putWall);
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldGetPutWall() {
        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        Optional<PutWall> result = applicationService.getPutWall(putWallId);

        assertTrue(result.isPresent());
        assertEquals(putWall, result.get());
    }

    @Test
    void shouldGetAllPutWalls() {
        List<PutWall> putWalls = List.of(putWall);
        when(putWallRepository.findAll()).thenReturn(putWalls);

        List<PutWall> result = applicationService.getAllPutWalls();

        assertEquals(putWalls, result);
    }

    @Test
    void shouldGetPutWallsByLocation() {
        String location = "Test Location";
        List<PutWall> putWalls = List.of(putWall);
        when(putWallRepository.findByLocation(location)).thenReturn(putWalls);

        List<PutWall> result = applicationService.getPutWallsByLocation(location);

        assertEquals(putWalls, result);
    }

    @Test
    void shouldGetReadyForPackSlots() {
        // Assign and complete an order
        OrderId orderId = OrderId.generate();
        SkuCode skuCode = SkuCode.of("SKU001");
        Map<SkuCode, Quantity> requiredItems = Map.of(skuCode, Quantity.of(1));

        Optional<PutWallSlotId> assignedSlot = putWall.assignOrderToSlot(orderId, requiredItems);
        assertTrue(assignedSlot.isPresent());

        PutWallSlotId slotId = assignedSlot.get();
        putWall.placeItemInSlot(slotId, skuCode, Quantity.of(1));

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        List<PutWallSlotId> result = applicationService.getReadyForPackSlots(putWallId);

        assertEquals(1, result.size());
        assertEquals(slotId, result.get(0));
    }

    @Test
    void shouldFindSlotForOrder() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU001"), Quantity.of(1));

        putWall.assignOrderToSlot(orderId, requiredItems);

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        Optional<PutWallSlotId> result = applicationService.findSlotForOrder(putWallId, orderId);

        assertTrue(result.isPresent());
    }

    @Test
    void shouldGetAvailablePutWalls() {
        int minCapacity = 2;
        List<PutWall> availablePutWalls = List.of(putWall);

        when(putWallRepository.findByAvailableCapacityGreaterThan(minCapacity))
            .thenReturn(availablePutWalls);

        List<PutWall> result = applicationService.getAvailablePutWalls(minCapacity);

        assertEquals(availablePutWalls, result);
    }

    @Test
    void shouldClearDomainEventsAfterPublishing() {
        OrderId orderId = OrderId.generate();
        Map<SkuCode, Quantity> requiredItems = Map.of(SkuCode.of("SKU001"), Quantity.of(1));

        AssignOrderToSlotCommand command = new AssignOrderToSlotCommand(putWallId, orderId, requiredItems);

        when(putWallRepository.findById(putWallId)).thenReturn(Optional.of(putWall));

        applicationService.assignOrderToSlot(command);

        // Verify that events were published and then cleared
        verify(eventPublisher, atLeastOnce()).publish(any());
        assertTrue(putWall.getDomainEvents().isEmpty());
    }
}