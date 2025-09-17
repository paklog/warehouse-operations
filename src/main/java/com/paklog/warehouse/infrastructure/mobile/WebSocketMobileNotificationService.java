package com.paklog.warehouse.infrastructure.mobile;

import com.paklog.warehouse.application.mobile.MobileNotificationService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class WebSocketMobileNotificationService implements MobileNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMobileNotificationService.class);
    
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMobileNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = Objects.requireNonNull(messagingTemplate, 
            "SimpMessagingTemplate cannot be null");
    }

    @Override
    public void notifyWorkStarted(UUID workId, String workerId) {
        logger.info("Notifying work started: {} for worker: {}", workId, workerId);
        
        Map<String, Object> notification = createNotification("WORK_STARTED", 
            "Work started successfully", Map.of(
                "workId", workId.toString(),
                "action", "GET_WORK_DETAILS"
            ));
        
        sendWorkerUpdate(workerId, "Work Started", notification);
    }

    @Override
    public void notifyStepCompleted(UUID workId, int stepNumber, String workerId) {
        logger.debug("Notifying step completed: {} step {} for worker: {}", 
                    workId, stepNumber, workerId);
        
        Map<String, Object> notification = createNotification("STEP_COMPLETED", 
            "Step " + stepNumber + " completed", Map.of(
                "workId", workId.toString(),
                "stepNumber", stepNumber,
                "action", "CONTINUE_WORK"
            ));
        
        sendWorkerUpdate(workerId, "Step Completed", notification);
    }

    @Override
    public void notifyWorkCompleted(UUID workId, String workerId) {
        logger.info("Notifying work completed: {} for worker: {}", workId, workerId);
        
        Map<String, Object> notification = createNotification("WORK_COMPLETED", 
            "Work completed successfully! Great job!", Map.of(
                "workId", workId.toString(),
                "action", "GET_NEXT_WORK",
                "showCelebration", true
            ));
        
        sendWorkerUpdate(workerId, "Work Completed", notification);
    }

    @Override
    public void notifyWorkSuspended(UUID workId, String workerId, String reason) {
        logger.info("Notifying work suspended: {} for worker: {} - reason: {}", 
                   workId, workerId, reason);
        
        Map<String, Object> notification = createNotification("WORK_SUSPENDED", 
            "Work suspended: " + reason, Map.of(
                "workId", workId.toString(),
                "reason", reason,
                "action", "CONTACT_SUPERVISOR",
                "priority", "HIGH"
            ));
        
        sendWorkerUpdate(workerId, "Work Suspended", notification);
    }

    @Override
    public void notifyWorkResumed(UUID workId, String workerId) {
        logger.info("Notifying work resumed: {} for worker: {}", workId, workerId);
        
        Map<String, Object> notification = createNotification("WORK_RESUMED", 
            "Work resumed - you can continue", Map.of(
                "workId", workId.toString(),
                "action", "CONTINUE_WORK"
            ));
        
        sendWorkerUpdate(workerId, "Work Resumed", notification);
    }

    @Override
    public void notifyEmergencyStop(String workerId, String reason) {
        logger.warn("Notifying emergency stop for worker: {} - reason: {}", workerId, reason);
        
        Map<String, Object> notification = createNotification("EMERGENCY_STOP", 
            "EMERGENCY STOP: " + reason, Map.of(
                "reason", reason,
                "action", "STOP_ALL_WORK",
                "priority", "CRITICAL",
                "requiresAcknowledgment", true
            ));
        
        sendWorkerUpdate(workerId, "Emergency Stop", notification);
        
        // Also send to supervisors
        sendZoneMessage("SUPERVISORS", "Emergency Stop", notification);
    }

    @Override
    public void notifyWorkAssigned(UUID workId, String workerId) {
        logger.info("Notifying work assigned: {} to worker: {}", workId, workerId);
        
        Map<String, Object> notification = createNotification("WORK_ASSIGNED", 
            "New work has been assigned to you", Map.of(
                "workId", workId.toString(),
                "action", "START_WORK",
                "playSound", true
            ));
        
        sendWorkerUpdate(workerId, "Work Assigned", notification);
    }

    @Override
    public void sendWorkerUpdate(String workerId, String message, Object data) {
        try {
            String destination = "/topic/worker/" + workerId;
            messagingTemplate.convertAndSend(destination, data);
            logger.debug("Sent update to worker {}: {}", workerId, message);
            
        } catch (Exception e) {
            logger.error("Failed to send update to worker {}: {}", workerId, e.getMessage());
        }
    }

    @Override
    public void broadcastToAllWorkers(String message, Object data) {
        try {
            messagingTemplate.convertAndSend("/topic/broadcast", data);
            logger.info("Broadcast message sent: {}", message);
            
        } catch (Exception e) {
            logger.error("Failed to send broadcast: {}", e.getMessage());
        }
    }

    @Override
    public void sendZoneMessage(String zone, String message, Object data) {
        try {
            String destination = "/topic/zone/" + zone;
            messagingTemplate.convertAndSend(destination, data);
            logger.debug("Sent zone message to {}: {}", zone, message);
            
        } catch (Exception e) {
            logger.error("Failed to send zone message to {}: {}", zone, e.getMessage());
        }
    }

    private Map<String, Object> createNotification(String type, String message, 
                                                  Map<String, Object> data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", Instant.now());
        notification.put("data", data);
        return notification;
    }
}