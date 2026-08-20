package com.scaleup.ai.dto;

import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.lead.LeadType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiQualificationFinalizeResponse(

        UUID leadId,

        LeadType leadType,

        Integer aiScore,

        String aiSummary,

        CrmSyncStatus agencyCrmSyncStatus,

        LocalDateTime qualifiedAt

) {
}