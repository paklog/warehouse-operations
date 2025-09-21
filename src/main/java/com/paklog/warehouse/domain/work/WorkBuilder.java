package com.paklog.warehouse.domain.work;

import com.paklog.warehouse.domain.shared.BinLocation;
import com.paklog.warehouse.domain.shared.SkuCode;
import com.paklog.warehouse.domain.shared.Quantity;

import java.util.List;
import java.util.Objects;

public class WorkBuilder {
    private WorkTemplate template;
    private BinLocation location;
    private SkuCode item;
    private Quantity quantity;
    private String assignedTo;

    private WorkBuilder(WorkTemplate template) {
        this.template = Objects.requireNonNull(template, "Template cannot be null");
    }

    public static WorkBuilder fromTemplate(WorkTemplate template) {
        return new WorkBuilder(template);
    }

    public WorkBuilder withLocation(BinLocation location) {
        this.location = location;
        return this;
    }

    public WorkBuilder withItem(SkuCode item) {
        this.item = item;
        return this;
    }

    public WorkBuilder withQuantity(Quantity quantity) {
        this.quantity = quantity;
        return this;
    }

    public WorkBuilder withAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
        return this;
    }

    public Work build() {
        Objects.requireNonNull(location, "Location must be specified");
        Objects.requireNonNull(item, "Item must be specified");
        Objects.requireNonNull(quantity, "Quantity must be specified");

        if (!template.isActive()) {
            throw new IllegalStateException("Cannot build work from inactive template");
        }

        List<WorkStep> steps = template.getSteps();
        if (steps.isEmpty()) {
            throw new IllegalStateException("Template must have at least one step");
        }

        Work work = new Work(template.getId(), template.getWorkType(), location, item, quantity, steps, 
                           template.isQualityInspectionRequired(), 
                           template.getRequiredInspectionType());

        if (assignedTo != null) {
            work.assignTo(assignedTo);
        }

        return work;
    }
}