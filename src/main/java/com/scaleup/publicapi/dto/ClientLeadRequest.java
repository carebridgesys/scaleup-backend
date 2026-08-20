package com.scaleup.publicapi.dto;

import jakarta.validation.constraints.Size;

public record ClientLeadRequest(

        @Size(max = 100)
        String serviceNeeded,

        @Size(max = 100)
        String careStartTimeline,

        @Size(max = 100)
        String payerType,

        @Size(max = 100)
        String decisionMaker

) {
}