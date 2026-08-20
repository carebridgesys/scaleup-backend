package com.scaleup.ai;

import com.scaleup.ai.dto.AiQualificationFinalizeRequest;
import com.scaleup.ai.dto.AiQualificationFinalizeResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/internal/leads/{leadId}/ai-qualification"
)
public class AiQualificationController {

    private final AiQualificationService
            aiQualificationService;

    public AiQualificationController(
            AiQualificationService aiQualificationService
    ) {
        this.aiQualificationService =
                aiQualificationService;
    }

    @PostMapping
    public ResponseEntity<AiQualificationFinalizeResponse>
    finalizeQualification(

            @PathVariable
            UUID leadId,

            @Valid
            @RequestBody
            AiQualificationFinalizeRequest request
    ) {

        return ResponseEntity.ok(
                aiQualificationService
                        .finalizeQualification(
                                leadId,
                                request
                        )
        );
    }
}