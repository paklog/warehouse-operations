package com.paklog.warehouse.infrastructure.web.mobile;

import com.paklog.warehouse.application.mobile.MobileNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Controller
public class MobileWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(MobileWebSocketController.class);
    
    private final MobileNotificationService notificationService;

    public MobileWebSocketController(MobileNotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService,
            "MobileNotificationService cannot be null");
    }

    /**
     * Handle worker heartbeat/ping messages
     */
    @MessageMapping("/mobile/ping/{workerId}")
    public void handleWorkerPing(@DestinationVariable String workerId, 
                                @Payload Map<String, Object> pingData) {
        logger.debug("Received ping from worker: {}", workerId);
        
        // Update worker's last seen timestamp
        // Could store this in a cache or database for monitoring
        
        // Echo back with server timestamp
        Map<String, Object> pongData = Map.of(
            "type", "PONG",
            "serverTime", Instant.now(),
            "workerId", workerId
        );
        
        notificationService.sendWorkerUpdate(workerId, "Pong", pongData);
    }

    /**
     * Handle worker status updates
     */
    @MessageMapping("/mobile/status/{workerId}")
    public void handleWorkerStatus(@DestinationVariable String workerId, 
                                  @Payload Map<String, Object> statusData) {
        logger.debug("Received status update from worker: {} - {}", workerId, statusData);
        
        String status = (String) statusData.get("status");
        String location = (String) statusData.get("location");
        
        // Could update worker status in database
        // Notify supervisors of status changes if needed
        
        if ("BREAK".equals(status) || "OFFLINE".equals(status)) {
            logger.info("Worker {} is now {}", workerId, status);
            // Could notify supervisors about worker break/offline status
        }
    }

    /**
     * Handle emergency alerts from workers
     */
    @MessageMapping("/mobile/emergency/{workerId}")
    public void handleEmergencyAlert(@DestinationVariable String workerId, 
                                   @Payload Map<String, Object> emergencyData) {
        logger.warn("EMERGENCY ALERT from worker: {} - {}", workerId, emergencyData);
        
        String reason = (String) emergencyData.get("reason");
        String location = (String) emergencyData.get("location");
        
        // Immediately notify all supervisors
        Map<String, Object> alertData = Map.of(
            "type", "EMERGENCY_ALERT",
            "workerId", workerId,
            "reason", reason != null ? reason : "Unknown",
            "location", location != null ? location : "Unknown",
            "timestamp", Instant.now(),
            "priority", "CRITICAL",
            "requiresResponse", true
        );
        
        notificationService.sendZoneMessage("SUPERVISORS", "Emergency Alert", alertData);
        notificationService.sendZoneMessage("SECURITY", "Emergency Alert", alertData);
        
        // Send acknowledgment back to worker
        Map<String, Object> ackData = Map.of(
            "type", "EMERGENCY_ACK",
            "message", "Emergency alert received. Help is on the way.",
            "timestamp", Instant.now()
        );
        
        notificationService.sendWorkerUpdate(workerId, "Emergency Acknowledged", ackData);
    }

    /**
     * Handle worker subscription to their personal channel
     */
    @SubscribeMapping("/topic/worker/{workerId}")
    public Map<String, Object> handleWorkerSubscription(@DestinationVariable String workerId) {
        logger.info("Worker {} subscribed to personal notifications", workerId);
        
        return Map.of(
            "type", "SUBSCRIPTION_CONFIRMED",
            "message", "Connected to mobile notifications",
            "workerId", workerId,
            "timestamp", Instant.now()
        );
    }

    /**
     * Handle supervisor subscription to zone updates
     */
    @SubscribeMapping("/topic/zone/{zone}")
    public Map<String, Object> handleZoneSubscription(@DestinationVariable String zone) {
        logger.info("Client subscribed to zone notifications: {}", zone);
        
        return Map.of(
            "type", "ZONE_SUBSCRIPTION_CONFIRMED",
            "message", "Connected to " + zone + " notifications",
            "zone", zone,
            "timestamp", Instant.now()
        );
    }

    /**
     * Handle worker location updates (for tracking and routing)
     */
    @MessageMapping("/mobile/location/{workerId}")
    public void handleLocationUpdate(@DestinationVariable String workerId, 
                                   @Payload Map<String, Object> locationData) {
        logger.debug("Location update from worker: {} at {}", 
                    workerId, locationData.get("location"));
        
        // Could update worker location in real-time tracking system
        // Used for routing optimization and emergency response
        
        String location = (String) locationData.get("location");
        Double latitude = (Double) locationData.get("latitude");
        Double longitude = (Double) locationData.get("longitude");
        
        // Store location for routing and analytics
        // Could trigger location-based notifications or optimizations
    }

    /**
     * Broadcast system messages to all connected mobile devices
     */
    @MessageMapping("/mobile/system/broadcast")
    @SendTo("/topic/broadcast")
    public Map<String, Object> broadcastSystemMessage(@Payload Map<String, Object> message) {
        logger.info("Broadcasting system message: {}", message.get("message"));
        
        return Map.of(
            "type", "SYSTEM_BROADCAST",
            "message", message.get("message"),
            "priority", message.getOrDefault("priority", "NORMAL"),
            "timestamp", Instant.now(),
            "sender", "SYSTEM"
        );
    }
}