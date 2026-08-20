package com.scaleup.integration.internalcrm;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/integrations/internal-crm")
public class InternalCrmSyncController {

    private final InternalCrmSyncService
            internalCrmSyncService;

    public InternalCrmSyncController(
            InternalCrmSyncService internalCrmSyncService
    ) {
        this.internalCrmSyncService =
                internalCrmSyncService;
    }

    @PostMapping("/sync-lead")
    public ResponseEntity<Void> syncLead(
            @RequestParam
            UUID leadId
    ) {

        internalCrmSyncService.syncLead(
                leadId
        );

        return ResponseEntity.noContent().build();
    }
}