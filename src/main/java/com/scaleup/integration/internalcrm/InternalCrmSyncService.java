package com.scaleup.integration.internalcrm;

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
public class InternalCrmSyncService {

    private final LeadRepository leadRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final InternalCrmClient
            internalCrmClient;

    public InternalCrmSyncService(
            LeadRepository leadRepository,
            LeadCrmSyncRepository leadCrmSyncRepository,
            InternalCrmClient internalCrmClient
    ) {
        this.leadRepository =
                leadRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.internalCrmClient =
                internalCrmClient;
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

        LeadCrmSync sync =
                leadCrmSyncRepository
                        .findByLeadPublicIdAndDestination(
                                leadPublicId,
                                CrmDestination.INTERNAL_CRM
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Internal CRM sync record was not found."
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
                    "Internal CRM synchronization is marked as not required."
            );
        }

        sync.markProcessing();

        leadCrmSyncRepository
                .saveAndFlush(sync);

        try {

            InternalCrmSyncResult result =
                    internalCrmClient
                            .createLead(lead);

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
                    sanitizeError(exception),
                    retryAt
            );
        }

        leadCrmSyncRepository.save(sync);
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
            return "Internal CRM synchronization failed.";
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