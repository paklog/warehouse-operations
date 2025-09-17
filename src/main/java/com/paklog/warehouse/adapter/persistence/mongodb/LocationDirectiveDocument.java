package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.location.*;
import com.paklog.warehouse.domain.work.WorkType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "location_directives")
public class LocationDirectiveDocument {
    @Id
    private String id;
    private String directiveId;
    private String name;
    private String description;
    private String workType;
    private String strategy;
    private List<LocationConstraintDocument> constraints;
    private int priority;
    private boolean active;
    private Instant createdAt;
    private Instant lastModifiedAt;
    private String createdBy;
    private int version;

    // Constructors
    public LocationDirectiveDocument() {}

    public LocationDirectiveDocument(LocationDirective directive) {
        this.id = directive.getId().getValue().toString();
        this.directiveId = directive.getId().getValue().toString();
        this.name = directive.getName();
        this.description = directive.getDescription();
        this.workType = directive.getWorkType().name();
        this.strategy = directive.getStrategy().name();
        this.constraints = directive.getConstraints().stream()
            .map(LocationConstraintDocument::new)
            .collect(Collectors.toList());
        this.priority = directive.getPriority();
        this.active = directive.isActive();
        this.createdAt = directive.getCreatedAt();
        this.lastModifiedAt = directive.getLastModifiedAt();
        this.createdBy = directive.getCreatedBy();
        this.version = directive.getVersion();
    }

    public LocationDirective toDomain() {
        LocationDirectiveId directiveId = LocationDirectiveId.of(this.directiveId);
        WorkType workType = WorkType.valueOf(this.workType);
        LocationStrategy strategy = LocationStrategy.valueOf(this.strategy);
        
        List<LocationConstraint> domainConstraints = this.constraints.stream()
            .map(LocationConstraintDocument::toDomain)
            .collect(Collectors.toList());

        return new LocationDirective(
            directiveId, this.name, this.description, workType, strategy,
            domainConstraints, this.priority, this.active,
            this.createdAt, this.lastModifiedAt, this.createdBy, this.version
        );
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDirectiveId() { return directiveId; }
    public void setDirectiveId(String directiveId) { this.directiveId = directiveId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public List<LocationConstraintDocument> getConstraints() { return constraints; }
    public void setConstraints(List<LocationConstraintDocument> constraints) { this.constraints = constraints; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(Instant lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    // Nested document class for constraints
    public static class LocationConstraintDocument {
        private String type;
        private String parameter;
        private String value;
        private boolean enabled;

        public LocationConstraintDocument() {}

        public LocationConstraintDocument(LocationConstraint constraint) {
            this.type = constraint.getType().name();
            this.parameter = constraint.getParameter();
            this.value = constraint.getValue();
            this.enabled = constraint.isEnabled();
        }

        public LocationConstraint toDomain() {
            LocationConstraintType type = LocationConstraintType.valueOf(this.type);
            return new LocationConstraint(type, this.parameter, this.value, this.enabled);
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getParameter() { return parameter; }
        public void setParameter(String parameter) { this.parameter = parameter; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}