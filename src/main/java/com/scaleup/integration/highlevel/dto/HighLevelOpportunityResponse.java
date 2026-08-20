package com.scaleup.integration.highlevel.dto;

public record HighLevelOpportunityResponse(
        Opportunity opportunity
) {

    public record Opportunity(
            String id
    ) {
    }
}