package com.scaleup.internalcrm;

import com.scaleup.common.exception.InvalidLeadStatusTransitionException;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.internalcrm.dto.LeadStatusUpdateResponse;
import com.scaleup.internalcrm.dto.UpdateLeadStatusRequest;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import com.scaleup.lead.LeadStatus;
import com.scaleup.lead.LeadStatusTransitionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InternalLeadCommandService {

    private final LeadRepository leadRepository;

    private final LeadStatusTransitionPolicy
            leadStatusTransitionPolicy;

    public InternalLeadCommandService(
            LeadRepository leadRepository,
            LeadStatusTransitionPolicy leadStatusTransitionPolicy
    ) {
        this.leadRepository =
                leadRepository;

        this.leadStatusTransitionPolicy =
                leadStatusTransitionPolicy;
    }

    @Transactional
    public LeadStatusUpdateResponse updateStatus(
            UUID leadId,
            UpdateLeadStatusRequest request
    ) {

        Lead lead =
                leadRepository
                        .findByPublicId(leadId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Lead was not found."
                                )
                        );

        LeadStatus currentStatus =
                lead.getStatus();

        LeadStatus targetStatus =
                request.status();

        if (
                !leadStatusTransitionPolicy.isAllowed(
                        currentStatus,
                        targetStatus
                )
        ) {
            throw new InvalidLeadStatusTransitionException(
                    "Lead status cannot transition from "
                            + currentStatus
                            + " to "
                            + targetStatus
                            + "."
            );
        }

        /*
         * PATCH requests should be safe to retry.
         * If the lead is already in the requested state,
         * return the current representation without
         * performing a database update.
         */
        if (currentStatus == targetStatus) {
            return new LeadStatusUpdateResponse(
                    lead.getPublicId(),
                    currentStatus,
                    currentStatus,
                    lead.getUpdatedAt()
            );
        }

        lead.changeStatus(targetStatus);

        /*
         * Flush intentionally so @PreUpdate executes
         * before we construct the API response.
         */
        Lead savedLead =
                leadRepository.saveAndFlush(lead);

        return new LeadStatusUpdateResponse(
                savedLead.getPublicId(),
                currentStatus,
                savedLead.getStatus(),
                savedLead.getUpdatedAt()
        );
    }
}