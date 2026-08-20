package com.scaleup.caregiverlead;

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
@Table(name = "caregiver_lead_details")
public class CaregiverLeadDetails {

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
            name = "years_experience"
    )
    private Integer yearsExperience;

    @Column(
            name = "certifications",
            columnDefinition = "TEXT"
    )
    private String certifications;

    @Column(
            name = "availability",
            length = 100
    )
    private String availability;

    @Column(
            name = "transportation",
            length = 100
    )
    private String transportation;

    @Column(
            name = "preferred_schedule",
            length = 100
    )
    private String preferredSchedule;

    @Column(
            name = "desired_hours_per_week"
    )
    private Integer desiredHoursPerWeek;

    @Column(
            name = "service_area",
            length = 255
    )
    private String serviceArea;

    @Column(
            name = "background_check_status",
            length = 100
    )
    private String backgroundCheckStatus;

    @Column(
            name = "interview_status",
            length = 100
    )
    private String interviewStatus;

    @Column(
            name = "ai_screening_score"
    )
    private Integer aiScreeningScore;

    @Column(
            name = "ai_screening_summary",
            columnDefinition = "TEXT"
    )
    private String aiScreeningSummary;

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

    protected CaregiverLeadDetails() {
        // Required by JPA.
    }

    public CaregiverLeadDetails(Lead lead) {
        this.lead = Objects.requireNonNull(
                lead,
                "Lead must not be null."
        );

        if (lead.getLeadType() != LeadType.CAREGIVER) {
            throw new IllegalArgumentException(
                    "CaregiverLeadDetails can only be created for CAREGIVER leads."
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

    public void updateExperience(
            Integer yearsExperience,
            String certifications
    ) {
        validateYearsExperience(yearsExperience);

        this.yearsExperience = yearsExperience;
        this.certifications =
                normalizeNullableText(certifications);
    }

    public void updateAvailability(
            String availability,
            String preferredSchedule,
            Integer desiredHoursPerWeek
    ) {
        validateDesiredHours(desiredHoursPerWeek);

        this.availability =
                normalizeNullableText(availability);

        this.preferredSchedule =
                normalizeNullableText(preferredSchedule);

        this.desiredHoursPerWeek =
                desiredHoursPerWeek;
    }

    public void updateTransportation(
            String transportation
    ) {
        this.transportation =
                normalizeNullableText(transportation);
    }

    public void updateServiceArea(
            String serviceArea
    ) {
        this.serviceArea =
                normalizeNullableText(serviceArea);
    }

    public void updateBackgroundCheckStatus(
            String backgroundCheckStatus
    ) {
        this.backgroundCheckStatus =
                normalizeNullableText(
                        backgroundCheckStatus
                );
    }

    public void updateInterviewStatus(
            String interviewStatus
    ) {
        this.interviewStatus =
                normalizeNullableText(interviewStatus);
    }

    public void updateAiScreening(
            Integer score,
            String summary
    ) {
        validateScore(score);

        this.aiScreeningScore = score;

        this.aiScreeningSummary =
                normalizeNullableText(summary);
    }

    public void clearAiScreening() {
        this.aiScreeningScore = null;
        this.aiScreeningSummary = null;
    }

    private static void validateYearsExperience(
            Integer yearsExperience
    ) {
        if (
                yearsExperience != null
                        && yearsExperience < 0
        ) {
            throw new IllegalArgumentException(
                    "Years of experience cannot be negative."
            );
        }
    }

    private static void validateDesiredHours(
            Integer desiredHoursPerWeek
    ) {
        if (
                desiredHoursPerWeek != null
                        && (
                        desiredHoursPerWeek < 0
                                || desiredHoursPerWeek > 168
                )
        ) {
            throw new IllegalArgumentException(
                    "Desired hours per week must be between 0 and 168."
            );
        }
    }

    private static void validateScore(
            Integer score
    ) {
        if (
                score != null
                        && (score < 0 || score > 100)
        ) {
            throw new IllegalArgumentException(
                    "AI screening score must be between 0 and 100."
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

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public String getCertifications() {
        return certifications;
    }

    public String getAvailability() {
        return availability;
    }

    public String getTransportation() {
        return transportation;
    }

    public String getPreferredSchedule() {
        return preferredSchedule;
    }

    public Integer getDesiredHoursPerWeek() {
        return desiredHoursPerWeek;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public String getBackgroundCheckStatus() {
        return backgroundCheckStatus;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public Integer getAiScreeningScore() {
        return aiScreeningScore;
    }

    public String getAiScreeningSummary() {
        return aiScreeningSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}