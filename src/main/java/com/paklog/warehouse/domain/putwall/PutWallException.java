package com.paklog.warehouse.domain.putwall;

public class PutWallException extends RuntimeException {

    public PutWallException(String message) {
        super(message);
    }

    public PutWallException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class SlotNotAvailableException extends PutWallException {
        public SlotNotAvailableException(PutWallSlotId slotId) {
            super("Slot " + slotId + " is not available for assignment");
        }
    }

    public static class InvalidItemPlacementException extends PutWallException {
        public InvalidItemPlacementException(String message) {
            super("Invalid item placement: " + message);
        }
    }

    public static class OrderNotAssignedException extends PutWallException {
        public OrderNotAssignedException(String orderId) {
            super("Order " + orderId + " is not assigned to any slot");
        }
    }

    public static class PutWallNotFoundException extends PutWallException {
        public PutWallNotFoundException(PutWallId putWallId) {
            super("PutWall not found: " + putWallId);
        }
    }

    public static class SlotNotFoundException extends PutWallException {
        public SlotNotFoundException(PutWallSlotId slotId) {
            super("Slot not found: " + slotId);
        }
    }

    public static class PutWallCapacityExceededException extends PutWallException {
        public PutWallCapacityExceededException() {
            super("PutWall has reached maximum capacity");
        }
    }

    public static class InvalidSortationException extends PutWallException {
        public InvalidSortationException(String skuCode, String reason) {
            super("Cannot sort SKU " + skuCode + ": " + reason);
        }
    }
}