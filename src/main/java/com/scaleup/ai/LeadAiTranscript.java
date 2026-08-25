package com.scaleup.ai;

import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
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
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "lead_ai_transcripts")
public class LeadAiTranscript {

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

    @Column(
            name = "provider",
            nullable = false,
            length = 100
    )
    private String provider;

    @Column(
            name = "external_call_id",
            length = 150
    )
    private String externalCallId;

    @Column(
            name = "event_key",
            length = 64
    )
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "agent_type",
            nullable = false,
            length = 30
    )
    private LeadType agentType;

    @Column(
            name = "transcript",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String transcript;

    @Column(
            name = "summary",
            columnDefinition = "TEXT"
    )
    private String summary;

    @Column(
            name = "received_at",
            nullable = false
    )
    private LocalDateTime receivedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected LeadAiTranscript() {
        // Required by JPA.
    }

    public LeadAiTranscript(
            Lead lead,
            String provider,
            String externalCallId,
            String eventKey,
            LeadType agentType,
            String transcript,
            String summary
    ) {

        this.lead =
                Objects.requireNonNull(
                        lead,
                        "Lead must not be null."
                );

        this.provider =
                requireText(
                        provider,
                        "Provider"
                );

        this.externalCallId =
                normalizeNullableText(
                        externalCallId
                );

        this.eventKey =
                requireText(
                        eventKey,
                        "Event key"
                );

        this.agentType =
                Objects.requireNonNull(
                        agentType,
                        "Agent type must not be null."
                );

        this.transcript =
                requireText(
                        transcript,
                        "Transcript"
                );

        this.summary =
                normalizeNullableText(
                        summary
                );

        this.receivedAt =
                LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {

        if (receivedAt == null) {
            receivedAt =
                    LocalDateTime.now();
        }

        if (createdAt == null) {
            createdAt =
                    LocalDateTime.now();
        }
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
                    fieldName
                            + " must not be blank."
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

    public Lead getLead() {
        return lead;
    }

    public String getProvider() {
        return provider;
    }

    public String getExternalCallId() {
        return externalCallId;
    }

    public String getEventKey() {
        return eventKey;
    }

    public LeadType getAgentType() {
        return agentType;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}