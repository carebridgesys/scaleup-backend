package com.scaleup.integration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CrmSyncStateService {

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    public CrmSyncStateService(
            LeadCrmSyncRepository leadCrmSyncRepository
    ) {
        this.leadCrmSyncRepository =
                leadCrmSyncRepository;
    }

    @Transactional
    public CrmSyncClaim claimForProcessing(
            UUID leadPublicId,
            CrmDestination destination
    ) {

        LeadCrmSync sync =
                leadCrmSyncRepository
                        .findForUpdate(
                                leadPublicId,
                                destination
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "CRM sync record was not found."
                                )
                        );

        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.SYNCED
        ) {

            return new CrmSyncClaim(
                    false,
                    sync.getAttemptCount()
            );
        }

        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.PROCESSING
        ) {

            return new CrmSyncClaim(
                    false,
                    sync.getAttemptCount()
            );
        }

        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.NOT_REQUIRED
        ) {

            throw new IllegalStateException(
                    "CRM synchronization is marked as not required."
            );
        }

        sync.markProcessing();

        leadCrmSyncRepository.save(
                sync
        );

        return new CrmSyncClaim(
                true,
                sync.getAttemptCount()
        );
    }

    @Transactional
    public void markSynced(
            UUID leadPublicId,
            CrmDestination destination,
            String externalContactId,
            String externalOpportunityId
    ) {

        LeadCrmSync sync =
                getLockedSync(
                        leadPublicId,
                        destination
                );

        /*
         * Defensive idempotency.
         */
        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.SYNCED
        ) {
            return;
        }

        sync.markSynced(
                externalContactId,
                externalOpportunityId
        );

        leadCrmSyncRepository.save(
                sync
        );
    }

    @Transactional
    public void markFailed(
            UUID leadPublicId,
            CrmDestination destination,
            String error,
            LocalDateTime nextAttemptAt
    ) {

        LeadCrmSync sync =
                getLockedSync(
                        leadPublicId,
                        destination
                );

        /*
         * Never overwrite a successful sync because
         * of a late failure from another execution.
         */
        if (
                sync.getSyncStatus()
                        == CrmSyncStatus.SYNCED
        ) {
            return;
        }

        sync.markFailed(
                error,
                nextAttemptAt
        );

        leadCrmSyncRepository.save(
                sync
        );
    }

    private LeadCrmSync getLockedSync(
            UUID leadPublicId,
            CrmDestination destination
    ) {

        return leadCrmSyncRepository
                .findForUpdate(
                        leadPublicId,
                        destination
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "CRM sync record was not found."
                        )
                );
    }

    public record CrmSyncClaim(
            boolean claimed,
            int attemptCount
    ) {
    }
}