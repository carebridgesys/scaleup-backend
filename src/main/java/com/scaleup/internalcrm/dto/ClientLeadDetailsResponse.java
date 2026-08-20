package com.scaleup.internalcrm.dto;

public record ClientLeadDetailsResponse(

        String serviceNeeded,

        String careStartTimeline,

        String payerType,

        String decisionMaker,

        Integer aiQualificationScore,

        String aiSummary

) {
}