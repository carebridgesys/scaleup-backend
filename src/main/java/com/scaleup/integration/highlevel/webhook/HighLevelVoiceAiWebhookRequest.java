package com.scaleup.integration.highlevel.webhook;

import java.util.Map;

public record HighLevelVoiceAiWebhookRequest(

        String locationId,
        String contactId,
        String callId,
        String summary,
        String transcript,
        Map<String, Object> extractedData

) {
}