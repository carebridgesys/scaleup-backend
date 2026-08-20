package com.scaleup.ai;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiContactResponse(

        UUID leadId,

        AiContactStatus status,

        String provider,

        String externalCallId,

        int attemptCount,

        LocalDateTime startedAt,

        LocalDateTime completedAt,

        String lastError,

        String transcriptReference

) {

    public static AiContactResponse from(
            LeadAiContact contact
    ) {

        return new AiContactResponse(
                contact.getLead()
                        .getPublicId(),

                contact.getStatus(),

                contact.getProvider(),

                contact.getExternalCallId(),

                contact.getAttemptCount(),

                contact.getStartedAt(),

                contact.getCompletedAt(),

                contact.getLastError(),

                contact.getTranscriptReference()
        );
    }
}