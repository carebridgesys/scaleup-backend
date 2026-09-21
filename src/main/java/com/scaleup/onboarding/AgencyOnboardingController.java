package com.scaleup.onboarding;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/agencies")
public class AgencyOnboardingController {

    private final AgencyOnboardingService
            agencyOnboardingService;

    public AgencyOnboardingController(
            AgencyOnboardingService
                    agencyOnboardingService
    ) {
        this.agencyOnboardingService =
                agencyOnboardingService;
    }

    @PostMapping("/onboard")
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyOnboardingResponse onboardAgency(
            @RequestBody AgencyOnboardingRequest request
    ) {
        return agencyOnboardingService
                .onboardAgency(request);
    }
}