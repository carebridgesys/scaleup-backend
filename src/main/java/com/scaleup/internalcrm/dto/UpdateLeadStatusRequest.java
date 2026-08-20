package com.scaleup.internalcrm.dto;

import com.scaleup.lead.LeadStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLeadStatusRequest(

        @NotNull(
                message = "Lead status is required."
        )
        LeadStatus status

) {
}