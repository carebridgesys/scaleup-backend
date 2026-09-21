package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.caregiverlead.CaregiverLeadDetailsRepository;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.clientlead.ClientLeadDetailsRepository;
import com.scaleup.integration.highlevel.HighLevelCustomFieldMapping;
import com.scaleup.integration.highlevel.HighLevelCustomFieldMappingRepository;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.integration.highlevel.dto.HighLevelContactRequest;
import com.scaleup.integration.highlevel.dto.HighLevelContactResponse;
import com.scaleup.integration.highlevel.dto.HighLevelCustomFieldValue;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityRequest;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityResponse;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import com.scaleup.security.SecretEncryptionService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class HighLevelAgencyCrmClient
        implements AgencyCrmClient {

    /*
     * Shared destination custom fields.
     */
    private static final String FIELD_PREFERRED_CONTACT_METHOD =
            "contact.preferred_contact_method";

    private static final String FIELD_LEAD_SOURCE =
            "contact.lead_source";

    private static final String FIELD_CAMPAIGN_NAME =
            "contact.campaign_name";

    private static final String FIELD_ZIP_CODE =
            "contact.zip_code";

    /*
     * Client destination custom fields.
     */
    private static final String FIELD_SERVICE_NEEDED =
            "contact.service_needed";

    private static final String FIELD_CARE_START_TIMELINE =
            "contact.care_start_timeline";

    private static final String FIELD_PAYER_TYPE =
            "contact.payer_type";

    private static final String FIELD_DECISION_MAKER =
            "contact.decision_maker";

    private static final String FIELD_AI_QUALIFICATION_SCORE =
            "contact.ai_qualification_score";

    private static final String FIELD_AI_SUMMARY =
            "contact.ai_summary";

    /*
     * Caregiver destination custom fields.
     */
    private static final String FIELD_YEARS_EXPERIENCE =
            "contact.years_experience";

    private static final String FIELD_CERTIFICATIONS =
            "contact.certifications";

    private static final String FIELD_AVAILABILITY =
            "contact.availability";

    private static final String FIELD_TRANSPORTATION =
            "contact.transportation";

    private static final String FIELD_PREFERRED_SCHEDULE =
            "contact.preferred_schedule";

    private static final String FIELD_DESIRED_HOURS_PER_WEEK =
            "contact.desired_hours_per_week";

    private static final String FIELD_SERVICE_AREA =
            "contact.service_area";

    private static final String FIELD_AI_SCREENING_SCORE =
            "contact.ai_screening_score";

    private static final String FIELD_AI_SCREENING_SUMMARY =
            "contact.ai_screening_summary";

    private final RestClient highLevelRestClient;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    private final AgencyHighLevelConnectionRepository
            connectionRepository;

    private final HighLevelCustomFieldMappingRepository
            customFieldMappingRepository;

    private final ClientLeadDetailsRepository
            clientLeadDetailsRepository;

    private final CaregiverLeadDetailsRepository
            caregiverLeadDetailsRepository;

    private final SecretEncryptionService
            secretEncryptionService;

    public HighLevelAgencyCrmClient(
            RestClient highLevelRestClient,
            HighLevelPipelineMappingRepository pipelineMappingRepository,
            AgencyHighLevelConnectionRepository connectionRepository,
            HighLevelCustomFieldMappingRepository customFieldMappingRepository,
            ClientLeadDetailsRepository clientLeadDetailsRepository,
            CaregiverLeadDetailsRepository caregiverLeadDetailsRepository,
            SecretEncryptionService secretEncryptionService
    ) {

        this.highLevelRestClient =
                highLevelRestClient;

        this.pipelineMappingRepository =
                pipelineMappingRepository;

        this.connectionRepository =
                connectionRepository;

        this.customFieldMappingRepository =
                customFieldMappingRepository;

        this.clientLeadDetailsRepository =
                clientLeadDetailsRepository;

        this.caregiverLeadDetailsRepository =
                caregiverLeadDetailsRepository;

        this.secretEncryptionService =
                secretEncryptionService;
    }

    @Override
    public AgencyCrmSyncResult createLead(
            Lead lead
    ) {

        validateLead(
                lead
        );

        Agency agency =
                lead.getAgency();

        validateAgency(
                agency
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
                normalizeRequired(
                        connection.getLocationId(),
                        "HighLevel location ID is missing."
                );

        String token =
                secretEncryptionService
                        .decrypt(
                                connection
                                        .getAccessTokenEncrypted()
                        );

        token =
                normalizeRequired(
                        token,
                        "HighLevel access token is missing."
                );

        /*
         * Keep agency + lead-type-specific pipeline routing.
         *
         * Agency-specific mappings take precedence.
         * Global mappings remain available as fallback.
         */
        HighLevelPipelineMapping pipeline =
                pipelineMappingRepository
                        .findByAgencyPublicIdAndLocationIdAndLeadTypeAndActiveTrue(
                                agency.getPublicId(),
                                locationId,
                                lead.getLeadType()
                        )
                        .or(() ->
                                pipelineMappingRepository
                                        .findByAgencyIsNullAndLocationIdAndLeadTypeAndActiveTrue(
                                                locationId,
                                                lead.getLeadType()
                                        )
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HighLevel agency pipeline mapping was not found"
                                                + " for agency="
                                                + agency.getPublicId()
                                                + ", locationId="
                                                + locationId
                                                + ", leadType="
                                                + lead.getLeadType()
                                )
                        );

        String contactId =
                createOrUpdateContact(
                        lead,
                        locationId,
                        token
                );

        String opportunityId =
                createOrUpdateOpportunity(
                        lead,
                        pipeline,
                        locationId,
                        contactId,
                        token
                );

        return new AgencyCrmSyncResult(
                contactId,
                opportunityId
        );
    }

    private String createOrUpdateContact(
            Lead lead,
            String locationId,
            String token
    ) {

        List<String> tags =
                buildTags(
                        lead
                );

        List<HighLevelCustomFieldValue> customFields =
                buildCustomFields(
                        lead,
                        locationId
                );

        HighLevelContactRequest request =
                new HighLevelContactRequest(
                        locationId,
                        lead.getFirstName(),
                        lead.getLastName(),
                        lead.getEmail(),
                        lead.getPhone(),
                        lead.getSource(),
                        tags,
                        customFields
                );

        HighLevelContactResponse response =
                highLevelRestClient
                        .post()
                        .uri(
                                "/contacts/upsert"
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .body(
                                request
                        )
                        .retrieve()
                        .body(
                                HighLevelContactResponse.class
                        );

        if (
                response == null
                        || response.contact() == null
                        || response.contact().id() == null
                        || response.contact().id().isBlank()
        ) {

            throw new IllegalStateException(
                    "HighLevel did not return a contact ID."
            );
        }

        return response
                .contact()
                .id()
                .trim();
    }

    private List<HighLevelCustomFieldValue> buildCustomFields(
            Lead lead,
            String locationId
    ) {

        List<HighLevelCustomFieldValue> fields =
                new ArrayList<>();

        /*
         * Fields shared by both CLIENT and CAREGIVER leads.
         */
        addSharedCustomFields(
                fields,
                lead,
                locationId
        );

        /*
         * Lead-type-specific fields.
         */
        if (lead.getLeadType() == LeadType.CLIENT) {

            addClientCustomFields(
                    fields,
                    lead,
                    locationId
            );

        } else if (
                lead.getLeadType()
                        == LeadType.CAREGIVER
        ) {

            addCaregiverCustomFields(
                    fields,
                    lead,
                    locationId
            );
        }

        return fields;
    }

    private void addSharedCustomFields(
            List<HighLevelCustomFieldValue> fields,
            Lead lead,
            String locationId
    ) {

        addCustomField(
                fields,
                locationId,
                FIELD_ZIP_CODE,
                lead.getZipCode()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_PREFERRED_CONTACT_METHOD,
                lead.getPreferredContactMethod()
        );

        if (lead.getCampaign() != null) {

            addCustomField(
                    fields,
                    locationId,
                    FIELD_CAMPAIGN_NAME,
                    lead.getCampaign().getName()
            );
        }

        addCustomField(
                fields,
                locationId,
                FIELD_LEAD_SOURCE,
                lead.getSource()
        );
    }

    private void addClientCustomFields(
            List<HighLevelCustomFieldValue> fields,
            Lead lead,
            String locationId
    ) {

        ClientLeadDetails details =
                clientLeadDetailsRepository
                        .findByLeadPublicId(
                                lead.getPublicId()
                        )
                        .orElse(null);

        if (details == null) {
            return;
        }

        addCustomField(
                fields,
                locationId,
                FIELD_SERVICE_NEEDED,
                details.getServiceNeeded()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_CARE_START_TIMELINE,
                details.getCareStartTimeline()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_PAYER_TYPE,
                details.getPayerType()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_DECISION_MAKER,
                details.getDecisionMaker()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_AI_QUALIFICATION_SCORE,
                details.getAiQualificationScore()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_AI_SUMMARY,
                details.getAiSummary()
        );
    }

    private void addCaregiverCustomFields(
            List<HighLevelCustomFieldValue> fields,
            Lead lead,
            String locationId
    ) {

        CaregiverLeadDetails details =
                caregiverLeadDetailsRepository
                        .findByLeadPublicId(
                                lead.getPublicId()
                        )
                        .orElse(null);

        if (details == null) {
            return;
        }

        addCustomField(
                fields,
                locationId,
                FIELD_YEARS_EXPERIENCE,
                details.getYearsExperience()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_CERTIFICATIONS,
                details.getCertifications()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_AVAILABILITY,
                details.getAvailability()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_TRANSPORTATION,
                details.getTransportation()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_PREFERRED_SCHEDULE,
                details.getPreferredSchedule()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_DESIRED_HOURS_PER_WEEK,
                details.getDesiredHoursPerWeek()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_SERVICE_AREA,
                details.getServiceArea()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_AI_SCREENING_SCORE,
                details.getAiScreeningScore()
        );

        addCustomField(
                fields,
                locationId,
                FIELD_AI_SCREENING_SUMMARY,
                details.getAiScreeningSummary()
        );
    }

    private void addCustomField(
            List<HighLevelCustomFieldValue> fields,
            String locationId,
            String fieldKey,
            Object value
    ) {

        if (value == null) {
            return;
        }

        if (
                value instanceof String stringValue
                        && stringValue.isBlank()
        ) {
            return;
        }

        HighLevelCustomFieldMapping mapping =
                customFieldMappingRepository
                        .findByLocationIdAndFieldKeyAndActiveTrue(
                                locationId,
                                fieldKey
                        )
                        .orElse(null);

        /*
         * Optional custom-field mappings must not prevent
         * the contact/opportunity from reaching the agency.
         */
        if (mapping == null) {
            return;
        }

        String externalFieldId =
                mapping.getExternalFieldId();

        if (
                externalFieldId == null
                        || externalFieldId.isBlank()
        ) {
            return;
        }

        fields.add(
                new HighLevelCustomFieldValue(
                        externalFieldId.trim(),
                        value
                )
        );
    }

    private String createOrUpdateOpportunity(
            Lead lead,
            HighLevelPipelineMapping pipeline,
            String locationId,
            String contactId,
            String token
    ) {

        String opportunityName =
                buildOpportunityName(
                        lead
                );

        HighLevelOpportunityRequest request =
                new HighLevelOpportunityRequest(
                        locationId,
                        pipeline.getPipelineId(),
                        pipeline.getInitialStageId(),
                        contactId,
                        opportunityName,
                        "open"
                );

        HighLevelOpportunityResponse response =
                highLevelRestClient
                        .post()
                        .uri(
                                "/opportunities/upsert"
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .body(
                                request
                        )
                        .retrieve()
                        .body(
                                HighLevelOpportunityResponse.class
                        );

        if (
                response == null
                        || response.opportunity() == null
                        || response.opportunity().id() == null
                        || response.opportunity().id().isBlank()
        ) {

            throw new IllegalStateException(
                    "HighLevel did not return an opportunity ID."
            );
        }

        return response
                .opportunity()
                .id()
                .trim();
    }

    private void validateLead(
            Lead lead
    ) {

        if (lead == null) {

            throw new IllegalArgumentException(
                    "Lead must not be null."
            );
        }

        if (lead.getLeadType() == null) {

            throw new IllegalStateException(
                    "Lead type is missing."
            );
        }
    }

    private void validateAgency(
            Agency agency
    ) {

        if (agency == null) {

            throw new IllegalStateException(
                    "Lead does not have an agency."
            );
        }

        if (!agency.isActive()) {

            throw new IllegalStateException(
                    "Agency is not active."
            );
        }

        if (!agency.isHighLevelSyncEnabled()) {

            throw new IllegalStateException(
                    "HighLevel synchronization is disabled for agency."
            );
        }
    }

    private List<String> buildTags(
            Lead lead
    ) {

        List<String> tags =
                new ArrayList<>();

        tags.add(
                "careprospect-lead"
        );

        if (
                lead.getAgency() != null
                        && lead.getAgency().getSlug() != null
                        && !lead.getAgency()
                        .getSlug()
                        .isBlank()
        ) {

            String normalizedAgency =
                    normalizeTag(
                            lead.getAgency()
                                    .getSlug()
                    );

            if (!normalizedAgency.isBlank()) {

                tags.add(
                        "agency-"
                                + normalizedAgency
                );
            }
        }

        if (
                lead.getLeadType()
                        == LeadType.CLIENT
        ) {

            tags.add(
                    "client-lead"
            );

        } else if (
                lead.getLeadType()
                        == LeadType.CAREGIVER
        ) {

            tags.add(
                    "caregiver-lead"
            );
        }

        return tags;
    }

    private String buildOpportunityName(
            Lead lead
    ) {

        StringBuilder name =
                new StringBuilder();

        if (
                lead.getFirstName() != null
                        && !lead.getFirstName()
                        .isBlank()
        ) {

            name.append(
                    lead.getFirstName()
                            .trim()
            );
        }

        if (
                lead.getLastName() != null
                        && !lead.getLastName()
                        .isBlank()
        ) {

            if (!name.isEmpty()) {
                name.append(" ");
            }

            name.append(
                    lead.getLastName()
                            .trim()
            );
        }

        if (
                lead.getLeadType()
                        == LeadType.CLIENT
        ) {

            name.append(
                    " - Client Lead"
            );

        } else if (
                lead.getLeadType()
                        == LeadType.CAREGIVER
        ) {

            name.append(
                    " - Caregiver Applicant"
            );
        }

        if (name.isEmpty()) {

            return "CareProspect Lead";
        }

        return name.toString();
    }

    private String normalizeTag(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]+",
                        "-"
                )
                .replaceAll(
                        "^-+|-+$",
                        ""
                );
    }

    private String normalizeRequired(
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
}