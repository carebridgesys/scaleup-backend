package com.scaleup.integration.internalcrm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record InternalCrmPipelineMappingsRequest(

        @NotNull
        @Valid
        InternalCrmPipelineMappingRequest client,

        @NotNull
        @Valid
        InternalCrmPipelineMappingRequest caregiver

) {
}