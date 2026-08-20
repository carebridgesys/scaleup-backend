package com.scaleup.agency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "agencies")
public class Agency {

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

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "slug",
            nullable = false,
            unique = true,
            length = 120
    )
    private String slug;

    @Column(
            name = "highlevel_location_id",
            length = 150
    )
    private String highLevelLocationId;

    @Column(
            name = "highlevel_sync_enabled",
            nullable = false
    )
    private boolean highLevelSyncEnabled;

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

    protected Agency() {
        // Required by JPA.
    }

    public Agency(
            String name,
            String slug
    ) {
        this.name = requireText(name, "Agency name");
        this.slug = requireText(slug, "Agency slug");
        this.highLevelSyncEnabled = false;
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
        this.name = requireText(name, "Agency name");
    }

    public void updateSlug(String slug) {
        this.slug = requireText(slug, "Agency slug");
    }

    public void configureHighLevelLocation(
            String locationId
    ) {
        this.highLevelLocationId =
                normalizeNullableText(locationId);
    }

    public void enableHighLevelSync() {
        if (highLevelLocationId == null) {
            throw new IllegalStateException(
                    "HighLevel sync cannot be enabled without a HighLevel location ID."
            );
        }

        this.highLevelSyncEnabled = true;
    }

    public void disableHighLevelSync() {
        this.highLevelSyncEnabled = false;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
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

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getHighLevelLocationId() {
        return highLevelLocationId;
    }

    public boolean isHighLevelSyncEnabled() {
        return highLevelSyncEnabled;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Agency agency)) {
            return false;
        }

        return publicId != null
                && publicId.equals(agency.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(publicId);
    }
}