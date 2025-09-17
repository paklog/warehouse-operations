package com.paklog.warehouse.application.mobile;

import java.util.UUID;

/**
 * Service for sending mobile notifications and real-time updates to warehouse workers
 */
public interface MobileNotificationService {
    
    /**
     * Notify when work is started by a worker
     */
    void notifyWorkStarted(UUID workId, String workerId);
    
    /**
     * Notify when a step is completed
     */
    void notifyStepCompleted(UUID workId, int stepNumber, String workerId);
    
    /**
     * Notify when work is completed
     */
    void notifyWorkCompleted(UUID workId, String workerId);
    
    /**
     * Notify when work is suspended
     */
    void notifyWorkSuspended(UUID workId, String workerId, String reason);
    
    /**
     * Notify when work is resumed
     */
    void notifyWorkResumed(UUID workId, String workerId);
    
    /**
     * Send emergency stop notification
     */
    void notifyEmergencyStop(String workerId, String reason);
    
    /**
     * Notify about work assignment
     */
    void notifyWorkAssigned(UUID workId, String workerId);
    
    /**
     * Send real-time update to specific worker
     */
    void sendWorkerUpdate(String workerId, String message, Object data);
    
    /**
     * Send broadcast message to all active workers
     */
    void broadcastToAllWorkers(String message, Object data);
    
    /**
     * Send message to workers in specific zone/area
     */
    void sendZoneMessage(String zone, String message, Object data);
}