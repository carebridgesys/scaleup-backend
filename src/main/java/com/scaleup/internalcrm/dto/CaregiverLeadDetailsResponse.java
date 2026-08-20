package com.scaleup.internalcrm.dto;

public record CaregiverLeadDetailsResponse(

        Integer yearsExperience,

        String certifications,

        String availability,

        String transportation,

        String preferredSchedule,

        Integer desiredHoursPerWeek,

        String serviceArea,

        String backgroundCheckStatus,

        String interviewStatus,

        Integer aiScreeningScore,

        String aiScreeningSummary

) {
}