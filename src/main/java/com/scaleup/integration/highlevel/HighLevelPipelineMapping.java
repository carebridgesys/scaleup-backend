package com.scaleup.integration.highlevel;

import com.scaleup.agency.Agency;
import com.scaleup.lead.LeadType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "highlevel_pipeline_mapping")
public class HighLevelPipelineMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

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
            name = "attempting_contact_stage_id",
            length = 150
    )
    private String attemptingContactStageId;

    @Column(
            name = "contacted_stage_id",
            length = 150
    )
    private String contactedStageId;

    @Column(
            name = "qualified_stage_id",
            length = 150
    )
    private String qualifiedStageId;

    @Column(
            name = "routed_stage_id",
            length = 150
    )
    private String routedStageId;

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

    public HighLevelPipelineMapping(
            Agency agency,
            String locationId,
            LeadType leadType,
            String pipelineId,
            String initialStageId,
            String pipelineName,
            String initialStageName
    ) {

        this.agency = agency;

        this.locationId =
                requireText(
                        locationId,
                        "HighLevel location ID"
                );

        this.leadType =
                Objects.requireNonNull(
                        leadType,
                        "Lead type must not be null."
                );

        this.pipelineId =
                requireText(
                        pipelineId,
                        "Pipeline ID"
                );

        this.initialStageId =
                requireText(
                        initialStageId,
                        "Initial stage ID"
                );

        this.pipelineName =
                normalizeNullableText(
                        pipelineName
                );

        this.initialStageName =
                normalizeNullableText(
                        initialStageName
                );

        this.active = true;
    }

    public void updateMapping(
            String pipelineId,
            String initialStageId,
            String pipelineName,
            String initialStageName
    ) {

        this.pipelineId =
                requireText(
                        pipelineId,
                        "Pipeline ID"
                );

        this.initialStageId =
                requireText(
                        initialStageId,
                        "Initial stage ID"
                );

        this.pipelineName =
                normalizeNullableText(
                        pipelineName
                );

        this.initialStageName =
                normalizeNullableText(
                        initialStageName
                );

        this.active = true;
    }

    public void updateLifecycleStages(
            String attemptingContactStageId,
            String contactedStageId,
            String qualifiedStageId,
            String routedStageId
    ) {

        this.attemptingContactStageId =
                normalizeNullableText(
                        attemptingContactStageId
                );

        this.contactedStageId =
                normalizeNullableText(
                        contactedStageId
                );

        this.qualifiedStageId =
                normalizeNullableText(
                        qualifiedStageId
                );

        this.routedStageId =
                normalizeNullableText(
                        routedStageId
                );
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt =
                LocalDateTime.now();
    }

    private static String requireText(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return value.trim();
    }

    private static String normalizeNullableText(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public Agency getAgency() {
        return agency;
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

    public String getAttemptingContactStageId() {
        return attemptingContactStageId;
    }

    public String getContactedStageId() {
        return contactedStageId;
    }

    public String getQualifiedStageId() {
        return qualifiedStageId;
    }

    public String getRoutedStageId() {
        return routedStageId;
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