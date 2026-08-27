package com.scaleup.integration.highlevel.dto;

public record HighLevelOpportunityUpdateRequest(

        String pipelineId,
        String name,
        String pipelineStageId,
        String status

) {
}