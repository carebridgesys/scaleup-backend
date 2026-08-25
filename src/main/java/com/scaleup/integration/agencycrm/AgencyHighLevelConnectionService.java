package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.security.SecretEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AgencyHighLevelConnectionService {

    private final AgencyRepository
            agencyRepository;

    private final AgencyHighLevelConnectionRepository
            connectionRepository;

    private final SecretEncryptionService
            secretEncryptionService;

    public AgencyHighLevelConnectionService(
            AgencyRepository agencyRepository,
            AgencyHighLevelConnectionRepository connectionRepository,
            SecretEncryptionService secretEncryptionService
    ) {

        this.agencyRepository =
                agencyRepository;

        this.connectionRepository =
                connectionRepository;

        this.secretEncryptionService =
                secretEncryptionService;
    }

    @Transactional
    public AgencyHighLevelConnection configureConnection(
            UUID agencyPublicId,
            String locationId,
            String accessToken
    ) {

        Agency agency =
                agencyRepository
                        .findByPublicId(
                                agencyPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Agency was not found."
                                )
                        );

        String normalizedLocationId =
                requireText(
                        locationId,
                        "HighLevel location ID"
                );

        String normalizedToken =
                requireText(
                        accessToken,
                        "HighLevel access token"
                );

        String encryptedToken =
                secretEncryptionService
                        .encrypt(
                                normalizedToken
                        );

        AgencyHighLevelConnection connection =
                connectionRepository
                        .findByAgencyPublicId(
                                agencyPublicId
                        )
                        .orElse(null);

        if (connection == null) {

            connection =
                    new AgencyHighLevelConnection(
                            agency,
                            normalizedLocationId,
                            encryptedToken
                    );

        } else {

            connection.updatePrivateToken(
                    normalizedLocationId,
                    encryptedToken
            );
        }

        AgencyHighLevelConnection savedConnection =
                connectionRepository
                        .saveAndFlush(
                                connection
                        );

        /*
         * Keep the existing Agency fields synchronized
         * during the transition to the new connection table.
         */
        agency.configureHighLevelLocation(
                normalizedLocationId
        );

        agency.enableHighLevelSync();

        agencyRepository.save(
                agency
        );

        return savedConnection;
    }

    private String requireText(
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
}