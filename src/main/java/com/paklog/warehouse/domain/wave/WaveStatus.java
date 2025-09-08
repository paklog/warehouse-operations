package com.paklog.warehouse.domain.wave;

public enum WaveStatus {
    /**
     * Wave has been created and orders have been assigned but not yet released for picking
     */
    PLANNED,
    
    /**
     * Wave has been released and picking work can begin
     */
    RELEASED,
    
    /**
     * All picking work in the wave is complete and wave is closed
     */
    CLOSED,
    
    /**
     * Wave has been cancelled and will not be processed
     */
    CANCELLED
}