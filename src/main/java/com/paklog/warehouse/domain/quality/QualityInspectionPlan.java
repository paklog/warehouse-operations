package com.paklog.warehouse.domain.quality;

import java.util.List;
import java.util.Objects;

public class QualityInspectionPlan {
    private final String planId;
    private final String name;
    private final String description;
    private final List<QualityStepTemplate> stepTemplates;
    private final boolean active;

    public QualityInspectionPlan(String planId, String name, String description,
                                List<QualityStepTemplate> stepTemplates, boolean active) {
        this.planId = Objects.requireNonNull(planId, "Plan ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.stepTemplates = Objects.requireNonNull(stepTemplates, "Step templates cannot be null");
        this.active = active;
    }

    // Getters
    public String getPlanId() { return planId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<QualityStepTemplate> getStepTemplates() { return stepTemplates; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityInspectionPlan that = (QualityInspectionPlan) o;
        return Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId);
    }
}