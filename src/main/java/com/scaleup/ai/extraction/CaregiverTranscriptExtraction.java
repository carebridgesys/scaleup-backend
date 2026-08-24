package com.scaleup.ai.extraction;

public record CaregiverTranscriptExtraction(

        Integer yearsExperience,
        String certifications,
        String availability,
        String transportation,
        String preferredSchedule,
        Integer desiredHoursPerWeek,
        String serviceArea

) {
}