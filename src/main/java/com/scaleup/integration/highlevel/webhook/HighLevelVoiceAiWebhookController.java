package com.scaleup.integration.highlevel.webhook;

import com.scaleup.integration.highlevel.HighLevelProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/integrations/highlevel/voice-ai")
public class HighLevelVoiceAiWebhookController {

    private static final Logger log =
            LoggerFactory.getLogger(
                    HighLevelVoiceAiWebhookController.class
            );

    private static final Set<String> SUPPORTED_AGENT_TYPES =
            Set.of(
                    "CLIENT",
                    "CAREGIVER"
            );

    /*
     * Defensive payload limits.
     * These are intentionally generous for Voice AI transcripts.
     */
    private static final int MAX_CONTACT_ID_LENGTH =
            200;

    private static final int MAX_CALL_ID_LENGTH =
            250;

    private static final int MAX_AGENT_TYPE_LENGTH =
            30;

    private static final int MAX_SUMMARY_LENGTH =
            20_000;

    private static final int MAX_TRANSCRIPT_LENGTH =
            250_000;

    private final String webhookSecret;

    private final String internalCrmLocationId;

    private final HighLevelVoiceAiWebhookService
            webhookService;

    public HighLevelVoiceAiWebhookController(
            HighLevelProperties properties,
            HighLevelVoiceAiWebhookService webhookService
    ) {

        if (
                properties.getWebhook() == null
                        || properties
                        .getWebhook()
                        .getSecret() == null
                        || properties
                        .getWebhook()
                        .getSecret()
                        .isBlank()
        ) {

            throw new IllegalStateException(
                    "HighLevel webhook secret is not configured."
            );
        }

        if (
                properties.getInternalCrm() == null
                        || properties
                        .getInternalCrm()
                        .getLocationId() == null
                        || properties
                        .getInternalCrm()
                        .getLocationId()
                        .isBlank()
        ) {

            throw new IllegalStateException(
                    "HighLevel Internal CRM location ID is not configured."
            );
        }

        this.webhookSecret =
                properties
                        .getWebhook()
                        .getSecret()
                        .trim();

        this.internalCrmLocationId =
                properties
                        .getInternalCrm()
                        .getLocationId()
                        .trim();

        this.webhookService =
                webhookService;
    }

    @PostMapping("/completed")
    public ResponseEntity<Map<String, Object>>
    handleCompletedCall(

            @RequestHeader(
                    value = "X-ScaleUp-Webhook-Secret",
                    required = false
            )
            String providedSecret,

            @RequestBody(required = false)
            HighLevelVoiceAiWebhookRequest request
    ) {

        /*
         * Authenticate before processing any
         * webhook-controlled data.
         */
        if (
                !isValidSecret(
                        providedSecret
                )
        ) {

            log.warn(
                    "Rejected HighLevel Voice AI webhook due to invalid authentication."
            );

            return error(
                    401,
                    "INVALID_WEBHOOK_SECRET"
            );
        }

        if (request == null) {

            return error(
                    400,
                    "MISSING_REQUEST_BODY"
            );
        }

        String locationId =
                normalizeNullable(
                        request.locationId()
                );

        if (locationId == null) {

            return error(
                    400,
                    "MISSING_LOCATION_ID"
            );
        }

        /*
         * Tenant-boundary protection:
         * this endpoint is for our Internal CRM only.
         */
        if (
                !internalCrmLocationId.equals(
                        locationId
                )
        ) {

            log.warn(
                    "Rejected HighLevel Voice AI webhook for unexpected location."
            );

            return error(
                    403,
                    "INVALID_LOCATION"
            );
        }

        String contactId =
                normalizeNullable(
                        request.contactId()
                );

        if (contactId == null) {

            return error(
                    400,
                    "MISSING_CONTACT_ID"
            );
        }

        if (
                contactId.length()
                        > MAX_CONTACT_ID_LENGTH
        ) {

            return error(
                    400,
                    "CONTACT_ID_TOO_LONG"
            );
        }

        String callId =
                normalizeNullable(
                        request.callId()
                );

        if (
                callId != null
                        && callId.length()
                        > MAX_CALL_ID_LENGTH
        ) {

            return error(
                    400,
                    "CALL_ID_TOO_LONG"
            );
        }

        String normalizedAgentType =
                normalizeAgentType(
                        request.agentType()
                );

        if (
                normalizedAgentType == null
                        || normalizedAgentType.length()
                        > MAX_AGENT_TYPE_LENGTH
                        || !SUPPORTED_AGENT_TYPES.contains(
                        normalizedAgentType
                )
        ) {

            return error(
                    400,
                    "INVALID_AGENT_TYPE"
            );
        }

        if (
                request.transcript() == null
                        || request
                        .transcript()
                        .isBlank()
        ) {

            return error(
                    400,
                    "MISSING_TRANSCRIPT"
            );
        }

        if (
                request.transcript().length()
                        > MAX_TRANSCRIPT_LENGTH
        ) {

            return error(
                    413,
                    "TRANSCRIPT_TOO_LARGE"
            );
        }

        if (
                request.summary() != null
                        && request.summary().length()
                        > MAX_SUMMARY_LENGTH
        ) {

            return error(
                    413,
                    "SUMMARY_TOO_LARGE"
            );
        }

        /*
         * Do not catch unexpected processing exceptions here.
         *
         * If database/HighLevel processing genuinely fails,
         * returning a server error is preferable so the
         * webhook provider can retry delivery.
         */
        webhookService.processCompletedCall(
                request
        );

        log.info(
                "HighLevel Voice AI completion webhook processed successfully. agentType={}",
                normalizedAgentType
        );

        return ResponseEntity
                .ok(
                        Map.of(
                                "success",
                                true,
                                "message",
                                "Voice AI webhook accepted"
                        )
                );
    }

    private boolean isValidSecret(
            String providedSecret
    ) {

        if (
                providedSecret == null
                        || providedSecret.isBlank()
        ) {
            return false;
        }

        byte[] expected =
                webhookSecret.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] actual =
                providedSecret
                        .trim()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        return MessageDigest.isEqual(
                expected,
                actual
        );
    }

    private ResponseEntity<Map<String, Object>>
    error(
            int status,
            String errorCode
    ) {

        return ResponseEntity
                .status(status)
                .body(
                        Map.of(
                                "success",
                                false,
                                "error",
                                errorCode
                        )
                );
    }

    private String normalizeAgentType(
            String agentType
    ) {

        String normalized =
                normalizeNullable(
                        agentType
                );

        if (normalized == null) {
            return null;
        }

        return normalized
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String normalizeNullable(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}