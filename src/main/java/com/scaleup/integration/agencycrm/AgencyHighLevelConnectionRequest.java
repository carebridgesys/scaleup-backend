package com.scaleup.integration.agencycrm;

import jakarta.validation.constraints.NotBlank;

public record AgencyHighLevelConnectionRequest(

        @NotBlank
        String locationId,

        @NotBlank
        String accessToken

) {
}