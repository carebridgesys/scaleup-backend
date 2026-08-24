package com.scaleup.integration.agencycrm;

import java.util.Objects;
import java.util.UUID;

public record AgencyCrmSyncRequestedEvent(
        UUID leadId
) {

    public AgencyCrmSyncRequestedEvent {
        Objects.requireNonNull(
                leadId,
                "Lead ID must not be null."
        );
    }
}