package com.scaleup.onboarding;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import com.scaleup.campaign.Campaign;
import com.scaleup.campaign.CampaignRepository;
import com.scaleup.campaign.CampaignType;
import com.scaleup.integration.agencycrm.AgencyCrmConnectionStatus;
import com.scaleup.integration.agencycrm.AgencyHighLevelConnection;
import com.scaleup.integration.agencycrm.AgencyHighLevelConnectionRepository;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.lead.LeadType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgencyOnboardingStatusService {

    private final AgencyRepository agencyRepository;

    private final CampaignRepository campaignRepository;

    private final AgencyHighLevelConnectionRepository
            agencyHighLevelConnectionRepository;

    private final HighLevelPipelineMappingRepository
            highLevelPipelineMappingRepository;

    private final String internalCrmLocationId;

    public AgencyOnboardingStatusService(
            AgencyRepository agencyRepository,
            CampaignRepository campaignRepository,
            AgencyHighLevelConnectionRepository
                    agencyHighLevelConnectionRepository,
            HighLevelPipelineMappingRepository
                    highLevelPipelineMappingRepository,
            @Value("${highlevel.internal-crm.location-id}")
            String internalCrmLocationId
    ) {
        this.agencyRepository =
                agencyRepository;

        this.campaignRepository =
                campaignRepository;

        this.agencyHighLevelConnectionRepository =
                agencyHighLevelConnectionRepository;

        this.highLevelPipelineMappingRepository =
                highLevelPipelineMappingRepository;

        this.internalCrmLocationId =
                requireText(
                        internalCrmLocationId,
                        "Internal CRM location ID"
                );
    }

    @Transactional(readOnly = true)
    public AgencyOnboardingStatusResponse getStatus(
            UUID agencyPublicId
    ) {

        if (agencyPublicId == null) {
            throw new IllegalArgumentException(
                    "Agency public ID must not be null."
            );
        }

        Agency agency =
                agencyRepository
                        .findByPublicId(
                                agencyPublicId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Agency was not found: "
                                                + agencyPublicId
                                )
                        );

        List<Campaign> campaigns =
                campaignRepository
                        .findByAgencyPublicId(
                                agencyPublicId,
                                Pageable.unpaged()
                        )
                        .getContent();

        boolean clientCampaignConfigured =
                hasActiveCampaign(
                        campaigns,
                        CampaignType.CLIENT
                );

        boolean caregiverCampaignConfigured =
                hasActiveCampaign(
                        campaigns,
                        CampaignType.CAREGIVER
                );

        Optional<AgencyHighLevelConnection>
                connectionOptional =
                agencyHighLevelConnectionRepository
                        .findByAgencyPublicId(
                                agencyPublicId
                        );

        boolean highLevelConnected =
                connectionOptional
                        .map(connection ->
                                connection.getConnectionStatus()
                                        == AgencyCrmConnectionStatus.ACTIVE
                        )
                        .orElse(false);

        String connectionStatus =
                connectionOptional
                        .map(connection ->
                                connection
                                        .getConnectionStatus()
                                        .name()
                        )
                        .orElse(null);

        String agencyLocationId =
                connectionOptional
                        .map(
                                AgencyHighLevelConnection::getLocationId
                        )
                        .orElse(null);

        boolean internalClientMapping =
                hasActivePipelineMapping(
                        agencyPublicId,
                        internalCrmLocationId,
                        LeadType.CLIENT
                );

        boolean internalCaregiverMapping =
                hasActivePipelineMapping(
                        agencyPublicId,
                        internalCrmLocationId,
                        LeadType.CAREGIVER
                );

        boolean agencyClientMapping =
                agencyLocationId != null
                        && hasActivePipelineMapping(
                        agencyPublicId,
                        agencyLocationId,
                        LeadType.CLIENT
                );

        boolean agencyCaregiverMapping =
                agencyLocationId != null
                        && hasActivePipelineMapping(
                        agencyPublicId,
                        agencyLocationId,
                        LeadType.CAREGIVER
                );

        boolean atLeastOneProgramConfigured =
                clientCampaignConfigured
                        || caregiverCampaignConfigured;

        boolean clientProgramReady =
                !clientCampaignConfigured
                        || (
                        internalClientMapping
                                && agencyClientMapping
                );

        boolean caregiverProgramReady =
                !caregiverCampaignConfigured
                        || (
                        internalCaregiverMapping
                                && agencyCaregiverMapping
                );

        boolean readyForAds =
                agency.isActive()
                        && atLeastOneProgramConfigured
                        && highLevelConnected
                        && clientProgramReady
                        && caregiverProgramReady;

        return new AgencyOnboardingStatusResponse(

                agency.getPublicId(),
                agency.getName(),
                agency.getSlug(),
                agency.isActive(),

                new AgencyOnboardingStatusResponse
                        .CampaignStatus(
                        clientCampaignConfigured,
                        caregiverCampaignConfigured
                ),

                new AgencyOnboardingStatusResponse
                        .HighLevelConnectionStatus(
                        highLevelConnected,
                        connectionStatus,
                        agencyLocationId
                ),

                new AgencyOnboardingStatusResponse
                        .PipelineStatus(
                        internalClientMapping,
                        internalCaregiverMapping
                ),

                new AgencyOnboardingStatusResponse
                        .PipelineStatus(
                        agencyClientMapping,
                        agencyCaregiverMapping
                ),

                readyForAds
        );
    }

    private boolean hasActiveCampaign(
            List<Campaign> campaigns,
            CampaignType campaignType
    ) {

        return campaigns
                .stream()
                .anyMatch(campaign ->
                        campaign.isActive()
                                && campaign.getCampaignType()
                                == campaignType
                );
    }

    private boolean hasActivePipelineMapping(
            UUID agencyPublicId,
            String locationId,
            LeadType leadType
    ) {

        return highLevelPipelineMappingRepository
                .findByAgencyPublicIdAndLocationIdAndLeadTypeAndActiveTrue(
                        agencyPublicId,
                        locationId,
                        leadType
                )
                .isPresent();
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