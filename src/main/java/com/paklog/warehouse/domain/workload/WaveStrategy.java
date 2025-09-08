package com.paklog.warehouse.domain.workload;

import com.paklog.warehouse.domain.shared.*;
import com.paklog.warehouse.domain.wave.*;
import com.paklog.warehouse.domain.picklist.PickListId;
import com.paklog.warehouse.domain.picklist.PickListCreatedEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WaveStrategy implements IWorkloadReleaseStrategy {
    
    private final List<DomainEvent> events = new ArrayList<>();
    private final int maxOrdersPerWave;
    private final long waveIntervalHours;
    
    public WaveStrategy() {
        this.maxOrdersPerWave = 50; // Default max orders per wave
        this.waveIntervalHours = 4; // Default 4-hour wave intervals
    }
    
    public WaveStrategy(int maxOrdersPerWave, long waveIntervalHours) {
        this.maxOrdersPerWave = maxOrdersPerWave;
        this.waveIntervalHours = waveIntervalHours;
    }

    @Override
    public WorkloadPlan planWork(List<FulfillmentOrder> orders) {
        events.clear();
        
        if (orders.isEmpty()) {
            return new WorkloadPlan(new ArrayList<>());
        }
        
        // Group orders by shipping speed category for wave optimization
        Map<String, List<FulfillmentOrder>> ordersBySpeed = orders.stream()
                .collect(Collectors.groupingBy(FulfillmentOrder::getShippingSpeedCategory));
        
        List<DomainEvent> allEvents = new ArrayList<>();
        
        // Process each shipping speed category
        for (Map.Entry<String, List<FulfillmentOrder>> entry : ordersBySpeed.entrySet()) {
            String shippingSpeed = entry.getKey();
            List<FulfillmentOrder> speedOrders = entry.getValue();
            
            // Create waves for this shipping speed
            List<Wave> waves = createWavesForOrders(speedOrders, shippingSpeed);
            
            // Generate events for each wave
            for (Wave wave : waves) {
                // Create wave created event
                WaveCreatedEvent waveCreatedEvent = new WaveCreatedEvent(
                    wave.getId(), 
                    wave.getOrderIds(),
                    shippingSpeed,
                    Instant.now()
                );
                allEvents.add(waveCreatedEvent);
                
                // Create pick list created events for each order in the wave
                for (OrderId orderId : wave.getOrderIds()) {
                    PickListId pickListId = PickListId.generate();
                    PickListCreatedEvent pickListEvent = new PickListCreatedEvent(pickListId, orderId);
                    allEvents.add(pickListEvent);
                }
            }
        }
        
        events.addAll(allEvents);
        return new WorkloadPlan(allEvents);
    }
    
    private List<Wave> createWavesForOrders(List<FulfillmentOrder> orders, String shippingSpeed) {
        List<Wave> waves = new ArrayList<>();
        List<OrderId> currentWaveOrders = new ArrayList<>();
        
        for (FulfillmentOrder order : orders) {
            currentWaveOrders.add(order.getOrderId());
            
            // Create a new wave when we reach the max orders per wave
            if (currentWaveOrders.size() >= maxOrdersPerWave) {
                Wave wave = createWave(currentWaveOrders, shippingSpeed);
                waves.add(wave);
                currentWaveOrders = new ArrayList<>();
            }
        }
        
        // Create a wave for remaining orders
        if (!currentWaveOrders.isEmpty()) {
            Wave wave = createWave(currentWaveOrders, shippingSpeed);
            waves.add(wave);
        }
        
        return waves;
    }
    
    private Wave createWave(List<OrderId> orderIds, String shippingSpeed) {
        Wave wave = new Wave(orderIds);
        
        // Set carrier and cutoff time based on shipping speed
        if ("express".equalsIgnoreCase(shippingSpeed)) {
            wave.setCutoffTime(Instant.now().plus(2, ChronoUnit.HOURS));
        } else if ("priority".equalsIgnoreCase(shippingSpeed)) {
            wave.setCutoffTime(Instant.now().plus(6, ChronoUnit.HOURS));
        } else {
            // Standard shipping
            wave.setCutoffTime(Instant.now().plus(24, ChronoUnit.HOURS));
        }
        
        return wave;
    }

    @Override
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(events);
    }
    
    // Configuration methods
    public int getMaxOrdersPerWave() {
        return maxOrdersPerWave;
    }
    
    public long getWaveIntervalHours() {
        return waveIntervalHours;
    }
}