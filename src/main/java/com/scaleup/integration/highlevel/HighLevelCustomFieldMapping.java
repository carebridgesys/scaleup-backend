package com.scaleup.integration.highlevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "highlevel_custom_field_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_highlevel_custom_field_mapping",
                        columnNames = {
                                "location_id",
                                "field_key"
                        }
                )
        }
)
public class HighLevelCustomFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "location_id",
            nullable = false,
            length = 150
    )
    private String locationId;

    @Column(
            name = "field_key",
            nullable = false,
            length = 255
    )
    private String fieldKey;

    @Column(
            name = "external_field_id",
            nullable = false,
            length = 150
    )
    private String externalFieldId;

    @Column(
            name = "field_name",
            length = 255
    )
    private String fieldName;

    @Column(
            name = "data_type",
            length = 100
    )
    private String dataType;

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

    protected HighLevelCustomFieldMapping() {
        // Required by JPA.
    }

    public HighLevelCustomFieldMapping(
            String locationId,
            String fieldKey,
            String externalFieldId,
            String fieldName,
            String dataType
    ) {
        this.locationId =
                requireText(locationId, "Location ID");

        this.fieldKey =
                requireText(fieldKey, "Field key");

        this.externalFieldId =
                requireText(
                        externalFieldId,
                        "External field ID"
                );

        this.fieldName =
                normalizeNullableText(fieldName);

        this.dataType =
                normalizeNullableText(dataType);

        this.active = true;
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

    public void updateExternalDefinition(
            String externalFieldId,
            String fieldName,
            String dataType
    ) {
        this.externalFieldId =
                requireText(
                        externalFieldId,
                        "External field ID"
                );

        this.fieldName =
                normalizeNullableText(fieldName);

        this.dataType =
                normalizeNullableText(dataType);

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

    public String getLocationId() {
        return locationId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getExternalFieldId() {
        return externalFieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDataType() {
        return dataType;
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