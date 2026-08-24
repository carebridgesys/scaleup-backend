package com.scaleup.integration.internalcrm;

import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.caregiverlead.CaregiverLeadDetailsRepository;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.clientlead.ClientLeadDetailsRepository;
import com.scaleup.integration.highlevel.HighLevelCustomFieldMapping;
import com.scaleup.integration.highlevel.HighLevelCustomFieldMappingRepository;
import com.scaleup.integration.highlevel.HighLevelFieldValueMapper;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.integration.highlevel.HighLevelProperties;
import com.scaleup.integration.highlevel.dto.HighLevelContactRequest;
import com.scaleup.integration.highlevel.dto.HighLevelContactResponse;
import com.scaleup.integration.highlevel.dto.HighLevelCustomFieldValue;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityRequest;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityResponse;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Primary
public class HighLevelInternalCrmClient
        implements InternalCrmClient {

    private final RestClient highLevelRestClient;

    private final HighLevelProperties properties;

    private final HighLevelCustomFieldMappingRepository
            customFieldMappingRepository;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    private final ClientLeadDetailsRepository
            clientLeadDetailsRepository;

    private final CaregiverLeadDetailsRepository
            caregiverLeadDetailsRepository;

    private final HighLevelFieldValueMapper
            fieldValueMapper;

    public HighLevelInternalCrmClient(
            RestClient highLevelRestClient,
            HighLevelProperties properties,
            HighLevelCustomFieldMappingRepository customFieldMappingRepository,
            HighLevelPipelineMappingRepository pipelineMappingRepository,
            ClientLeadDetailsRepository clientLeadDetailsRepository,
            CaregiverLeadDetailsRepository caregiverLeadDetailsRepository,
            HighLevelFieldValueMapper fieldValueMapper
    ) {
        this.highLevelRestClient =
                highLevelRestClient;

        this.properties =
                properties;

        this.customFieldMappingRepository =
                customFieldMappingRepository;

        this.pipelineMappingRepository =
                pipelineMappingRepository;

        this.clientLeadDetailsRepository =
                clientLeadDetailsRepository;

        this.caregiverLeadDetailsRepository =
                caregiverLeadDetailsRepository;

        this.fieldValueMapper =
                fieldValueMapper;
    }

    @Override
    public InternalCrmSyncResult createLead(
            Lead lead
    ) {

        String locationId =
                properties
                        .getInternalCrm()
                        .getLocationId();

        String processEnvToken =
                System.getenv(
                        "HIGHLEVEL_INTERNAL_CRM_TOKEN"
                );

        if (processEnvToken == null) {

            System.out.println(
                    "PROCESS ENV TOKEN = NOT SET"
            );

        } else {

            String trimmedProcessToken =
                    processEnvToken.trim();

            System.out.println(
                    "PROCESS ENV TOKEN: length="
                            + trimmedProcessToken.length()
                            + ", fingerprint="
                            + tokenFingerprint(
                            trimmedProcessToken
                    )
            );
        }

        String token =
                properties
                        .getInternalCrm()
                        .getToken();

        if (
                token == null
                        || token.isBlank()
        ) {
            throw new IllegalStateException(
                    "HighLevel Internal CRM token is not configured."
            );
        }

        token =
                token.trim();

        System.out.println(
                "HighLevel token loaded: length="
                        + token.length()
                        + ", fingerprint="
                        + tokenFingerprint(token)
                        + ", startsWithBearer="
                        + token.startsWith("Bearer ")
        );

        if (
                locationId == null
                        || locationId.isBlank()
        ) {
            throw new IllegalStateException(
                    "HighLevel Internal CRM location ID is not configured."
            );
        }

        if (lead.getLeadType() == null) {
            throw new IllegalStateException(
                    "Lead type is missing for lead "
                            + lead.getPublicId()
            );
        }

        if (lead.getAgency() == null) {
            throw new IllegalStateException(
                    "Agency is missing for lead "
                            + lead.getPublicId()
            );
        }

        HighLevelPipelineMapping pipeline =
                pipelineMappingRepository
                        .findByLocationIdAndLeadTypeAndActiveTrue(
                                locationId,
                                lead.getLeadType()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HighLevel pipeline mapping was not found"
                                                + " for locationId="
                                                + locationId
                                                + ", leadType="
                                                + lead.getLeadType()
                                )
                        );

        Map<String, HighLevelCustomFieldMapping> fieldMappings =
                customFieldMappingRepository
                        .findAllByLocationIdAndActiveTrue(
                                locationId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        HighLevelCustomFieldMapping::getFieldKey,
                                        Function.identity()
                                )
                        );

        List<HighLevelCustomFieldValue> customFields =
                buildCustomFields(
                        lead,
                        fieldMappings
                );

        List<String> tags =
                buildContactTags(
                        lead
                );

        HighLevelContactRequest contactRequest =
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

        HighLevelContactResponse contactResponse =
                highLevelRestClient
                        .post()
                        .uri("/contacts/upsert")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .body(contactRequest)
                        .retrieve()
                        .body(
                                HighLevelContactResponse.class
                        );

        if (
                contactResponse == null
                        || contactResponse.contact() == null
                        || contactResponse.contact().id() == null
        ) {
            throw new IllegalStateException(
                    "HighLevel did not return a contact ID."
            );
        }

        String contactId =
                contactResponse
                        .contact()
                        .id();

        String opportunityName =
                buildOpportunityName(
                        lead
                );

        HighLevelOpportunityRequest opportunityRequest =
                new HighLevelOpportunityRequest(
                        locationId,
                        pipeline.getPipelineId(),
                        pipeline.getInitialStageId(),
                        contactId,
                        opportunityName,
                        "open"
                );

        HighLevelOpportunityResponse opportunityResponse =
                highLevelRestClient
                        .post()
                        .uri("/opportunities/upsert")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .body(opportunityRequest)
                        .retrieve()
                        .body(
                                HighLevelOpportunityResponse.class
                        );

        if (
                opportunityResponse == null
                        || opportunityResponse.opportunity() == null
                        || opportunityResponse.opportunity().id() == null
        ) {
            throw new IllegalStateException(
                    "HighLevel did not return an opportunity ID."
            );
        }

        return new InternalCrmSyncResult(
                contactId,
                opportunityResponse
                        .opportunity()
                        .id()
        );
    }

    private List<HighLevelCustomFieldValue>
    buildCustomFields(
            Lead lead,
            Map<String, HighLevelCustomFieldMapping> mappings
    ) {

        List<HighLevelCustomFieldValue> values =
                new ArrayList<>();

        /*
         * Shared lead-attribution fields.
         */
        add(
                values,
                mappings,
                "contact.agency_name",
                lead.getAgency().getName()
        );

        add(
                values,
                mappings,
                "contact.lead_type",
                mapLeadType(
                        lead.getLeadType()
                )
        );

        /*
         * General contact fields.
         */
        add(
                values,
                mappings,
                "contact.zip_code",
                lead.getZipCode()
        );

        if (
                lead.getPreferredContactMethod()
                        != null
        ) {
            add(
                    values,
                    mappings,
                    "contact.preferred_contact_method",
                    fieldValueMapper
                            .mapPreferredContactMethod(
                                    lead
                                            .getPreferredContactMethod()
                                            .name()
                            )
            );
        }

        add(
                values,
                mappings,
                "contact.campaign_name",
                lead.getCampaignName()
        );

        add(
                values,
                mappings,
                "contact.lead_source",
                fieldValueMapper
                        .mapLeadSource(
                                lead.getSource()
                        )
        );

        if (
                lead.getLeadType()
                        == LeadType.CLIENT
        ) {

            addClientFields(
                    lead,
                    values,
                    mappings
            );

        } else if (
                lead.getLeadType()
                        == LeadType.CAREGIVER
        ) {

            addCaregiverFields(
                    lead,
                    values,
                    mappings
            );
        }

        return values;
    }

    private void addClientFields(
            Lead lead,
            List<HighLevelCustomFieldValue> values,
            Map<String, HighLevelCustomFieldMapping> mappings
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

        add(
                values,
                mappings,
                "contact.service_needed",
                fieldValueMapper
                        .mapServiceNeeded(
                                details.getServiceNeeded()
                        )
        );

        add(
                values,
                mappings,
                "contact.care_start_timeline",
                fieldValueMapper
                        .mapCareStartTimeline(
                                details.getCareStartTimeline()
                        )
        );

        add(
                values,
                mappings,
                "contact.payer_type",
                fieldValueMapper
                        .mapPayerType(
                                details.getPayerType()
                        )
        );

        add(
                values,
                mappings,
                "contact.decision_maker",
                fieldValueMapper
                        .mapDecisionMaker(
                                details.getDecisionMaker()
                        )
        );

        add(
                values,
                mappings,
                "contact.ai_qualification_score",
                details.getAiQualificationScore()
        );

        add(
                values,
                mappings,
                "contact.ai_summary",
                details.getAiSummary()
        );
    }

    private void addCaregiverFields(
            Lead lead,
            List<HighLevelCustomFieldValue> values,
            Map<String, HighLevelCustomFieldMapping> mappings
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

        add(
                values,
                mappings,
                "contact.years_of_experience",
                details.getYearsExperience()
        );

        add(
                values,
                mappings,
                "contact.certification",
                details.getCertifications()
        );

        add(
                values,
                mappings,
                "contact.availability",
                fieldValueMapper
                        .mapAvailability(
                                details.getAvailability()
                        )
        );

        add(
                values,
                mappings,
                "contact.transportation",
                fieldValueMapper
                        .mapTransportation(
                                details.getTransportation()
                        )
        );

        add(
                values,
                mappings,
                "contact.preferred_schedule",
                fieldValueMapper
                        .mapPreferredSchedule(
                                details.getPreferredSchedule()
                        )
        );

        add(
                values,
                mappings,
                "contact.desired_hours_per_week",
                details.getDesiredHoursPerWeek()
        );

        add(
                values,
                mappings,
                "contact.service_area",
                details.getServiceArea()
        );

        add(
                values,
                mappings,
                "contact.background_status_check",
                fieldValueMapper
                        .mapBackgroundCheckStatus(
                                details.getBackgroundCheckStatus()
                        )
        );

        add(
                values,
                mappings,
                "contact.interview_status",
                fieldValueMapper
                        .mapInterviewStatus(
                                details.getInterviewStatus()
                        )
        );

        add(
                values,
                mappings,
                "contact.ai_screening_score",
                details.getAiScreeningScore()
        );

        add(
                values,
                mappings,
                "contact.ai_screening_summary",
                details.getAiScreeningSummary()
        );
    }

    private void add(
            List<HighLevelCustomFieldValue> values,
            Map<String, HighLevelCustomFieldMapping> mappings,
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
                mappings.get(
                        fieldKey
                );

        if (mapping == null) {

            System.out.println(
                    "HighLevel custom field mapping not found for fieldKey="
                            + fieldKey
            );

            return;
        }

        values.add(
                new HighLevelCustomFieldValue(
                        mapping.getExternalFieldId(),
                        value
                )
        );
    }

    private List<String> buildContactTags(
            Lead lead
    ) {

        List<String> tags =
                new ArrayList<>();

        if (
                lead.getAgency() != null
                        && lead.getAgency().getSlug() != null
                        && !lead.getAgency()
                        .getSlug()
                        .isBlank()
        ) {

            String agencyTag =
                    normalizeTag(
                            lead.getAgency()
                                    .getSlug()
                    );

            if (!agencyTag.isBlank()) {

                tags.add(
                        "agency-"
                                + agencyTag
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

    private String mapLeadType(
            LeadType leadType
    ) {

        if (leadType == null) {
            return null;
        }

        return switch (leadType) {

            case CLIENT ->
                    "Client";

            case CAREGIVER ->
                    "Caregiver";
        };
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

    private String buildOpportunityName(
            Lead lead
    ) {

        StringBuilder name =
                new StringBuilder();

        name.append(
                lead.getFirstName()
        );

        if (
                lead.getLastName() != null
                        && !lead.getLastName()
                        .isBlank()
        ) {

            name.append(" ")
                    .append(
                            lead.getLastName()
                    );
        }

        if (
                lead.getLeadType()
                        == LeadType.CLIENT
        ) {

            name.append(
                    " - Client Lead"
            );

        } else {

            name.append(
                    " - Caregiver Applicant"
            );
        }

        return name.toString();
    }

    private String tokenFingerprint(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            String fullHash =
                    HexFormat.of()
                            .formatHex(hash);

            return fullHash.substring(
                    0,
                    8
            );

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "Unable to calculate token fingerprint.",
                    ex
            );
        }
    }
}