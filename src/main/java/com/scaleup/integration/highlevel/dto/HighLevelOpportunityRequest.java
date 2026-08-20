package com.scaleup.integration.highlevel.dto;

public record HighLevelOpportunityRequest(

        String locationId,
        String pipelineId,
        String pipelineStageId,
        String contactId,
        String name,
        String status

) {
}