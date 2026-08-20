package com.scaleup.lead;

import com.scaleup.agency.Agency;
import com.scaleup.campaign.Campaign;
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
@Table(name = "leads")
public class Lead {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "lead_type",
            nullable = false,
            length = 30
    )
    private LeadType leadType;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            length = 100
    )
    private String lastName;

    @Column(
            name = "phone",
            nullable = false,
            length = 30
    )
    private String phone;

    @Column(
            name = "email",
            length = 255
    )
    private String email;

    @Column(
            name = "zip_code",
            length = 20
    )
    private String zipCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "preferred_contact_method",
            length = 30
    )
    private PreferredContactMethod preferredContactMethod;

    @Column(
            name = "initial_request_notes",
            length = 2000
    )
    private String initialRequestNotes;

    @Column(
            name = "source",
            length = 100
    )
    private String source;

    @Column(
            name = "campaign_name",
            length = 200
    )
    private String campaignName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private LeadStatus status;

    @Column(
            name = "consent_given",
            nullable = false
    )
    private boolean consentGiven;

    @Column(name = "consent_timestamp")
    private LocalDateTime consentTimestamp;

    @Column(
            name = "highlevel_contact_id",
            length = 150
    )
    private String highLevelContactId;

    @Column(
            name = "highlevel_opportunity_id",
            length = 150
    )
    private String highLevelOpportunityId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "highlevel_sync_status",
            nullable = false,
            length = 30
    )
    private HighLevelSyncStatus highLevelSyncStatus;

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

    protected Lead() {
        // Required by JPA.
    }

    public Lead(
            Agency agency,
            LeadType leadType,
            String firstName,
            String phone
    ) {
        this.agency = Objects.requireNonNull(
                agency,
                "Agency must not be null."
        );

        this.leadType = Objects.requireNonNull(
                leadType,
                "Lead type must not be null."
        );

        this.firstName = requireText(
                firstName,
                "First name"
        );

        this.phone = requireText(
                phone,
                "Phone"
        );

        this.status = LeadStatus.NEW;
        this.consentGiven = false;
        this.highLevelSyncStatus =
                HighLevelSyncStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (publicId == null) {
            publicId = UUID.randomUUID();
        }

        if (status == null) {
            status = LeadStatus.NEW;
        }

        if (highLevelSyncStatus == null) {
            highLevelSyncStatus =
                    HighLevelSyncStatus.PENDING;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateName(
            String firstName,
            String lastName
    ) {
        this.firstName = requireText(
                firstName,
                "First name"
        );

        this.lastName =
                normalizeNullableText(lastName);
    }

    public void updateContactInformation(
            String phone,
            String email
    ) {
        this.phone = requireText(
                phone,
                "Phone"
        );

        this.email =
                normalizeNullableText(email);
    }

    public void updateZipCode(
            String zipCode
    ) {
        this.zipCode =
                normalizeNullableText(zipCode);
    }

    public void updatePreferredContactMethod(
            PreferredContactMethod preferredContactMethod
    ) {
        this.preferredContactMethod =
                preferredContactMethod;
    }

    public void updateInitialRequestNotes(
            String initialRequestNotes
    ) {
        this.initialRequestNotes =
                normalizeNullableText(
                        initialRequestNotes
                );
    }

    public void updateMarketingAttribution(
            String source,
            String campaignName
    ) {
        this.source =
                normalizeNullableText(source);

        this.campaignName =
                normalizeNullableText(campaignName);
    }

    public void recordConsent(
            LocalDateTime consentTimestamp
    ) {
        this.consentGiven = true;

        this.consentTimestamp =
                Objects.requireNonNull(
                        consentTimestamp,
                        "Consent timestamp must not be null."
                );
    }

    public void revokeConsent() {
        this.consentGiven = false;
        this.consentTimestamp = null;
    }

    public void changeStatus(
            LeadStatus status
    ) {
        this.status = Objects.requireNonNull(
                status,
                "Lead status must not be null."
        );
    }

    public void markHighLevelPending() {
        this.highLevelSyncStatus =
                HighLevelSyncStatus.PENDING;
    }

    public void markHighLevelSynced(
            String highLevelContactId,
            String highLevelOpportunityId
    ) {
        this.highLevelContactId = requireText(
                highLevelContactId,
                "HighLevel contact ID"
        );

        this.highLevelOpportunityId =
                normalizeNullableText(
                        highLevelOpportunityId
                );

        this.highLevelSyncStatus =
                HighLevelSyncStatus.SYNCED;
    }

    public void markHighLevelSyncFailed() {
        this.highLevelSyncStatus =
                HighLevelSyncStatus.FAILED;
    }

    public void markHighLevelSyncNotRequired() {
        this.highLevelSyncStatus =
                HighLevelSyncStatus.NOT_REQUIRED;
    }

    public void assignCampaign(
            Campaign campaign
    ) {
        Objects.requireNonNull(
                campaign,
                "Campaign must not be null."
        );

        if (
                !campaign.getAgency()
                        .getPublicId()
                        .equals(agency.getPublicId())
        ) {
            throw new IllegalArgumentException(
                    "Campaign must belong to the same agency as the lead."
            );
        }

        this.campaign = campaign;
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

    public Campaign getCampaign() {
        return campaign;
    }

    public LeadType getLeadType() {
        return leadType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getZipCode() {
        return zipCode;
    }

    public PreferredContactMethod getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public String getInitialRequestNotes() {
        return initialRequestNotes;
    }

    public String getSource() {
        return source;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public boolean isConsentGiven() {
        return consentGiven;
    }

    public LocalDateTime getConsentTimestamp() {
        return consentTimestamp;
    }

    public String getHighLevelContactId() {
        return highLevelContactId;
    }

    public String getHighLevelOpportunityId() {
        return highLevelOpportunityId;
    }

    public HighLevelSyncStatus getHighLevelSyncStatus() {
        return highLevelSyncStatus;
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

        if (!(other instanceof Lead lead)) {
            return false;
        }

        return publicId != null
                && publicId.equals(lead.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(publicId);
    }
}