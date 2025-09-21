package com.paklog.warehouse.adapter.rest;

import com.paklog.warehouse.domain.putwall.PutWallId;
import com.paklog.warehouse.domain.putwall.PutWallMetrics;
import com.paklog.warehouse.domain.putwall.PutWallMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/putwalls/metrics")
@Tag(name = "Put Wall Metrics", description = "REST API for put wall performance metrics and KPIs")
public class PutWallMetricsController {

    private final PutWallMetricsService metricsService;

    public PutWallMetricsController(PutWallMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/{putWallId}")
    @Operation(summary = "Get put wall metrics", description = "Retrieves current metrics for a specific put wall")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Put wall not found")
    })
    public ResponseEntity<PutWallMetricsResponse> getMetrics(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId) {

        Optional<PutWallMetrics> metrics = metricsService.getMetrics(PutWallId.of(putWallId));

        return metrics.map(m -> ResponseEntity.ok(PutWallMetricsResponse.fromDomain(m)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all put wall metrics", description = "Retrieves metrics for all put walls")
    public ResponseEntity<List<PutWallMetricsResponse>> getAllMetrics() {
        List<PutWallMetrics> allMetrics = metricsService.getAllMetrics();
        List<PutWallMetricsResponse> responses = allMetrics.stream()
            .map(PutWallMetricsResponse::fromDomain)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{putWallId}/performance-report")
    @Operation(summary = "Get performance report", description = "Generates a comprehensive performance report for a put wall")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Performance report generated successfully"),
        @ApiResponse(responseCode = "404", description = "Put wall not found")
    })
    public ResponseEntity<PerformanceReportResponse> getPerformanceReport(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId) {

        try {
            PutWallMetricsService.PutWallPerformanceReport report =
                metricsService.generatePerformanceReport(PutWallId.of(putWallId));

            return ResponseEntity.ok(PerformanceReportResponse.fromDomain(report));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{putWallId}")
    @Operation(summary = "Clear put wall metrics", description = "Clears all metrics for a specific put wall")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Metrics cleared successfully"),
        @ApiResponse(responseCode = "404", description = "Put wall not found")
    })
    public ResponseEntity<Void> clearMetrics(
            @PathVariable @Parameter(description = "Put wall ID") String putWallId) {

        metricsService.clearMetrics(PutWallId.of(putWallId));
        return ResponseEntity.noContent().build();
    }

    // Response DTOs
    public static class PutWallMetricsResponse {
        private final String putWallId;
        private final int totalOrdersProcessed;
        private final int totalItemsPlaced;
        private final double averageOrderCompletionTimeMinutes;
        private final double throughputOrdersPerHour;
        private final double utilizationPercentage;
        private final int currentActiveOrders;
        private final String lastUpdated;

        private PutWallMetricsResponse(String putWallId, int totalOrdersProcessed, int totalItemsPlaced,
                                     double averageOrderCompletionTimeMinutes, double throughputOrdersPerHour,
                                     double utilizationPercentage, int currentActiveOrders, String lastUpdated) {
            this.putWallId = putWallId;
            this.totalOrdersProcessed = totalOrdersProcessed;
            this.totalItemsPlaced = totalItemsPlaced;
            this.averageOrderCompletionTimeMinutes = averageOrderCompletionTimeMinutes;
            this.throughputOrdersPerHour = throughputOrdersPerHour;
            this.utilizationPercentage = utilizationPercentage;
            this.currentActiveOrders = currentActiveOrders;
            this.lastUpdated = lastUpdated;
        }

        public static PutWallMetricsResponse fromDomain(PutWallMetrics metrics) {
            return new PutWallMetricsResponse(
                metrics.getPutWallId().toString(),
                metrics.getTotalOrdersProcessed(),
                metrics.getTotalItemsPlaced(),
                metrics.getAverageOrderCompletionTimeMinutes(),
                metrics.getThroughputOrdersPerHour(),
                metrics.getUtilizationPercentage(),
                metrics.getCurrentActiveOrders(),
                metrics.getLastUpdated().toString()
            );
        }

        // Getters
        public String getPutWallId() { return putWallId; }
        public int getTotalOrdersProcessed() { return totalOrdersProcessed; }
        public int getTotalItemsPlaced() { return totalItemsPlaced; }
        public double getAverageOrderCompletionTimeMinutes() { return averageOrderCompletionTimeMinutes; }
        public double getThroughputOrdersPerHour() { return throughputOrdersPerHour; }
        public double getUtilizationPercentage() { return utilizationPercentage; }
        public int getCurrentActiveOrders() { return currentActiveOrders; }
        public String getLastUpdated() { return lastUpdated; }
    }

    public static class PerformanceReportResponse {
        private final String putWallId;
        private final int totalOrdersProcessed;
        private final int totalItemsPlaced;
        private final double averageOrderCompletionTimeMinutes;
        private final double throughputOrdersPerHour;
        private final double utilizationPercentage;
        private final int currentActiveOrders;
        private final double efficiencyScore;
        private final String recommendations;

        private PerformanceReportResponse(String putWallId, int totalOrdersProcessed, int totalItemsPlaced,
                                        double averageOrderCompletionTimeMinutes, double throughputOrdersPerHour,
                                        double utilizationPercentage, int currentActiveOrders,
                                        double efficiencyScore, String recommendations) {
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

        public static PerformanceReportResponse fromDomain(PutWallMetricsService.PutWallPerformanceReport report) {
            return new PerformanceReportResponse(
                report.getPutWallId().toString(),
                report.getTotalOrdersProcessed(),
                report.getTotalItemsPlaced(),
                report.getAverageOrderCompletionTimeMinutes(),
                report.getThroughputOrdersPerHour(),
                report.getUtilizationPercentage(),
                report.getCurrentActiveOrders(),
                report.getEfficiencyScore(),
                report.getRecommendations()
            );
        }

        // Getters
        public String getPutWallId() { return putWallId; }
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