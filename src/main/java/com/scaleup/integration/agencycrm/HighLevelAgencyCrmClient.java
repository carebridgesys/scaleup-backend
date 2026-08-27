package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.integration.highlevel.dto.HighLevelContactRequest;
import com.scaleup.integration.highlevel.dto.HighLevelContactResponse;
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

    private final RestClient
            highLevelRestClient;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    private final AgencyHighLevelConnectionRepository
            connectionRepository;

    private final SecretEncryptionService
            secretEncryptionService;

    public HighLevelAgencyCrmClient(
            RestClient highLevelRestClient,
            HighLevelPipelineMappingRepository pipelineMappingRepository,
            AgencyHighLevelConnectionRepository connectionRepository,
            SecretEncryptionService secretEncryptionService
    ) {

        this.highLevelRestClient =
                highLevelRestClient;

        this.pipelineMappingRepository =
                pipelineMappingRepository;

        this.connectionRepository =
                connectionRepository;

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

        HighLevelContactRequest request =
                new HighLevelContactRequest(
                        locationId,
                        lead.getFirstName(),
                        lead.getLastName(),
                        lead.getEmail(),
                        lead.getPhone(),
                        lead.getSource(),
                        tags,
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
                    "HighLevel did not return a contact ID."
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