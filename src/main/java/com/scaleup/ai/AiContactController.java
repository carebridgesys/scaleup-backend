package com.scaleup.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/internal/leads/{leadId}/ai-contact"
)
public class AiContactController {

    private final AiContactLifecycleService
            aiContactLifecycleService;

    public AiContactController(
            AiContactLifecycleService aiContactLifecycleService
    ) {
        this.aiContactLifecycleService =
                aiContactLifecycleService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<AiContactResponse>
    initialize(
            @PathVariable
            UUID leadId
    ) {

        return ResponseEntity.ok(
                AiContactResponse.from(
                        aiContactLifecycleService
                                .initializeAiContact(
                                        leadId
                                )
                )
        );
    }

    @PatchMapping("/start")
    public ResponseEntity<AiContactResponse>
    start(

            @PathVariable
            UUID leadId,

            @RequestParam(required = false)
            String provider,

            @RequestParam(required = false)
            String externalCallId
    ) {

        return ResponseEntity.ok(
                AiContactResponse.from(
                        aiContactLifecycleService
                                .markInProgress(
                                        leadId,
                                        provider,
                                        externalCallId
                                )
                )
        );
    }

    @PatchMapping("/complete")
    public ResponseEntity<AiContactResponse>
    complete(

            @PathVariable
            UUID leadId,

            @RequestParam(required = false)
            String transcriptReference
    ) {

        return ResponseEntity.ok(
                AiContactResponse.from(
                        aiContactLifecycleService
                                .markCompleted(
                                        leadId,
                                        transcriptReference
                                )
                )
        );
    }

    @PatchMapping("/fail")
    public ResponseEntity<AiContactResponse>
    fail(

            @PathVariable
            UUID leadId,

            @RequestParam
            String error
    ) {

        return ResponseEntity.ok(
                AiContactResponse.from(
                        aiContactLifecycleService
                                .markFailed(
                                        leadId,
                                        error
                                )
                )
        );
    }
}