package com.scaleup.integration.agencycrm;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/internal/leads/{leadId}/agency-crm"
)
public class AgencyCrmSyncController {

    private final AgencyCrmSyncService
            agencyCrmSyncService;

    public AgencyCrmSyncController(
            AgencyCrmSyncService agencyCrmSyncService
    ) {
        this.agencyCrmSyncService =
                agencyCrmSyncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>>
    syncLead(
            @PathVariable
            UUID leadId
    ) {

        agencyCrmSyncService.syncLead(
                leadId
        );

        return ResponseEntity.ok(
                Map.of(
                        "leadId",
                        leadId,
                        "message",
                        "Agency CRM synchronization processed."
                )
        );
    }
}