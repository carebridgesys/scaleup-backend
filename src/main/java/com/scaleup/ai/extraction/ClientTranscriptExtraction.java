package com.scaleup.ai.extraction;

public record ClientTranscriptExtraction(

        String serviceNeeded,
        String careStartTimeline,
        String payerType,
        String decisionMaker

) {
}