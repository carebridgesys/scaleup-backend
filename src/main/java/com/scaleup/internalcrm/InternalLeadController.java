package com.scaleup.internalcrm;

import com.scaleup.internalcrm.dto.LeadDetailResponse;
import com.scaleup.internalcrm.dto.LeadStatusUpdateResponse;
import com.scaleup.internalcrm.dto.LeadSummaryResponse;
import com.scaleup.internalcrm.dto.PageResponse;
import com.scaleup.internalcrm.dto.UpdateLeadStatusRequest;
import com.scaleup.lead.LeadStatus;
import com.scaleup.lead.LeadType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/leads")
public class InternalLeadController {

    private final InternalLeadQueryService
            internalLeadQueryService;

    private final InternalLeadCommandService
            internalLeadCommandService;

    public InternalLeadController(
            InternalLeadQueryService internalLeadQueryService,
            InternalLeadCommandService internalLeadCommandService
    ) {
        this.internalLeadQueryService =
                internalLeadQueryService;

        this.internalLeadCommandService =
                internalLeadCommandService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<LeadSummaryResponse>>
    getLeads(

            @RequestParam(required = false)
            UUID agencyId,

            @RequestParam(required = false)
            UUID campaignId,

            @RequestParam(required = false)
            LeadType leadType,

            @RequestParam(required = false)
            LeadStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        return ResponseEntity.ok(
                internalLeadQueryService.getLeads(
                        agencyId,
                        campaignId,
                        leadType,
                        status,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadDetailResponse> getLead(
            @PathVariable
            UUID leadId
    ) {

        return ResponseEntity.ok(
                internalLeadQueryService.getLead(
                        leadId
                )
        );
    }

    @PatchMapping("/{leadId}/status")
    public ResponseEntity<LeadStatusUpdateResponse>
    updateLeadStatus(

            @PathVariable
            UUID leadId,

            @Valid
            @RequestBody
            UpdateLeadStatusRequest request
    ) {

        return ResponseEntity.ok(
                internalLeadCommandService.updateStatus(
                        leadId,
                        request
                )
        );
    }
}