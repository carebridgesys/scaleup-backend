package com.scaleup.integration.internalcrm;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/agencies")
public class AgencyInternalCrmPipelineMappingController {

    private final AgencyInternalCrmPipelineMappingService
            mappingService;

    public AgencyInternalCrmPipelineMappingController(
            AgencyInternalCrmPipelineMappingService mappingService
    ) {

        this.mappingService =
                mappingService;
    }

    @PostMapping(
            "/{agencyPublicId}/internal-crm-pipeline-mappings"
    )
    public ResponseEntity<Map<String, Object>>
    configureMappings(

            @PathVariable
            UUID agencyPublicId,

            @Valid
            @RequestBody
            InternalCrmPipelineMappingsRequest request
    ) {

        mappingService.configureMappings(
                agencyPublicId,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message",
                        "Internal CRM pipeline mappings configured."
                )
        );
    }
}