package com.scaleup.campaign;

import java.util.UUID;

public record CampaignResponse(
        UUID campaignPublicId,
        UUID agencyPublicId,
        String agencyName,
        String campaignName,
        String campaignSlug,
        String campaignType,
        String source,
        String externalCampaignId,
        String campaignKey,
        String landingPath,
        boolean active
) {
}