package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.application.service.PackingStationService;
import com.paklog.warehouse.domain.packaging.Package;
import com.paklog.warehouse.domain.shared.OrderId;
import com.paklog.warehouse.domain.shared.FulfillmentOrder;
import com.paklog.warehouse.domain.shared.Address;
import com.paklog.warehouse.domain.shared.OrderItem;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.*;

// OpenAPI Documentation imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/packages")
@Tag(name = "Packages", description = "REST API for managing warehouse packages and shipping")
public class PackageController {

    private final PackingStationService packingStationService;

    public PackageController(PackingStationService packingStationService) {
        this.packingStationService = packingStationService;
    }

    @PostMapping
    public ResponseEntity<PackageDto> createPackage(@RequestBody @Valid CreatePackageRequest request) {
        // Placeholder implementation for Address creation
        Address shippingAddress = createAddressFromRequest(request);

        List<OrderItem> orderItems = request.getItems().stream()
            .map(item -> new OrderItem(
                SkuCode.of(item.getSkuCode()), 
                Quantity.of(item.getQuantity())
            ))
            .collect(Collectors.toList());

        FulfillmentOrder order = new FulfillmentOrder(
            OrderId.generate(), 
            request.getOrderType(), 
            shippingAddress, 
            orderItems
        );

        // Create package through service
        Package pkg = packingStationService.createPackage(order);

        return ResponseEntity.ok(PackageDto.fromDomain(pkg));
    }

    @PatchMapping("/{packageId}/confirm")
    public ResponseEntity<PackageDto> confirmPackage(@PathVariable @NotBlank String packageId) {
        try {
            // Retrieve package first, then confirm
            Package existingPackage = packingStationService.findPackageByOrderId(OrderId.of(packageId));
            packingStationService.confirmPackage(existingPackage);
            return ResponseEntity.ok(PackageDto.fromDomain(existingPackage));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PackageDto> getPackageByOrderId(@PathVariable @NotBlank String orderId) {
        try {
            // Convert orderId to OrderId
            Package pkg = packingStationService.findPackageByOrderId(OrderId.of(orderId));
            return ResponseEntity.ok(PackageDto.fromDomain(pkg));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper method to create Address
    private Address createAddressFromRequest(CreatePackageRequest request) {
        // Placeholder implementation - uses default constructor and setters
        // TODO: Replace with proper Address constructor when available
        Address address = new Address();
        // Assuming setters exist - adjust as needed
        // address.setStreet(request.getStreet());
        // address.setCity(request.getCity());
        // address.setState(request.getState());
        // address.setPostalCode(request.getPostalCode());
        // address.setCountry(request.getCountry());
        return address;
    }

    // Inner class for package creation request
    public static class CreatePackageRequest {
        @NotBlank(message = "Order type is required")
        private String orderType;
        
        @NotBlank(message = "Street is required")
        private String street;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Postal code is required")
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        private String country;
        
        @NotEmpty(message = "Items list cannot be empty")
        private List<@Valid OrderItemRequest> items;

        // Getters and setters
        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public List<OrderItemRequest> getItems() {
            return items;
        }

        public void setItems(List<OrderItemRequest> items) {
            this.items = items;
        }
    }

    // Inner class for order items in the request
    public static class OrderItemRequest {
        @NotBlank(message = "SKU code is required")
        private String skuCode;
        
        @Positive(message = "Quantity must be positive")
        private int quantity;

        // Getters and setters
        public String getSkuCode() {
            return skuCode;
        }

        public void setSkuCode(String skuCode) {
            this.skuCode = skuCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}