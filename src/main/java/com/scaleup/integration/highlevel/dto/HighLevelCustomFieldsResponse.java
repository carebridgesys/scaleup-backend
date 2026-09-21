package com.scaleup.integration.highlevel.dto;

import java.util.List;

public record HighLevelCustomFieldsResponse(
        List<HighLevelCustomFieldDefinition> customFields
) {

    public record HighLevelCustomFieldDefinition(
            String id,
            String name,
            String fieldKey,
            String dataType,
            String locationId,
            String model,
            List<String> picklistOptions
    ) {
    }
}