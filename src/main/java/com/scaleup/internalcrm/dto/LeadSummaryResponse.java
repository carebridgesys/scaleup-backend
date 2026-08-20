package com.scaleup.internalcrm.dto;

import com.scaleup.lead.HighLevelSyncStatus;
import com.scaleup.lead.LeadStatus;
import com.scaleup.lead.LeadType;
import com.scaleup.lead.PreferredContactMethod;

import java.time.LocalDateTime;
import java.util.UUID;

public record LeadSummaryResponse(

        UUID leadId,

        LeadType leadType,

        String firstName,

        String lastName,

        String phone,

        String email,

        String zipCode,

        PreferredContactMethod preferredContactMethod,

        LeadStatus status,

        boolean consentGiven,

        UUID agencyId,

        String agencyName,

        UUID campaignId,

        String campaignName,

        String campaignSource,

        HighLevelSyncStatus highLevelSyncStatus,

        Integer aiScore,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}