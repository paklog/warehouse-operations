package com.paklog.warehouse.domain.quality;

import com.paklog.warehouse.domain.shared.DomainEvent;
import com.paklog.warehouse.domain.shared.SkuCode;

import java.time.Instant;
import java.util.UUID;

// Quality Sample Collected Event
class QualitySampleCollectedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualitySampleId sampleId;
    private final QualitySamplingPlanId samplingPlanId;
    private final String batchNumber;
    private final SkuCode item;
    private final int sampleSize;
    private final Instant collectedAt;

    public QualitySampleCollectedEvent(QualitySampleId sampleId, QualitySamplingPlanId samplingPlanId,
                                     String batchNumber, SkuCode item, int sampleSize, 
                                     Instant collectedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.sampleId = sampleId;
        this.samplingPlanId = samplingPlanId;
        this.batchNumber = batchNumber;
        this.item = item;
        this.sampleSize = sampleSize;
        this.collectedAt = collectedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualitySampleId getSampleId() { return sampleId; }
    public QualitySamplingPlanId getSamplingPlanId() { return samplingPlanId; }
    public String getBatchNumber() { return batchNumber; }
    public SkuCode getItem() { return item; }
    public int getSampleSize() { return sampleSize; }
    public Instant getCollectedAt() { return collectedAt; }
}

// Quality Sample Testing Started Event
class QualitySampleTestingStartedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualitySampleId sampleId;
    private final Instant startedAt;

    public QualitySampleTestingStartedEvent(QualitySampleId sampleId, Instant startedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.sampleId = sampleId;
        this.startedAt = startedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualitySampleId getSampleId() { return sampleId; }
    public Instant getStartedAt() { return startedAt; }
}

// Quality Sample Test Completed Event
class QualitySampleTestCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualitySampleId sampleId;
    private final QualityTestResult testResult;
    private final Instant completedAt;

    public QualitySampleTestCompletedEvent(QualitySampleId sampleId, QualityTestResult testResult,
                                         Instant completedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.sampleId = sampleId;
        this.testResult = testResult;
        this.completedAt = completedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualitySampleId getSampleId() { return sampleId; }
    public QualityTestResult getTestResult() { return testResult; }
    public Instant getCompletedAt() { return completedAt; }
}

// Quality Sample Testing Completed Event
class QualitySampleTestingCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualitySampleId sampleId;
    private final QualitySampleVerdict verdict;
    private final Instant completedAt;

    public QualitySampleTestingCompletedEvent(QualitySampleId sampleId, QualitySampleVerdict verdict,
                                            Instant completedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.sampleId = sampleId;
        this.verdict = verdict;
        this.completedAt = completedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualitySampleId getSampleId() { return sampleId; }
    public QualitySampleVerdict getVerdict() { return verdict; }
    public Instant getCompletedAt() { return completedAt; }
}

// Quality Sample Rejected Event
class QualitySampleRejectedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final QualitySampleId sampleId;
    private final String reason;
    private final Instant rejectedAt;

    public QualitySampleRejectedEvent(QualitySampleId sampleId, String reason, Instant rejectedAt) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.sampleId = sampleId;
        this.reason = reason;
        this.rejectedAt = rejectedAt;
    }

    @Override
    public UUID getEventId() { return eventId; }
    @Override
    public Instant getOccurredAt() { return occurredAt; }

    public QualitySampleId getSampleId() { return sampleId; }
    public String getReason() { return reason; }
    public Instant getRejectedAt() { return rejectedAt; }
}