package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AgencyCrmSyncService {

    private final LeadRepository
            leadRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final AgencyCrmClient
            agencyCrmClient;

    public AgencyCrmSyncService(
            LeadRepository leadRepository,
            LeadCrmSyncRepository leadCrmSyncRepository,
            AgencyCrmClient agencyCrmClient
    ) {
        this.leadRepository =
                leadRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.agencyCrmClient =
                agencyCrmClient;
    }

    @Transactional
    public void syncLead(
            UUID leadPublicId
    ) {

        Lead lead =
                leadRepository
                        .findByPublicId(
                                leadPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Lead was not found."
                                )
                        );

        Agency agency =
                lead.getAgency();

        validateAgency(
                lead,
                agency
        );

        LeadCrmSync sync =
                leadCrmSyncRepository
                        .findByLeadPublicIdAndDestination(
                                leadPublicId,
                                CrmDestination.AGENCY_CRM
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Agency CRM sync record was not found."
                                )
                        );

        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.SYNCED
        ) {
            return;
        }

        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.PROCESSING
        ) {
            return;
        }

        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.NOT_REQUIRED
        ) {
            throw new IllegalStateException(
                    "Agency CRM synchronization is marked as not required."
            );
        }

        sync.markProcessing();

        leadCrmSyncRepository
                .saveAndFlush(
                        sync
                );

        try {

            AgencyCrmSyncResult result =
                    agencyCrmClient
                            .createLead(
                                    lead
                            );

            sync.markSynced(
                    result.externalContactId(),
                    result.externalOpportunityId()
            );

        } catch (RuntimeException exception) {

            LocalDateTime retryAt =
                    calculateNextRetryAt(
                            sync.getAttemptCount()
                    );

            sync.markFailed(
                    sanitizeError(
                            exception
                    ),
                    retryAt
            );
        }

        leadCrmSyncRepository
                .save(
                        sync
                );
    }

    private void validateAgency(
            Lead lead,
            Agency agency
    ) {

        if (agency == null) {
            throw new IllegalStateException(
                    "Agency is missing for lead "
                            + lead.getPublicId()
            );
        }

        if (!agency.isActive()) {
            throw new IllegalStateException(
                    "Agency is inactive: "
                            + agency.getSlug()
            );
        }

        if (!agency.isHighLevelSyncEnabled()) {
            throw new IllegalStateException(
                    "HighLevel synchronization is disabled for agency: "
                            + agency.getSlug()
            );
        }

        if (
                agency.getHighLevelLocationId()
                        == null
                        || agency
                        .getHighLevelLocationId()
                        .isBlank()
        ) {
            throw new IllegalStateException(
                    "HighLevel location ID is missing for agency: "
                            + agency.getSlug()
            );
        }
    }

    private LocalDateTime calculateNextRetryAt(
            int attemptCount
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        return switch (attemptCount) {

            case 1 ->
                    now.plusMinutes(1);

            case 2 ->
                    now.plusMinutes(5);

            case 3 ->
                    now.plusMinutes(15);

            case 4 ->
                    now.plusMinutes(30);

            default ->
                    now.plusHours(1);
        };
    }

    private String sanitizeError(
            RuntimeException exception
    ) {

        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            return "Agency CRM synchronization failed.";
        }

        if (message.length() > 1000) {
            return message.substring(
                    0,
                    1000
            );
        }

        return message;
    }
}