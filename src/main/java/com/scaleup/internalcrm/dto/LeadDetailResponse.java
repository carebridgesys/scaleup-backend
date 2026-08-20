package com.scaleup.internalcrm.dto;

import com.scaleup.lead.HighLevelSyncStatus;
import com.scaleup.lead.LeadStatus;
import com.scaleup.lead.LeadType;
import com.scaleup.lead.PreferredContactMethod;

import java.time.LocalDateTime;
import java.util.UUID;

public record LeadDetailResponse(

        UUID leadId,

        LeadType leadType,

        String firstName,

        String lastName,

        String phone,

        String email,

        String zipCode,

        PreferredContactMethod preferredContactMethod,

        String source,

        String campaignName,

        LeadStatus status,

        boolean consentGiven,

        LocalDateTime consentTimestamp,

        UUID agencyId,

        String agencyName,

        UUID campaignId,

        String campaignDisplayName,

        String campaignSource,

        HighLevelSyncStatus highLevelSyncStatus,

        String highLevelContactId,

        String highLevelOpportunityId,

        ClientLeadDetailsResponse clientDetails,

        CaregiverLeadDetailsResponse caregiverDetails,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}