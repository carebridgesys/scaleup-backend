package com.scaleup.integration.highlevel;

import com.scaleup.lead.LeadType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "highlevel_pipeline_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_highlevel_pipeline_mapping",
                        columnNames = {
                                "location_id",
                                "lead_type"
                        }
                )
        }
)
public class HighLevelPipelineMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "location_id",
            nullable = false,
            length = 150
    )
    private String locationId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "lead_type",
            nullable = false,
            length = 30
    )
    private LeadType leadType;

    @Column(
            name = "pipeline_id",
            nullable = false,
            length = 150
    )
    private String pipelineId;

    @Column(
            name = "initial_stage_id",
            nullable = false,
            length = 150
    )
    private String initialStageId;

    @Column(
            name = "pipeline_name",
            length = 200
    )
    private String pipelineName;

    @Column(
            name = "initial_stage_name",
            length = 200
    )
    private String initialStageName;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected HighLevelPipelineMapping() {
        // Required by JPA.
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getLocationId() {
        return locationId;
    }

    public LeadType getLeadType() {
        return leadType;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public String getInitialStageId() {
        return initialStageId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public String getInitialStageName() {
        return initialStageName;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}