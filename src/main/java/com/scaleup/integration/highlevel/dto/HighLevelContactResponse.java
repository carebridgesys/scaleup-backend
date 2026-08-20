package com.scaleup.integration.highlevel.dto;

public record HighLevelContactResponse(
        Contact contact
) {

    public record Contact(
            String id
    ) {
    }
}