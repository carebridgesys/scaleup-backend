package com.scaleup.integration;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "lead_crm_sync",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_lead_crm_sync_destination",
                        columnNames = {
                                "lead_id",
                                "destination"
                        }
                )
        }
)
public class LeadCrmSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "lead_id",
            nullable = false
    )
    private Lead lead;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "destination",
            nullable = false,
            length = 30
    )
    private CrmDestination destination;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "sync_status",
            nullable = false,
            length = 30
    )
    private CrmSyncStatus syncStatus;

    @Column(
            name = "external_contact_id",
            length = 150
    )
    private String externalContactId;

    @Column(
            name = "external_opportunity_id",
            length = 150
    )
    private String externalOpportunityId;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(
            name = "last_error",
            columnDefinition = "TEXT"
    )
    private String lastError;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

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

    protected LeadCrmSync() {
        // Required by JPA.
    }

    public LeadCrmSync(
            Lead lead,
            CrmDestination destination,
            CrmSyncStatus initialStatus
    ) {
        this.lead = Objects.requireNonNull(
                lead,
                "Lead must not be null."
        );

        this.destination = Objects.requireNonNull(
                destination,
                "CRM destination must not be null."
        );

        this.syncStatus = Objects.requireNonNull(
                initialStatus,
                "CRM sync status must not be null."
        );

        this.attemptCount = 0;
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

    public void markPending() {
        this.syncStatus = CrmSyncStatus.PENDING;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void markProcessing() {
        this.syncStatus = CrmSyncStatus.PROCESSING;
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
        this.lastError = null;
    }

    public void markSynced(
            String externalContactId,
            String externalOpportunityId
    ) {
        this.externalContactId =
                normalizeNullableText(externalContactId);

        this.externalOpportunityId =
                normalizeNullableText(
                        externalOpportunityId
                );

        this.syncStatus =
                CrmSyncStatus.SYNCED;

        this.syncedAt =
                LocalDateTime.now();

        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void markFailed(
            String error,
            LocalDateTime nextAttemptAt
    ) {
        this.syncStatus =
                CrmSyncStatus.FAILED;

        this.lastError =
                normalizeNullableText(error);

        this.nextAttemptAt =
                nextAttemptAt;
    }

    public void markNotRequired() {
        this.syncStatus =
                CrmSyncStatus.NOT_REQUIRED;

        this.nextAttemptAt = null;
        this.lastError = null;
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

    public CrmDestination getDestination() {
        return destination;
    }

    public CrmSyncStatus getSyncStatus() {
        return syncStatus;
    }

    public String getExternalContactId() {
        return externalContactId;
    }

    public String getExternalOpportunityId() {
        return externalOpportunityId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}