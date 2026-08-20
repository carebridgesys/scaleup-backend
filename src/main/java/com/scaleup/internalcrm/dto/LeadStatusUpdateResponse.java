package com.scaleup.internalcrm.dto;

import com.scaleup.lead.LeadStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LeadStatusUpdateResponse(

        UUID leadId,

        LeadStatus previousStatus,

        LeadStatus status,

        LocalDateTime updatedAt

) {
}