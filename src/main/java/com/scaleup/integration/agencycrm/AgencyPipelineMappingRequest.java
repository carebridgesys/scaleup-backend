package com.scaleup.integration.agencycrm;

import jakarta.validation.constraints.NotBlank;

public record AgencyPipelineMappingRequest(

        @NotBlank
        String pipelineId,

        @NotBlank
        String initialStageId,

        String pipelineName,

        String initialStageName

) {
}