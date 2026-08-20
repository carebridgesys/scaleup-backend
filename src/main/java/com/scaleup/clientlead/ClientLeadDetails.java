package com.scaleup.clientlead;

import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "client_lead_details")
public class ClientLeadDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "lead_id",
            nullable = false,
            unique = true
    )
    private Lead lead;

    @Column(
            name = "service_needed",
            length = 100
    )
    private String serviceNeeded;

    @Column(
            name = "care_start_timeline",
            length = 100
    )
    private String careStartTimeline;

    @Column(
            name = "payer_type",
            length = 100
    )
    private String payerType;

    @Column(
            name = "decision_maker",
            length = 100
    )
    private String decisionMaker;

    @Column(
            name = "ai_qualification_score"
    )
    private Integer aiQualificationScore;

    @Column(
            name = "ai_summary",
            columnDefinition = "TEXT"
    )
    private String aiSummary;

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

    protected ClientLeadDetails() {
        // Required by JPA.
    }

    public ClientLeadDetails(Lead lead) {
        this.lead = Objects.requireNonNull(
                lead,
                "Lead must not be null."
        );

        if (lead.getLeadType() != LeadType.CLIENT) {
            throw new IllegalArgumentException(
                    "ClientLeadDetails can only be created for CLIENT leads."
            );
        }
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

    public void updateIntakeInformation(
            String serviceNeeded,
            String careStartTimeline,
            String payerType,
            String decisionMaker
    ) {
        this.serviceNeeded =
                normalizeNullableText(serviceNeeded);

        this.careStartTimeline =
                normalizeNullableText(careStartTimeline);

        this.payerType =
                normalizeNullableText(payerType);

        this.decisionMaker =
                normalizeNullableText(decisionMaker);
    }

    public void updateServiceNeeded(
            String serviceNeeded
    ) {
        this.serviceNeeded =
                normalizeNullableText(serviceNeeded);
    }

    public void updateCareStartTimeline(
            String careStartTimeline
    ) {
        this.careStartTimeline =
                normalizeNullableText(careStartTimeline);
    }

    public void updatePayerType(
            String payerType
    ) {
        this.payerType =
                normalizeNullableText(payerType);
    }

    public void updateDecisionMaker(
            String decisionMaker
    ) {
        this.decisionMaker =
                normalizeNullableText(decisionMaker);
    }

    public void updateAiQualification(
            Integer score,
            String summary
    ) {
        validateScore(score);

        this.aiQualificationScore = score;
        this.aiSummary =
                normalizeNullableText(summary);
    }

    public void clearAiQualification() {
        this.aiQualificationScore = null;
        this.aiSummary = null;
    }

    private static void validateScore(
            Integer score
    ) {
        if (
                score != null
                        && (score < 0 || score > 100)
        ) {
            throw new IllegalArgumentException(
                    "AI qualification score must be between 0 and 100."
            );
        }
    }

    private static String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public Lead getLead() {
        return lead;
    }

    public String getServiceNeeded() {
        return serviceNeeded;
    }

    public String getCareStartTimeline() {
        return careStartTimeline;
    }

    public String getPayerType() {
        return payerType;
    }

    public String getDecisionMaker() {
        return decisionMaker;
    }

    public Integer getAiQualificationScore() {
        return aiQualificationScore;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}