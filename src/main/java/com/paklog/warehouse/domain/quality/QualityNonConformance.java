package com.paklog.warehouse.domain.quality;

import java.time.Instant;
import java.util.Objects;

public class QualityNonConformance {
    private final QualityNonConformanceId id;
    private final QualityNonConformanceType type;
    private final String description;
    private final QualitySeverity severity;
    private QualityNonConformanceStatus status;
    private final String identifiedBy;
    private final Instant identifiedAt;
    private String resolution;
    private String closedBy;
    private Instant closedAt;

    public QualityNonConformance(QualityNonConformanceId id, QualityNonConformanceType type,
                                String description, QualitySeverity severity, String identifiedBy) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.identifiedBy = Objects.requireNonNull(identifiedBy, "Identified by cannot be null");
        this.identifiedAt = Instant.now();
        this.status = QualityNonConformanceStatus.OPEN;
    }

    public void close(String resolution, String closedBy) {
        this.status = QualityNonConformanceStatus.CLOSED;
        this.resolution = resolution;
        this.closedBy = closedBy;
        this.closedAt = Instant.now();
    }

    public boolean isOpen() {
        return status == QualityNonConformanceStatus.OPEN;
    }

    // Getters
    public QualityNonConformanceId getId() { return id; }
    public QualityNonConformanceType getType() { return type; }
    public String getDescription() { return description; }
    public QualitySeverity getSeverity() { return severity; }
    public QualityNonConformanceStatus getStatus() { return status; }
    public String getIdentifiedBy() { return identifiedBy; }
    public Instant getIdentifiedAt() { return identifiedAt; }
    public String getResolution() { return resolution; }
    public String getClosedBy() { return closedBy; }
    public Instant getClosedAt() { return closedAt; }
}