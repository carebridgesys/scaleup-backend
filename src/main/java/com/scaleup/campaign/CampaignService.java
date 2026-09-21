package com.scaleup.campaign;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import java.util.List;

@Service
public class CampaignService {

    private static final String DEFAULT_SOURCE =
            "LANDING_PAGE";

    private final AgencyRepository agencyRepository;
    private final CampaignRepository campaignRepository;

    public CampaignService(
            AgencyRepository agencyRepository,
            CampaignRepository campaignRepository
    ) {
        this.agencyRepository = agencyRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public CampaignResponse createCampaign(
            UUID agencyPublicId,
            CreateCampaignRequest request
    ) {
        if (agencyPublicId == null) {
            throw new IllegalArgumentException(
                    "Agency public ID must not be null."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Campaign request must not be null."
            );
        }

        Agency agency = agencyRepository
                .findByPublicId(agencyPublicId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Agency was not found: "
                                        + agencyPublicId
                        )
                );

        if (!agency.isActive()) {
            throw new IllegalArgumentException(
                    "Campaign cannot be created for an inactive agency."
            );
        }

        String campaignName =
                requireText(
                        request.getCampaignName(),
                        "Campaign name"
                );

        if (campaignName.length() > 200) {
            throw new IllegalArgumentException(
                    "Campaign name must not exceed 200 characters."
            );
        }

        CampaignType campaignType =
                request.getCampaignType();

        if (campaignType == null) {
            throw new IllegalArgumentException(
                    "Campaign type must not be null."
            );
        }

        String campaignSlug =
                normalizeSlug(
                        request.getCampaignSlug(),
                        "Campaign slug"
                );

        if (campaignSlug.length() > 150) {
            throw new IllegalArgumentException(
                    "Campaign slug must not exceed 150 characters."
            );
        }

        if (
                campaignRepository
                        .existsByAgencyPublicIdAndSlug(
                                agencyPublicId,
                                campaignSlug
                        )
        ) {
            throw new IllegalArgumentException(
                    "Campaign slug already exists for this agency: "
                            + campaignSlug
            );
        }

        String campaignKey =
                buildCampaignKey(
                        agency,
                        campaignSlug
                );

        if (campaignKey.length() > 150) {
            throw new IllegalArgumentException(
                    "Generated campaign key must not exceed 150 characters."
            );
        }

        if (
                campaignRepository
                        .existsByLandingPageKey(
                                campaignKey
                        )
        ) {
            throw new IllegalArgumentException(
                    "Campaign key already exists: "
                            + campaignKey
            );
        }

        String source =
                normalizeOptionalText(
                        request.getSource()
                );

        if (source == null) {
            source = DEFAULT_SOURCE;
        }

        if (source.length() > 100) {
            throw new IllegalArgumentException(
                    "Campaign source must not exceed 100 characters."
            );
        }

        String externalCampaignId =
                normalizeOptionalText(
                        request.getExternalCampaignId()
                );

        if (
                externalCampaignId != null
                        && externalCampaignId.length() > 150
        ) {
            throw new IllegalArgumentException(
                    "External campaign ID must not exceed 150 characters."
            );
        }

        Campaign campaign =
                new Campaign(
                        agency,
                        campaignName,
                        campaignSlug,
                        campaignType,
                        campaignKey
                );

        campaign.updateAttribution(
                source,
                externalCampaignId
        );

        Campaign savedCampaign =
                campaignRepository.save(campaign);

        return toResponse(savedCampaign);
    }

    private String buildCampaignKey(
            Agency agency,
            String campaignSlug
    ) {
        String agencyPrefix =
                agency.getSlug() + "-";

        if (
                campaignSlug.startsWith(
                        agencyPrefix
                )
        ) {
            return campaignSlug;
        }

        return agencyPrefix + campaignSlug;
    }

    private CampaignResponse toResponse(
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

        return new CampaignResponse(
                campaign.getPublicId(),
                campaign.getAgency().getPublicId(),
                campaign.getAgency().getName(),
                campaign.getName(),
                campaign.getSlug(),
                campaign.getCampaignType().name(),
                campaign.getSource(),
                campaign.getExternalCampaignId(),
                campaign.getLandingPageKey(),
                landingPath,
                campaign.isActive()
        );
    }

    private String normalizeSlug(
            String value,
            String fieldName
    ) {
        String slug =
                requireText(
                        value,
                        fieldName
                )
                        .toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^a-z0-9]+",
                                "-"
                        )
                        .replaceAll(
                                "^-+|-+$",
                                ""
                        );

        if (slug.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is invalid."
            );
        }

        return slug;
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

    private String normalizeOptionalText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaigns(
            UUID agencyPublicId
    ) {
        if (agencyPublicId == null) {
            throw new IllegalArgumentException(
                    "Agency public ID must not be null."
            );
        }

        Agency agency = agencyRepository
                .findByPublicId(agencyPublicId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Agency was not found: "
                                        + agencyPublicId
                        )
                );

        return campaignRepository
                .findByAgencyPublicId(
                        agency.getPublicId(),
                        org.springframework.data.domain.Pageable.unpaged()
                )
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}