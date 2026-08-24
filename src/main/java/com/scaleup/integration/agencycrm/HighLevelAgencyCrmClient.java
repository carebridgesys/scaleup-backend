package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.integration.highlevel.HighLevelProperties;
import com.scaleup.integration.highlevel.dto.HighLevelContactRequest;
import com.scaleup.integration.highlevel.dto.HighLevelContactResponse;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityRequest;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityResponse;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class HighLevelAgencyCrmClient
        implements AgencyCrmClient {

    private final RestClient
            highLevelRestClient;

    private final HighLevelProperties
            highLevelProperties;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    public HighLevelAgencyCrmClient(
            RestClient highLevelRestClient,
            HighLevelProperties highLevelProperties,
            HighLevelPipelineMappingRepository pipelineMappingRepository
    ) {
        this.highLevelRestClient =
                highLevelRestClient;

        this.highLevelProperties =
                highLevelProperties;

        this.pipelineMappingRepository =
                pipelineMappingRepository;
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

        String locationId =
                normalizeRequired(
                        agency.getHighLevelLocationId(),
                        "HighLevel location ID is missing for agency "
                                + agency.getSlug()
                );

        String token =
                resolveAgencyToken(
                        agency
                );

        /*
         * Load the pipeline dynamically using:
         *
         * location_id + lead_type
         *
         * Example for XYZ:
         *
         * rpelZN1piHfi4cRKl2VP
         * +
         * CLIENT
         *
         * → Client Acquisition
         * → New Lead
         */
        HighLevelPipelineMapping pipeline =
                pipelineMappingRepository
                        .findByLocationIdAndLeadTypeAndActiveTrue(
                                locationId,
                                lead.getLeadType()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HighLevel agency pipeline mapping was not found"
                                                + " for locationId="
                                                + locationId
                                                + ", leadType="
                                                + lead.getLeadType()
                                )
                        );

        /*
         * Step 1:
         * Create or update the contact
         * inside the destination agency sub-account.
         */
        String contactId =
                createOrUpdateContact(
                        lead,
                        agency,
                        locationId,
                        token
                );

        /*
         * Step 2:
         * Create/update the agency opportunity
         * using the agency-specific pipeline mapping.
         */
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
            Agency agency,
            String locationId,
            String token
    ) {

        List<String> tags =
                buildTags(
                        lead
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

                        /*
                         * Do NOT send Internal CRM
                         * custom-field IDs here.
                         *
                         * HighLevel custom fields are
                         * location-specific.
                         *
                         * Agency-specific fields can be
                         * added later using mappings for
                         * this agency location.
                         */
                        List.of()
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
                    "HighLevel did not return a contact ID for agency "
                            + agency.getSlug()
            );
        }

        return response
                .contact()
                .id()
                .trim();
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
                    "HighLevel did not return an opportunity ID"
                            + " for lead "
                            + lead.getPublicId()
            );
        }

        return response
                .opportunity()
                .id()
                .trim();
    }

    private String resolveAgencyToken(
            Agency agency
    ) {

        HighLevelProperties.AgencyCrm configuration =
                highLevelProperties
                        .getAgencyConfiguration(
                                agency.getSlug()
                        );

        if (configuration == null) {
            throw new IllegalStateException(
                    "HighLevel credential configuration was not found for agency: "
                            + agency.getSlug()
            );
        }

        return normalizeRequired(
                configuration.getToken(),
                "HighLevel token is missing for agency "
                        + agency.getSlug()
        );
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
                    "Lead type is missing for lead "
                            + lead.getPublicId()
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
                    "Agency is not active: "
                            + agency.getSlug()
            );
        }

        if (!agency.isHighLevelSyncEnabled()) {
            throw new IllegalStateException(
                    "HighLevel synchronization is disabled for agency: "
                            + agency.getSlug()
            );
        }

        if (
                agency.getHighLevelLocationId() == null
                        || agency
                        .getHighLevelLocationId()
                        .isBlank()
        ) {
            throw new IllegalStateException(
                    "HighLevel location ID is missing for agency: "
                            + agency.getSlug()
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