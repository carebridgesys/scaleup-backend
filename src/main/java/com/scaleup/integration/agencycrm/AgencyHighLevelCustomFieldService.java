package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import com.scaleup.integration.highlevel.HighLevelCustomFieldMapping;
import com.scaleup.integration.highlevel.HighLevelCustomFieldMappingRepository;
import com.scaleup.integration.highlevel.dto.HighLevelCustomFieldsResponse;
import com.scaleup.security.SecretEncryptionService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgencyHighLevelCustomFieldService {

    private static final Map<String, String> CLIENT_FIELD_KEYS =
            createClientFieldKeys();

    private static final Map<String, String> CAREGIVER_FIELD_KEYS =
            createCaregiverFieldKeys();

    private final AgencyRepository agencyRepository;

    private final AgencyHighLevelConnectionRepository
            connectionRepository;

    private final HighLevelCustomFieldMappingRepository
            customFieldMappingRepository;

    private final SecretEncryptionService
            secretEncryptionService;

    private final RestClient
            highLevelRestClient;

    public AgencyHighLevelCustomFieldService(
            AgencyRepository agencyRepository,
            AgencyHighLevelConnectionRepository connectionRepository,
            HighLevelCustomFieldMappingRepository customFieldMappingRepository,
            SecretEncryptionService secretEncryptionService,
            RestClient highLevelRestClient
    ) {
        this.agencyRepository = agencyRepository;
        this.connectionRepository = connectionRepository;
        this.customFieldMappingRepository =
                customFieldMappingRepository;
        this.secretEncryptionService =
                secretEncryptionService;
        this.highLevelRestClient =
                highLevelRestClient;
    }

    @Transactional
    public CustomFieldSyncResult syncClientFields(
            UUID agencyPublicId
    ) {

        return syncFields(
                agencyPublicId,
                CLIENT_FIELD_KEYS
        );
    }

    @Transactional
    public CustomFieldSyncResult syncCaregiverFields(
            UUID agencyPublicId
    ) {

        return syncFields(
                agencyPublicId,
                CAREGIVER_FIELD_KEYS
        );
    }

    private CustomFieldSyncResult syncFields(
            UUID agencyPublicId,
            Map<String, String> expectedFields
    ) {

        Agency agency =
                agencyRepository
                        .findByPublicId(agencyPublicId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Agency was not found."
                                )
                        );

        AgencyHighLevelConnection connection =
                connectionRepository
                        .findByAgencyPublicId(
                                agency.getPublicId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HighLevel connection was not found for agency."
                                )
                        );

        if (
                connection.getConnectionStatus()
                        != AgencyCrmConnectionStatus.ACTIVE
        ) {
            throw new IllegalStateException(
                    "HighLevel connection is not active for agency."
            );
        }

        String locationId =
                requireText(
                        connection.getLocationId(),
                        "HighLevel location ID is missing."
                );

        String token =
                requireText(
                        secretEncryptionService.decrypt(
                                connection.getAccessTokenEncrypted()
                        ),
                        "HighLevel access token is missing."
                );

        HighLevelCustomFieldsResponse response =
                highLevelRestClient
                        .get()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .path(
                                                "/locations/{locationId}/customFields"
                                        )
                                        .queryParam(
                                                "model",
                                                "contact"
                                        )
                                        .build(
                                                locationId
                                        )
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .retrieve()
                        .body(
                                HighLevelCustomFieldsResponse.class
                        );

        if (
                response == null
                        || response.customFields() == null
        ) {
            throw new IllegalStateException(
                    "HighLevel did not return custom fields."
            );
        }

        List<HighLevelCustomFieldsResponse.HighLevelCustomFieldDefinition>
                highLevelFields =
                response.customFields();

        Map<String, String> matched =
                new LinkedHashMap<>();

        Map<String, String> missing =
                new LinkedHashMap<>();

        for (
                Map.Entry<String, String> expected :
                expectedFields.entrySet()
        ) {

            String logicalFieldKey =
                    expected.getKey();

            String expectedFieldName =
                    expected.getValue();

            HighLevelCustomFieldsResponse.HighLevelCustomFieldDefinition
                    externalField =
                    findByName(
                            highLevelFields,
                            expectedFieldName
                    );

            if (externalField == null) {

                missing.put(
                        logicalFieldKey,
                        expectedFieldName
                );

                continue;
            }

            String externalFieldId =
                    requireText(
                            externalField.id(),
                            "HighLevel custom field ID is missing for "
                                    + expectedFieldName
                    );

            HighLevelCustomFieldMapping mapping =
                    customFieldMappingRepository
                            .findByLocationIdAndFieldKeyAndActiveTrue(
                                    locationId,
                                    logicalFieldKey
                            )
                            .orElse(null);

            if (mapping == null) {

                mapping =
                        customFieldMappingRepository
                                .findAll()
                                .stream()
                                .filter(existing ->
                                        locationId.equals(
                                                existing.getLocationId()
                                        )
                                                && logicalFieldKey.equals(
                                                existing.getFieldKey()
                                        )
                                )
                                .findFirst()
                                .orElse(null);
            }

            if (mapping == null) {

                mapping =
                        new HighLevelCustomFieldMapping(
                                locationId,
                                logicalFieldKey,
                                externalFieldId,
                                externalField.name(),
                                externalField.dataType()
                        );

            } else {

                mapping.updateExternalDefinition(
                        externalFieldId,
                        externalField.name(),
                        externalField.dataType()
                );
            }

            customFieldMappingRepository.save(
                    mapping
            );

            matched.put(
                    logicalFieldKey,
                    externalFieldId
            );
        }

        return new CustomFieldSyncResult(
                agency.getPublicId(),
                agency.getName(),
                locationId,
                matched,
                missing,
                missing.isEmpty()
        );
    }

    private HighLevelCustomFieldsResponse.HighLevelCustomFieldDefinition
    findByName(
            List<HighLevelCustomFieldsResponse.HighLevelCustomFieldDefinition>
                    fields,
            String expectedName
    ) {

        if (fields == null || expectedName == null) {
            return null;
        }

        String normalizedExpected =
                normalizeFieldName(
                        expectedName
                );

        return fields
                .stream()
                .filter(field ->
                        field != null
                                && field.name() != null
                )
                .filter(field ->
                        normalizeFieldName(
                                field.name()
                        ).equals(
                                normalizedExpected
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private String normalizeFieldName(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]+",
                        ""
                );
    }

    private String requireText(
            String value,
            String errorMessage
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalStateException(
                    errorMessage
            );
        }

        return value.trim();
    }

    private static Map<String, String>
    createClientFieldKeys() {

        Map<String, String> fields =
                new LinkedHashMap<>();

        /*
         * Shared CareScale contact classification.
         */
        fields.put(
                "contact.lead_type",
                "Lead Type"
        );

        fields.put(
                "contact.preferred_contact_method",
                "Preferred Contact Method"
        );

        fields.put(
                "contact.lead_source",
                "Lead Source"
        );

        fields.put(
                "contact.campaign_name",
                "Campaign Name"
        );

        /*
         * Client-specific fields.
         */
        fields.put(
                "contact.service_needed",
                "Service Needed"
        );

        fields.put(
                "contact.care_start_timeline",
                "Care Start Timeline"
        );

        fields.put(
                "contact.payer_type",
                "Payer Type"
        );

        fields.put(
                "contact.decision_maker",
                "Decision Maker"
        );

        fields.put(
                "contact.ai_qualification_score",
                "AI Qualification Score"
        );

        fields.put(
                "contact.ai_summary",
                "AI Summary"
        );

        return Map.copyOf(
                fields
        );
    }

    private static Map<String, String>
    createCaregiverFieldKeys() {

        Map<String, String> fields =
                new LinkedHashMap<>();

        /*
         * Shared CareScale contact fields.
         */
        fields.put(
                "contact.lead_type",
                "Lead Type"
        );

        fields.put(
                "contact.preferred_contact_method",
                "Preferred Contact Method"
        );

        fields.put(
                "contact.lead_source",
                "Lead Source"
        );

        fields.put(
                "contact.campaign_name",
                "Campaign Name"
        );

        /*
         * Caregiver-specific fields.
         */
        fields.put(
                "contact.years_experience",
                "Years Experience"
        );

        fields.put(
                "contact.certifications",
                "Certifications"
        );

        fields.put(
                "contact.availability",
                "Availability"
        );

        fields.put(
                "contact.transportation",
                "Transportation"
        );

        fields.put(
                "contact.preferred_schedule",
                "Preferred Schedule"
        );

        fields.put(
                "contact.desired_hours_per_week",
                "Desired Hours Per Week"
        );

        fields.put(
                "contact.service_area",
                "Service Area"
        );

        fields.put(
                "contact.ai_screening_score",
                "AI Screening Score"
        );

        fields.put(
                "contact.ai_screening_summary",
                "AI Screening Summary"
        );

        return Map.copyOf(
                fields
        );
    }

    public record CustomFieldSyncResult(
            UUID agencyPublicId,
            String agencyName,
            String locationId,
            Map<String, String> matchedFields,
            Map<String, String> missingFields,
            boolean complete
    ) {
    }
}