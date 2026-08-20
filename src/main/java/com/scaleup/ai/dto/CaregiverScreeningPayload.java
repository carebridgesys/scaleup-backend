package com.scaleup.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CaregiverScreeningPayload(

        @Min(0)
        Integer yearsExperience,

        @Size(max = 2000)
        String certifications,

        @Size(max = 100)
        String availability,

        @Size(max = 100)
        String transportation,

        @Size(max = 100)
        String preferredSchedule,

        @Min(0)
        @Max(168)
        Integer desiredHoursPerWeek,

        @Size(max = 255)
        String serviceArea,

        @Min(0)
        @Max(100)
        Integer aiScreeningScore,

        @Size(max = 10000)
        String aiScreeningSummary

) {
}