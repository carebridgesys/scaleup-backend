package com.scaleup.integration.agencycrm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AgencyHighLevelPipelineMappingsRequest(

        @NotNull
        @Valid
        AgencyPipelineMappingRequest client,

        @NotNull
        @Valid
        AgencyPipelineMappingRequest caregiver

) {
}