package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "agency_highlevel_connections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_agency_highlevel_connection_agency",
                        columnNames = "agency_id"
                ),
                @UniqueConstraint(
                        name = "uq_agency_highlevel_connection_location",
                        columnNames = "location_id"
                )
        }
)
public class AgencyHighLevelConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "agency_id",
            nullable = false
    )
    private Agency agency;

    @Column(
            name = "location_id",
            nullable = false,
            length = 150
    )
    private String locationId;

    @Column(
            name = "access_token_encrypted",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String accessTokenEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "auth_type",
            nullable = false,
            length = 30
    )
    private AgencyCrmAuthType authType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "connection_status",
            nullable = false,
            length = 30
    )
    private AgencyCrmConnectionStatus connectionStatus;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(
            name = "refresh_token_encrypted",
            columnDefinition = "TEXT"
    )
    private String refreshTokenEncrypted;

    @Column(
            name = "scopes",
            columnDefinition = "TEXT"
    )
    private String scopes;

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

    protected AgencyHighLevelConnection() {
        // Required by JPA.
    }

    public AgencyHighLevelConnection(
            Agency agency,
            String locationId,
            String accessTokenEncrypted
    ) {

        this.agency =
                Objects.requireNonNull(
                        agency,
                        "Agency must not be null."
                );

        this.locationId =
                requireText(
                        locationId,
                        "HighLevel location ID"
                );

        this.accessTokenEncrypted =
                requireText(
                        accessTokenEncrypted,
                        "Encrypted HighLevel access token"
                );

        this.authType =
                AgencyCrmAuthType.PRIVATE_TOKEN;

        this.connectionStatus =
                AgencyCrmConnectionStatus.ACTIVE;
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

    public void updatePrivateToken(
            String locationId,
            String accessTokenEncrypted
    ) {

        this.locationId =
                requireText(
                        locationId,
                        "HighLevel location ID"
                );

        this.accessTokenEncrypted =
                requireText(
                        accessTokenEncrypted,
                        "Encrypted HighLevel access token"
                );

        this.authType =
                AgencyCrmAuthType.PRIVATE_TOKEN;

        this.tokenExpiresAt = null;
        this.refreshTokenEncrypted = null;

        this.connectionStatus =
                AgencyCrmConnectionStatus.ACTIVE;
    }

    public void markDisconnected() {
        this.connectionStatus =
                AgencyCrmConnectionStatus.DISCONNECTED;
    }

    public void markError() {
        this.connectionStatus =
                AgencyCrmConnectionStatus.ERROR;
    }

    public void activate() {
        this.connectionStatus =
                AgencyCrmConnectionStatus.ACTIVE;
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

    public Long getId() {
        return id;
    }

    public Agency getAgency() {
        return agency;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getAccessTokenEncrypted() {
        return accessTokenEncrypted;
    }

    public AgencyCrmAuthType getAuthType() {
        return authType;
    }

    public AgencyCrmConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public String getRefreshTokenEncrypted() {
        return refreshTokenEncrypted;
    }

    public String getScopes() {
        return scopes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}