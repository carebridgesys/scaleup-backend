package com.scaleup.onboarding;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import com.scaleup.campaign.Campaign;
import com.scaleup.campaign.CampaignRepository;
import com.scaleup.campaign.CampaignType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AgencyOnboardingService {

    private static final String LANDING_PAGE_SOURCE =
            "LANDING_PAGE";

    private final AgencyRepository agencyRepository;
    private final CampaignRepository campaignRepository;

    public AgencyOnboardingService(
            AgencyRepository agencyRepository,
            CampaignRepository campaignRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public AgencyOnboardingResponse onboardAgency(
            AgencyOnboardingRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Onboarding request must not be null."
            );
        }

        String agencyName = requireText(
                request.getAgencyName(),
                "Agency name"
        );

        String agencySlug = normalizeSlug(
                request.getAgencySlug()
        );

        if (
                !request.isCreateClientCampaign()
                        && !request.isCreateCaregiverCampaign()
        ) {
            throw new IllegalArgumentException(
                    "At least one acquisition program must be enabled."
            );
        }

        if (agencyRepository.existsBySlug(agencySlug)) {
            throw new IllegalArgumentException(
                    "An agency already exists with slug: "
                            + agencySlug
            );
        }

        Agency agency = new Agency(
                agencyName,
                agencySlug
        );

        agency = agencyRepository.save(agency);

        List<Campaign> campaigns =
                new ArrayList<>();

        int year = Year.now().getValue();

        if (request.isCreateClientCampaign()) {
            campaigns.add(
                    createCampaign(
                            agency,
                            CampaignType.CLIENT,
                            year
                    )
            );
        }

        if (request.isCreateCaregiverCampaign()) {
            campaigns.add(
                    createCampaign(
                            agency,
                            CampaignType.CAREGIVER,
                            year
                    )
            );
        }

        List<Campaign> savedCampaigns =
                campaignRepository.saveAll(campaigns);

        return buildResponse(
                agency,
                savedCampaigns
        );
    }

    private Campaign createCampaign(
            Agency agency,
            CampaignType campaignType,
            int year
    ) {
        String typePart =
                campaignType
                        .name()
                        .toLowerCase(Locale.ROOT);

        String campaignKey =
                agency.getSlug()
                        + "-"
                        + typePart
                        + "-"
                        + year;

        String campaignSlug =
                campaignKey;

        if (
                campaignRepository.existsByLandingPageKey(
                        campaignKey
                )
        ) {
            throw new IllegalArgumentException(
                    "Campaign key already exists: "
                            + campaignKey
            );
        }

        if (
                campaignRepository
                        .existsByAgencyPublicIdAndSlug(
                                agency.getPublicId(),
                                campaignSlug
                        )
        ) {
            throw new IllegalArgumentException(
                    "Campaign slug already exists for agency: "
                            + campaignSlug
            );
        }

        String campaignName =
                switch (campaignType) {
                    case CLIENT ->
                            agency.getName()
                                    + " - Client Acquisition";

                    case CAREGIVER ->
                            agency.getName()
                                    + " - Caregiver Acquisition";
                };

        Campaign campaign =
                new Campaign(
                        agency,
                        campaignName,
                        campaignSlug,
                        campaignType,
                        campaignKey
                );

        campaign.updateAttribution(
                LANDING_PAGE_SOURCE,
                null
        );

        return campaign;
    }

    private AgencyOnboardingResponse buildResponse(
            Agency agency,
            List<Campaign> campaigns
    ) {
        List<AgencyOnboardingResponse.CampaignResponse>
                campaignResponses =
                campaigns.stream()
                        .map(this::toCampaignResponse)
                        .toList();

        return new AgencyOnboardingResponse(
                agency.getPublicId(),
                agency.getName(),
                agency.getSlug(),
                campaignResponses
        );
    }

    private AgencyOnboardingResponse.CampaignResponse
    toCampaignResponse(
            Campaign campaign
    ) {
        String landingPath =
                switch (campaign.getCampaignType()) {
                    case CLIENT ->
                            "/client?campaign="
                                    + campaign.getLandingPageKey();

                    case CAREGIVER ->
                            "/caregiver?campaign="
                                    + campaign.getLandingPageKey();
                };

        return new AgencyOnboardingResponse.CampaignResponse(
                campaign.getPublicId(),
                campaign.getCampaignType().name(),
                campaign.getName(),
                campaign.getLandingPageKey(),
                landingPath
        );
    }

    private String normalizeSlug(
            String value
    ) {
        String slug = requireText(
                value,
                "Agency slug"
        )
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Agency slug is invalid."
            );
        }

        if (slug.length() > 120) {
            throw new IllegalArgumentException(
                    "Agency slug must not exceed 120 characters."
            );
        }

        return slug;
    }

    private String requireText(
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
}