package com.scaleup.onboarding;

import java.util.UUID;

public record AgencyOnboardingStatusResponse(

        UUID agencyPublicId,
        String agencyName,
        String agencySlug,
        boolean agencyActive,

        CampaignStatus campaigns,

        HighLevelConnectionStatus highLevel,

        PipelineStatus internalCrmMappings,

        PipelineStatus agencyCrmMappings,

        boolean readyForAds

) {

    public record CampaignStatus(
            boolean clientConfigured,
            boolean caregiverConfigured
    ) {
    }

    public record HighLevelConnectionStatus(
            boolean connected,
            String connectionStatus,
            String locationId
    ) {
    }

    public record PipelineStatus(
            boolean clientConfigured,
            boolean caregiverConfigured
    ) {
    }
}