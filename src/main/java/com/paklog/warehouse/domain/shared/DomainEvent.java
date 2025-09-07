package com.paklog.warehouse.domain.shared;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    Instant getOccurredAt();
}