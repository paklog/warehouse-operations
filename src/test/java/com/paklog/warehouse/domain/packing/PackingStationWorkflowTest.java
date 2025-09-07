package com.paklog.warehouse.domain.packing;

import com.paklog.warehouse.application.service.PackingStationService;
import com.paklog.warehouse.domain.picklist.PickListRepository;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.OrderItem;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackingStationWorkflowTest {
    @Mock
    private PackageRepository packageRepository;

    @Mock
    private PickListRepository pickListRepository;

    @Mock
    private FulfillmentOrder mockOrder;

    @Mock
    private com.paklog.warehouse.domain.shared.PickList mockPickList;

    private PackingStationService packingStationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        packingStationService = new PackingStationService(packageRepository, pickListRepository);
    }

    @Test
    void testCreatePackage() {
        // Prepare test data
        OrderId orderId = OrderId.generate();
        when(mockOrder.getOrderId()).thenReturn(orderId);
        
        // Mock the pick list repository to return a mock pick list
        when(pickListRepository.findByOrderId(orderId)).thenReturn(mockPickList);
        
        // Create package
        Package pkg = packingStationService.createPackage(mockOrder);
        
        // Verify package creation
        assertNotNull(pkg);
        assertEquals(PackageStatus.PENDING, pkg.getStatus());
        assertEquals(mockOrder, pkg.getOrder());
        
        // Verify repository save was called
        verify(packageRepository).save(pkg);
    }

    @Test
    void testCreatePackageWithNoPickList() {
        // Prepare test data
        OrderId orderId = OrderId.generate();
        when(mockOrder.getOrderId()).thenReturn(orderId);
        
        // Mock the pick list repository to return null
        when(pickListRepository.findByOrderId(orderId)).thenReturn(null);
        
        // Verify that creating a package throws an exception
        assertThrows(IllegalStateException.class, () -> 
            packingStationService.createPackage(mockOrder)
        );
    }

    @Test
    void testAddPackedItems() {
        // Prepare test data
        OrderId orderId = OrderId.generate();
        when(mockOrder.getOrderId()).thenReturn(orderId);
        Package pkg = new Package(mockOrder);
        
        // Create mock packed items
        PackedItem mockItem1 = mock(PackedItem.class);
        when(mockItem1.getSkuCode()).thenReturn(SkuCode.of("SKU-001"));
        when(mockItem1.getQuantity()).thenReturn(Quantity.of(2));
        
        PackedItem mockItem2 = mock(PackedItem.class);
        when(mockItem2.getSkuCode()).thenReturn(SkuCode.of("SKU-002"));
        when(mockItem2.getQuantity()).thenReturn(Quantity.of(3));
        
        // Add packed items
        pkg.addPackedItem(mockItem1);
        pkg.addPackedItem(mockItem2);
        
        // Verify items were added
        assertEquals(2, pkg.getPackedItems().size());
        assertTrue(pkg.getPackedItems().contains(mockItem1));
        assertTrue(pkg.getPackedItems().contains(mockItem2));
    }

    @Test
    void testConfirmPackage() {
        // Prepare test data
        OrderId orderId = OrderId.generate();
        when(mockOrder.getOrderId()).thenReturn(orderId);
        
        // Mock order items
        List<OrderItem> orderItems = Arrays.asList(
            createMockOrderItem("SKU-001", 2),
            createMockOrderItem("SKU-002", 3)
        );
        when(mockOrder.getItems()).thenReturn(orderItems);
        
        Package pkg = new Package(mockOrder);
        
        // Add packed items matching the order
        PackedItem mockItem1 = mock(PackedItem.class);
        when(mockItem1.getSkuCode()).thenReturn(SkuCode.of("SKU-001"));
        when(mockItem1.getQuantity()).thenReturn(Quantity.of(2));
        
        PackedItem mockItem2 = mock(PackedItem.class);
        when(mockItem2.getSkuCode()).thenReturn(SkuCode.of("SKU-002"));
        when(mockItem2.getQuantity()).thenReturn(Quantity.of(3));
        
        pkg.addPackedItem(mockItem1);
        pkg.addPackedItem(mockItem2);
        
        // Confirm package
        packingStationService.confirmPackage(pkg);
        
        // Verify package status and repository save
        assertEquals(PackageStatus.CONFIRMED, pkg.getStatus());
        verify(packageRepository).save(pkg);
    }

    @Test
    void testConfirmPackageWithNoItems() {
        // Prepare test data
        OrderId orderId = OrderId.generate();
        when(mockOrder.getOrderId()).thenReturn(orderId);
        Package pkg = new Package(mockOrder);
        
        // Verify that confirming a package without items throws an exception
        assertThrows(IllegalStateException.class, () -> 
            packingStationService.confirmPackage(pkg)
        );
    }

    @Test
    void testConfirmPackageWithMismatchedItems() {
        // Prepare test data
        OrderId orderId = OrderId.generate();
        when(mockOrder.getOrderId()).thenReturn(orderId);
        
        // Mock order items
        List<OrderItem> orderItems = Arrays.asList(
            createMockOrderItem("SKU-001", 2),
            createMockOrderItem("SKU-002", 3)
        );
        when(mockOrder.getItems()).thenReturn(orderItems);
        
        Package pkg = new Package(mockOrder);
        
        // Add packed items that do not match the order
        PackedItem mockItem = mock(PackedItem.class);
        when(mockItem.getSkuCode()).thenReturn(SkuCode.of("SKU-003"));
        when(mockItem.getQuantity()).thenReturn(Quantity.of(5));
        
        pkg.addPackedItem(mockItem);
        
        // Verify that confirming a package with mismatched items throws an exception
        assertThrows(IllegalStateException.class, () -> 
            packingStationService.confirmPackage(pkg)
        );
    }

    // Helper method to create a mock order item
    private OrderItem createMockOrderItem(String skuCode, int quantity) {
        OrderItem mockOrderItem = mock(OrderItem.class);
        when(mockOrderItem.getSkuCode()).thenReturn(SkuCode.of(skuCode));
        when(mockOrderItem.getQuantity()).thenReturn(Quantity.of(quantity));
        return mockOrderItem;
    }
}