package com.scaleup.integration.agencycrm;

public record AgencyPipelineMappingResponse(

        String leadType,
        String pipelineId,
        String initialStageId,
        String pipelineName,
        String initialStageName,
        boolean active

) {
}