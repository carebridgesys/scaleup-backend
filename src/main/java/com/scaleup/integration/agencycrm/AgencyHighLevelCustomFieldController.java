package com.scaleup.integration.agencycrm;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/agencies")
public class AgencyHighLevelCustomFieldController {

    private final AgencyHighLevelCustomFieldService
            customFieldService;

    public AgencyHighLevelCustomFieldController(
            AgencyHighLevelCustomFieldService customFieldService
    ) {
        this.customFieldService =
                customFieldService;
    }

    @PostMapping(
            "/{agencyPublicId}/highlevel-custom-fields/sync"
    )
    public ResponseEntity<
            AgencyHighLevelCustomFieldService.CustomFieldSyncResult
            >
    syncClientCustomFields(
            @PathVariable UUID agencyPublicId
    ) {

        AgencyHighLevelCustomFieldService.CustomFieldSyncResult result =
                customFieldService
                        .syncClientFields(
                                agencyPublicId
                        );

        return ResponseEntity.ok(
                result
        );
    }
}