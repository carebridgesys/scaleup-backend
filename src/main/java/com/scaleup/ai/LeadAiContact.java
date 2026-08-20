package com.scaleup.ai;

import com.scaleup.lead.Lead;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "lead_ai_contact")
public class LeadAiContact {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AiContactStatus status;

    @Column(
            name = "provider",
            length = 100
    )
    private String provider;

    @Column(
            name = "external_call_id",
            length = 150
    )
    private String externalCallId;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(
            name = "last_error",
            columnDefinition = "TEXT"
    )
    private String lastError;

    @Column(
            name = "transcript_reference",
            length = 500
    )
    private String transcriptReference;

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

    protected LeadAiContact() {
        // Required by JPA.
    }

    public LeadAiContact(
            Lead lead
    ) {
        this.lead = Objects.requireNonNull(
                lead,
                "Lead must not be null."
        );

        this.status =
                AiContactStatus.PENDING;

        this.attemptCount = 0;
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
        updatedAt = LocalDateTime.now();
    }

    public void markInProgress(
            String provider,
            String externalCallId
    ) {

        this.provider =
                normalizeNullableText(provider);

        this.externalCallId =
                normalizeNullableText(
                        externalCallId
                );

        this.status =
                AiContactStatus.IN_PROGRESS;

        this.attemptCount++;

        this.startedAt =
                LocalDateTime.now();

        this.completedAt = null;

        this.lastError = null;
    }

    public void markCompleted(
            String transcriptReference
    ) {

        if (status != AiContactStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "AI contact can only be completed from IN_PROGRESS status."
            );
        }

        this.status =
                AiContactStatus.COMPLETED;

        this.completedAt =
                LocalDateTime.now();

        this.transcriptReference =
                normalizeNullableText(
                        transcriptReference
                );

        this.lastError = null;
    }

    public void markFailed(
            String error
    ) {

        this.status =
                AiContactStatus.FAILED;

        this.lastError =
                normalizeNullableText(error);
    }

    public void markPendingForRetry() {

        this.status =
                AiContactStatus.PENDING;

        this.externalCallId = null;

        this.lastError = null;
    }

    public void cancel() {

        this.status =
                AiContactStatus.CANCELLED;
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

    public AiContactStatus getStatus() {
        return status;
    }

    public String getProvider() {
        return provider;
    }

    public String getExternalCallId() {
        return externalCallId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public String getTranscriptReference() {
        return transcriptReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}