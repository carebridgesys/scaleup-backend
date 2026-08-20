package com.scaleup.campaign;

import com.scaleup.agency.Agency;
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

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "public_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID publicId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "agency_id",
            nullable = false
    )
    private Agency agency;

    @Column(
            name = "name",
            nullable = false,
            length = 200
    )
    private String name;

    @Column(
            name = "slug",
            nullable = false,
            length = 150
    )
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "campaign_type",
            nullable = false,
            length = 30
    )
    private CampaignType campaignType;

    @Column(
            name = "source",
            length = 100
    )
    private String source;

    @Column(
            name = "external_campaign_id",
            length = 150
    )
    private String externalCampaignId;

    @Column(
            name = "landing_page_key",
            nullable = false,
            unique = true,
            length = 150
    )
    private String landingPageKey;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

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

    protected Campaign() {
        // Required by JPA.
    }

    public Campaign(
            Agency agency,
            String name,
            String slug,
            CampaignType campaignType,
            String landingPageKey
    ) {
        this.agency = Objects.requireNonNull(
                agency,
                "Agency must not be null."
        );

        this.name = requireText(
                name,
                "Campaign name"
        );

        this.slug = requireText(
                slug,
                "Campaign slug"
        );

        this.campaignType = Objects.requireNonNull(
                campaignType,
                "Campaign type must not be null."
        );

        this.landingPageKey = requireText(
                landingPageKey,
                "Landing page key"
        );

        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (publicId == null) {
            publicId = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = requireText(
                name,
                "Campaign name"
        );
    }

    public void updateSlug(String slug) {
        this.slug = requireText(
                slug,
                "Campaign slug"
        );
    }

    public void updateAttribution(
            String source,
            String externalCampaignId
    ) {
        this.source =
                normalizeNullableText(source);

        this.externalCampaignId =
                normalizeNullableText(externalCampaignId);
    }

    public void updateSchedule(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (
                startDate != null
                        && endDate != null
                        && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "Campaign end date cannot be before start date."
            );
        }

        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isCurrentlyActive(
            LocalDateTime now
    ) {
        if (!active) {
            return false;
        }

        if (
                startDate != null
                        && now.isBefore(startDate)
        ) {
            return false;
        }

        return endDate == null
                || !now.isAfter(endDate);
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return value.trim();
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

    public UUID getPublicId() {
        return publicId;
    }

    public Agency getAgency() {
        return agency;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public CampaignType getCampaignType() {
        return campaignType;
    }

    public String getSource() {
        return source;
    }

    public String getExternalCampaignId() {
        return externalCampaignId;
    }

    public String getLandingPageKey() {
        return landingPageKey;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Campaign campaign)) {
            return false;
        }

        return publicId != null
                && publicId.equals(campaign.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(publicId);
    }
}