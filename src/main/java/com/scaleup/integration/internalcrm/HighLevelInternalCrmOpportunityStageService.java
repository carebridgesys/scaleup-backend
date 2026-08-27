package com.scaleup.integration.internalcrm;

import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.integration.highlevel.HighLevelProperties;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityResponse;
import com.scaleup.integration.highlevel.dto.HighLevelOpportunityUpdateRequest;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class HighLevelInternalCrmOpportunityStageService {

    private final RestClient
            highLevelRestClient;

    private final HighLevelProperties
            highLevelProperties;

    private final LeadRepository
            leadRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    public HighLevelInternalCrmOpportunityStageService(
            RestClient highLevelRestClient,
            HighLevelProperties highLevelProperties,
            LeadRepository leadRepository,
            LeadCrmSyncRepository leadCrmSyncRepository,
            HighLevelPipelineMappingRepository pipelineMappingRepository
    ) {

        this.highLevelRestClient =
                highLevelRestClient;

        this.highLevelProperties =
                highLevelProperties;

        this.leadRepository =
                leadRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.pipelineMappingRepository =
                pipelineMappingRepository;
    }

    public void moveToStage(
            UUID leadPublicId,
            InternalCrmOpportunityStage targetStage
    ) {

        Lead lead =
                leadRepository
                        .findByPublicId(
                                leadPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Lead was not found."
                                )
                        );

        if (lead.getAgency() == null) {

            throw new IllegalStateException(
                    "Agency is missing for lead."
            );
        }

        String locationId =
                requireText(
                        highLevelProperties
                                .getInternalCrm()
                                .getLocationId(),
                        "Internal CRM location ID"
                );

        String token =
                requireText(
                        highLevelProperties
                                .getInternalCrm()
                                .getToken(),
                        "Internal CRM token"
                );

        LeadCrmSync internalCrmSync =
                leadCrmSyncRepository
                        .findByLeadPublicIdAndDestination(
                                leadPublicId,
                                CrmDestination.INTERNAL_CRM
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Internal CRM sync record was not found."
                                )
                        );

        if (
                internalCrmSync.getSyncStatus()
                        != CrmSyncStatus.SYNCED
        ) {

            throw new IllegalStateException(
                    "Internal CRM opportunity cannot be moved because the lead is not synced."
            );
        }

        String opportunityId =
                requireText(
                        internalCrmSync
                                .getExternalOpportunityId(),
                        "Internal CRM opportunity ID"
                );

        HighLevelPipelineMapping mapping =
                pipelineMappingRepository
                        .findByAgencyPublicIdAndLocationIdAndLeadTypeAndActiveTrue(
                                lead.getAgency()
                                        .getPublicId(),
                                locationId,
                                lead.getLeadType()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Agency-specific Internal CRM pipeline mapping was not found."
                                )
                        );

        String stageId =
                resolveStageId(
                        mapping,
                        targetStage
                );

        HighLevelOpportunityUpdateRequest request =
                new HighLevelOpportunityUpdateRequest(
                        mapping.getPipelineId(),
                        buildOpportunityName(
                                lead
                        ),
                        stageId,
                        "open"
                );

        HighLevelOpportunityResponse response =
                highLevelRestClient
                        .put()
                        .uri(
                                "/opportunities/{id}",
                                opportunityId
                        )

                        /*
                         * Current opportunity API uses v3.
                         * Override the shared client's
                         * default Version header here.
                         */
                        .header(
                                "Version",
                                "v3"
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
                    "HighLevel did not confirm the opportunity stage update."
            );
        }
    }

    private String resolveStageId(
            HighLevelPipelineMapping mapping,
            InternalCrmOpportunityStage stage
    ) {

        String stageId =
                switch (stage) {

                    case ATTEMPTING_CONTACT ->
                            mapping.getAttemptingContactStageId();

                    case CONTACTED ->
                            mapping.getContactedStageId();

                    case QUALIFIED ->
                            mapping.getQualifiedStageId();

                    case ROUTED ->
                            mapping.getRoutedStageId();
                };

        return requireText(
                stageId,
                "HighLevel stage ID for "
                        + stage.name()
        );
    }

    private String buildOpportunityName(
            Lead lead
    ) {

        StringBuilder name =
                new StringBuilder();

        if (
                lead.getFirstName() != null
                        && !lead.getFirstName().isBlank()
        ) {

            name.append(
                    lead.getFirstName().trim()
            );
        }

        if (
                lead.getLastName() != null
                        && !lead.getLastName().isBlank()
        ) {

            if (!name.isEmpty()) {
                name.append(" ");
            }

            name.append(
                    lead.getLastName().trim()
            );
        }

        switch (lead.getLeadType()) {

            case CLIENT ->
                    name.append(
                            " - Client Lead"
                    );

            case CAREGIVER ->
                    name.append(
                            " - Caregiver Applicant"
                    );
        }

        return name.toString();
    }

    private String requireText(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new IllegalStateException(
                    fieldName
                            + " must be configured."
            );
        }

        return value.trim();
    }
}