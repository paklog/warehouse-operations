package com.paklog.warehouse.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WorkloadPlan {
    private final List<DomainEvent> events;

    public WorkloadPlan(List<DomainEvent> inputEvents) {
        this.events = new ArrayList<>(Objects.requireNonNull(inputEvents, "Events cannot be null"));
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addEvent(DomainEvent event) {
        events.add(Objects.requireNonNull(event, "Event cannot be null"));
    }
}