package com.scaleup.integration.agencycrm;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /*
     * Existing CLIENT custom-field synchronization endpoint.
     *
     * Keep this route unchanged for backward compatibility.
     */
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

    /*
     * CAREGIVER custom-field synchronization endpoint.
     */
    @PostMapping(
            "/{agencyPublicId}/highlevel-custom-fields/caregiver/sync"
    )
    public ResponseEntity<
            AgencyHighLevelCustomFieldService.CustomFieldSyncResult
            >
    syncCaregiverCustomFields(
            @PathVariable UUID agencyPublicId
    ) {

        AgencyHighLevelCustomFieldService.CustomFieldSyncResult result =
                customFieldService
                        .syncCaregiverFields(
                                agencyPublicId
                        );

        return ResponseEntity.ok(
                result
        );
    }
}