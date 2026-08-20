package com.scaleup.publicapi.dto;

import com.scaleup.lead.LeadType;

import java.time.LocalDateTime;
import java.util.UUID;

public record LeadCreatedResponse(

        UUID leadId,
        LeadType leadType,
        String status,
        LocalDateTime createdAt

) {
}