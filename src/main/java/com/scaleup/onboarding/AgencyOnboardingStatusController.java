package com.scaleup.onboarding;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/internal/agencies/{agencyPublicId}"
)
public class AgencyOnboardingStatusController {

    private final AgencyOnboardingStatusService
            agencyOnboardingStatusService;

    public AgencyOnboardingStatusController(
            AgencyOnboardingStatusService
                    agencyOnboardingStatusService
    ) {
        this.agencyOnboardingStatusService =
                agencyOnboardingStatusService;
    }

    @GetMapping("/onboarding-status")
    public AgencyOnboardingStatusResponse
    getOnboardingStatus(
            @PathVariable UUID agencyPublicId
    ) {

        return agencyOnboardingStatusService
                .getStatus(
                        agencyPublicId
                );
    }
}