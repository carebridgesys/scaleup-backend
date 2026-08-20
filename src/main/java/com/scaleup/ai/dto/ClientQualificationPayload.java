package com.scaleup.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ClientQualificationPayload(

        @Size(max = 100)
        String serviceNeeded,

        @Size(max = 100)
        String careStartTimeline,

        @Size(max = 100)
        String payerType,

        @Size(max = 100)
        String decisionMaker,

        @Min(0)
        @Max(100)
        Integer aiQualificationScore,

        @Size(max = 10000)
        String aiSummary

) {
}