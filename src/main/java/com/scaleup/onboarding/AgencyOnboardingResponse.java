package com.scaleup.onboarding;

import java.util.List;
import java.util.UUID;

public record AgencyOnboardingResponse(
        UUID agencyPublicId,
        String agencyName,
        String agencySlug,
        List<CampaignResponse> campaigns
) {

    public record CampaignResponse(
            UUID campaignPublicId,
            String campaignType,
            String campaignName,
            String campaignKey,
            String landingPath
    ) {
    }
}