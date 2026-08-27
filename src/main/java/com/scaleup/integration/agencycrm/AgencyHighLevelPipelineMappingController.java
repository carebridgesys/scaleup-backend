package com.scaleup.integration.agencycrm;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/agencies")
public class AgencyHighLevelPipelineMappingController {

    private final AgencyHighLevelPipelineMappingService
            mappingService;

    public AgencyHighLevelPipelineMappingController(
            AgencyHighLevelPipelineMappingService mappingService
    ) {

        this.mappingService =
                mappingService;
    }

    @PostMapping(
            "/{agencyPublicId}/highlevel-pipeline-mappings"
    )
    public ResponseEntity<Map<String, Object>>
    configureMappings(

            @PathVariable
            UUID agencyPublicId,

            @Valid
            @RequestBody
            AgencyHighLevelPipelineMappingsRequest request
    ) {

        List<AgencyPipelineMappingResponse> mappings =
                mappingService
                        .configureMappings(
                                agencyPublicId,
                                request
                        );

        return ResponseEntity.ok(
                Map.of(
                        "success",
                        true,
                        "agencyId",
                        agencyPublicId,
                        "mappings",
                        mappings
                )
        );
    }

    @GetMapping(
            "/{agencyPublicId}/highlevel-pipeline-mappings"
    )
    public ResponseEntity<Map<String, Object>>
    getMappings(

            @PathVariable
            UUID agencyPublicId
    ) {

        List<AgencyPipelineMappingResponse> mappings =
                mappingService
                        .getMappings(
                                agencyPublicId
                        );

        return ResponseEntity.ok(
                Map.of(
                        "success",
                        true,
                        "agencyId",
                        agencyPublicId,
                        "mappings",
                        mappings
                )
        );
    }
}