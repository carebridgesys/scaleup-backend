package com.scaleup.integration.highlevel.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/integrations/highlevel/voice-ai")
public class HighLevelVoiceAiWebhookController {

    private final String webhookSecret;

    public HighLevelVoiceAiWebhookController(
            @Value("${highlevel.webhook.secret}")
            String webhookSecret
    ) {
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/completed")
    public ResponseEntity<Void> handleCompletedCall(
            @RequestHeader(
                    value = "X-ScaleUp-Webhook-Secret",
                    required = false
            )
            String providedSecret,

            @RequestBody
            HighLevelVoiceAiWebhookRequest request
    ) {

        if (
                providedSecret == null
                        || !Objects.equals(
                        providedSecret,
                        webhookSecret
                )
        ) {
            return ResponseEntity
                    .status(401)
                    .build();
        }

        System.out.println(
                "HighLevel Voice AI webhook received"
        );

        System.out.println(
                "locationId = "
                        + request.locationId()
        );

        System.out.println(
                "contactId = "
                        + request.contactId()
        );

        System.out.println(
                "callId = "
                        + request.callId()
        );

        System.out.println(
                "summary received = "
                        + (
                        request.summary() != null
                                && !request.summary().isBlank()
                )
        );

        System.out.println(
                "transcript received = "
                        + (
                        request.transcript() != null
                                && !request.transcript().isBlank()
                )
        );

        return ResponseEntity.ok().build();
    }
}