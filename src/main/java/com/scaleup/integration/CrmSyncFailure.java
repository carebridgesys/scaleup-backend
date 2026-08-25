package com.scaleup.integration;

public record CrmSyncFailure(

        boolean retryable,
        String message

) {
}