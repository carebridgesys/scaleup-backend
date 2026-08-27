package com.scaleup.integration.internalcrm;

import java.util.Objects;
import java.util.UUID;

public record InternalCrmStageRefreshRequestedEvent(
        UUID leadId
) {

    public InternalCrmStageRefreshRequestedEvent {

        Objects.requireNonNull(
                leadId,
                "Lead ID must not be null."
        );
    }
}