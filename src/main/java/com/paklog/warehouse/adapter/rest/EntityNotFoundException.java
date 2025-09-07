package com.paklog.warehouse.adapter.rest;

public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static EntityNotFoundException pickListNotFound(String pickListId) {
        return new EntityNotFoundException("PickList not found with ID: " + pickListId);
    }
    
    public static EntityNotFoundException packageNotFound(String packageId) {
        return new EntityNotFoundException("Package not found with ID: " + packageId);
    }
    
    public static EntityNotFoundException orderNotFound(String orderId) {
        return new EntityNotFoundException("Order not found with ID: " + orderId);
    }
}