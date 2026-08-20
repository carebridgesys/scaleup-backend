package com.scaleup.integration.internalcrm;

import java.util.Objects;
import java.util.UUID;

public record InternalCrmSyncRequestedEvent(
        UUID leadId
) {

    public InternalCrmSyncRequestedEvent {
        Objects.requireNonNull(
                leadId,
                "Lead ID must not be null."
        );
    }
}