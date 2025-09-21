package com.paklog.warehouse.domain.putwall;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PutWallMetricsService {

    private final ConcurrentMap<PutWallId, PutWallMetrics> metricsCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> orderStartTimes = new ConcurrentHashMap<>();

    public PutWallMetrics getOrCreateMetrics(PutWallId putWallId) {
        return metricsCache.computeIfAbsent(putWallId, PutWallMetrics::new);
    }

    public void recordOrderAssignment(PutWallId putWallId, String orderId) {
        PutWallMetrics metrics = getOrCreateMetrics(putWallId);
        metrics.recordOrderAssignment();
        orderStartTimes.put(orderId, Instant.now());
    }

    public void recordItemPlacement(PutWallId putWallId) {
        PutWallMetrics metrics = getOrCreateMetrics(putWallId);
        metrics.recordItemPlacement();
    }

    public void recordOrderCompletion(PutWallId putWallId, String orderId) {
        PutWallMetrics metrics = getOrCreateMetrics(putWallId);

        Instant startTime = orderStartTimes.remove(orderId);
        if (startTime != null) {
            Duration completionTime = Duration.between(startTime, Instant.now());
            metrics.recordOrderCompletion(completionTime);
        } else {
            // Fallback if start time not tracked
            metrics.recordOrderCompletion(Duration.ZERO);
        }
    }

    public void updateUtilization(PutWallId putWallId, int activeSlots, int totalSlots) {
        PutWallMetrics metrics = getOrCreateMetrics(putWallId);
        metrics.updateUtilization(activeSlots, totalSlots);
    }

    public void updateThroughput(PutWallId putWallId, Duration timeWindow) {
        PutWallMetrics metrics = getOrCreateMetrics(putWallId);
        metrics.updateThroughput(timeWindow);
    }

    public Optional<PutWallMetrics> getMetrics(PutWallId putWallId) {
        return Optional.ofNullable(metricsCache.get(putWallId));
    }

    public List<PutWallMetrics> getAllMetrics() {
        return List.copyOf(metricsCache.values());
    }

    public void clearMetrics(PutWallId putWallId) {
        metricsCache.remove(putWallId);
        // Clean up any orphaned order start times
        orderStartTimes.entrySet().removeIf(entry ->
            entry.getValue().isBefore(Instant.now().minus(Duration.ofHours(24)))
        );
    }

    public PutWallPerformanceReport generatePerformanceReport(PutWallId putWallId) {
        PutWallMetrics metrics = getOrCreateMetrics(putWallId);

        return new PutWallPerformanceReport(
            putWallId,
            metrics.getTotalOrdersProcessed(),
            metrics.getTotalItemsPlaced(),
            metrics.getAverageOrderCompletionTimeMinutes(),
            metrics.getThroughputOrdersPerHour(),
            metrics.getUtilizationPercentage(),
            metrics.getCurrentActiveOrders(),
            calculateEfficiencyScore(metrics),
            generateRecommendations(metrics)
        );
    }

    private double calculateEfficiencyScore(PutWallMetrics metrics) {
        // Simple efficiency calculation based on utilization and throughput
        double utilizationScore = Math.min(metrics.getUtilizationPercentage() / 85.0, 1.0); // 85% is optimal
        double throughputScore = Math.min(metrics.getThroughputOrdersPerHour() / 50.0, 1.0); // 50 orders/hour baseline

        return (utilizationScore + throughputScore) / 2.0 * 100;
    }

    private String generateRecommendations(PutWallMetrics metrics) {
        StringBuilder recommendations = new StringBuilder();

        if (metrics.getUtilizationPercentage() > 90) {
            recommendations.append("High utilization detected - consider adding more put walls. ");
        } else if (metrics.getUtilizationPercentage() < 50) {
            recommendations.append("Low utilization - review order assignment strategy. ");
        }

        if (metrics.getAverageOrderCompletionTimeMinutes() > 30) {
            recommendations.append("Long completion times - review picking efficiency. ");
        }

        if (metrics.getThroughputOrdersPerHour() < 20) {
            recommendations.append("Low throughput - analyze bottlenecks in the sortation process. ");
        }

        return recommendations.length() > 0 ? recommendations.toString().trim() : "Performance is within acceptable ranges.";
    }

    public static class PutWallPerformanceReport {
        private final PutWallId putWallId;
        private final int totalOrdersProcessed;
        private final int totalItemsPlaced;
        private final double averageOrderCompletionTimeMinutes;
        private final double throughputOrdersPerHour;
        private final double utilizationPercentage;
        private final int currentActiveOrders;
        private final double efficiencyScore;
        private final String recommendations;

        public PutWallPerformanceReport(PutWallId putWallId, int totalOrdersProcessed,
                                      int totalItemsPlaced, double averageOrderCompletionTimeMinutes,
                                      double throughputOrdersPerHour, double utilizationPercentage,
                                      int currentActiveOrders, double efficiencyScore,
                                      String recommendations) {
            this.putWallId = putWallId;
            this.totalOrdersProcessed = totalOrdersProcessed;
            this.totalItemsPlaced = totalItemsPlaced;
            this.averageOrderCompletionTimeMinutes = averageOrderCompletionTimeMinutes;
            this.throughputOrdersPerHour = throughputOrdersPerHour;
            this.utilizationPercentage = utilizationPercentage;
            this.currentActiveOrders = currentActiveOrders;
            this.efficiencyScore = efficiencyScore;
            this.recommendations = recommendations;
        }

        // Getters
        public PutWallId getPutWallId() { return putWallId; }
        public int getTotalOrdersProcessed() { return totalOrdersProcessed; }
        public int getTotalItemsPlaced() { return totalItemsPlaced; }
        public double getAverageOrderCompletionTimeMinutes() { return averageOrderCompletionTimeMinutes; }
        public double getThroughputOrdersPerHour() { return throughputOrdersPerHour; }
        public double getUtilizationPercentage() { return utilizationPercentage; }
        public int getCurrentActiveOrders() { return currentActiveOrders; }
        public double getEfficiencyScore() { return efficiencyScore; }
        public String getRecommendations() { return recommendations; }
    }
}