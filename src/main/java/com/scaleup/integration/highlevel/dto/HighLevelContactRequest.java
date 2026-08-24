package com.scaleup.integration.highlevel.dto;

import java.util.List;

public record HighLevelContactRequest(

        String locationId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String source,

        List<String> tags,

        List<HighLevelCustomFieldValue> customFields

) {
}