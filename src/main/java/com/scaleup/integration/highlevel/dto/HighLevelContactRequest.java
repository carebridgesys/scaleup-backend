package com.scaleup.integration.highlevel.dto;

import java.util.List;

public record HighLevelContactRequest(

        String locationId,
        String firstName,
        String lastName,
        String email,
        String phone,

        /*
         * Native HighLevel contact postal-code field.
         * Do not duplicate ZIP as a custom field.
         */
        String postalCode,

        String source,

        List<String> tags,

        List<HighLevelCustomFieldValue> customFields

) {
}