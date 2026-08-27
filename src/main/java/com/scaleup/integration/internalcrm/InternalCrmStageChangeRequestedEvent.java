package com.scaleup.integration.internalcrm;

import java.util.Objects;
import java.util.UUID;

public record InternalCrmStageChangeRequestedEvent(

        UUID leadId,
        InternalCrmOpportunityStage stage

) {

    public InternalCrmStageChangeRequestedEvent {

        Objects.requireNonNull(
                leadId,
                "Lead ID must not be null."
        );

        Objects.requireNonNull(
                stage,
                "Stage must not be null."
        );
    }
}