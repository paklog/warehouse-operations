package com.paklog.warehouse.infrastructure.messaging;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends MongoRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByProcessedFalse();
}