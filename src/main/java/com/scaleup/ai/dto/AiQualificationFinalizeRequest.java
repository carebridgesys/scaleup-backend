package com.scaleup.ai.dto;

import jakarta.validation.Valid;

public record AiQualificationFinalizeRequest(

        @Valid
        ClientQualificationPayload clientQualification,

        @Valid
        CaregiverScreeningPayload caregiverScreening

) {
}