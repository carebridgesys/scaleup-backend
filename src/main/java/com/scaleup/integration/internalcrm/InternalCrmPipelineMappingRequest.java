package com.scaleup.integration.internalcrm;

import jakarta.validation.constraints.NotBlank;

public record InternalCrmPipelineMappingRequest(

        @NotBlank
        String pipelineId,

        @NotBlank
        String initialStageId,

        @NotBlank
        String attemptingContactStageId,

        @NotBlank
        String contactedStageId,

        @NotBlank
        String qualifiedStageId,

        @NotBlank
        String routedStageId,

        String pipelineName,

        String initialStageName

) {
}