package com.scaleup.integration.highlevel.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/integrations/highlevel/voice-ai")
public class HighLevelVoiceAiWebhookController {

    private static final Set<String> SUPPORTED_AGENT_TYPES =
            Set.of(
                    "CLIENT",
                    "CAREGIVER"
            );

    private final String webhookSecret;
    private final HighLevelVoiceAiWebhookService webhookService;

    public HighLevelVoiceAiWebhookController(
            @Value("${highlevel.webhook.secret}")
            String webhookSecret,

            HighLevelVoiceAiWebhookService webhookService
    ) {
        this.webhookSecret =
                webhookSecret;

        this.webhookService =
                webhookService;
    }

    @PostMapping("/completed")
    public ResponseEntity<Map<String, Object>> handleCompletedCall(
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
                        providedSecret.trim(),
                        webhookSecret.trim()
                )
        ) {

            System.out.println(
                    "HighLevel Voice AI webhook rejected: invalid webhook secret"
            );

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "success", false,
                                    "error", "INVALID_WEBHOOK_SECRET"
                            )
                    );
        }

        if (
                request.contactId() == null
                        || request.contactId().isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "error", "MISSING_CONTACT_ID"
                            )
                    );
        }

        String normalizedAgentType =
                normalizeAgentType(
                        request.agentType()
                );

        if (
                normalizedAgentType == null
                        || !SUPPORTED_AGENT_TYPES.contains(
                        normalizedAgentType
                )
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "error", "INVALID_AGENT_TYPE"
                            )
                    );
        }

        System.out.println(
                "========================================"
        );

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
                "agentType = "
                        + normalizedAgentType
        );

        System.out.println(
                "summary received = "
                        + hasText(
                        request.summary()
                )
        );

        System.out.println(
                "transcript received = "
                        + hasText(
                        request.transcript()
                )
        );

        webhookService.processCompletedCall(
                request
        );

        System.out.println(
                "========================================"
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "success",
                true
        );

        response.put(
                "message",
                "Voice AI webhook received"
        );

        response.put(
                "agentType",
                normalizedAgentType
        );

        response.put(
                "contactId",
                request.contactId()
        );

        return ResponseEntity
                .ok(response);
    }

    private String normalizeAgentType(
            String agentType
    ) {

        if (
                agentType == null
                        || agentType.isBlank()
        ) {
            return null;
        }

        return agentType
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.isBlank();
    }
}