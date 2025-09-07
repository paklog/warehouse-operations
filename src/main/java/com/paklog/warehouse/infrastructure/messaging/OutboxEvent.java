package com.paklog.warehouse.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public class OutboxEvent {
    private final UUID id;
    private final String type;
    private final String source;
    private final String subject;
    private final String data;
    private final Instant createdAt;
    private boolean processed;

    public OutboxEvent(
        String type, 
        String source, 
        String subject, 
        String data
    ) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.source = source;
        this.subject = subject;
        this.data = data;
        this.createdAt = Instant.now();
        this.processed = false;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getSubject() {
        return subject;
    }

    public String getData() {
        return data;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void markProcessed() {
        this.processed = true;
    }
}