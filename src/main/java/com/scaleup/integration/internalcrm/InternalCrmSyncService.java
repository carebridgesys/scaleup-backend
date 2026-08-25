package com.scaleup.integration.internalcrm;

import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncFailure;
import com.scaleup.integration.CrmSyncFailureClassifier;
import com.scaleup.integration.CrmSyncStateService;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InternalCrmSyncService {

    private final LeadRepository
            leadRepository;

    private final InternalCrmClient
            internalCrmClient;

    private final CrmSyncStateService
            crmSyncStateService;

    private final CrmSyncFailureClassifier
            failureClassifier;

    public InternalCrmSyncService(
            LeadRepository leadRepository,
            InternalCrmClient internalCrmClient,
            CrmSyncStateService crmSyncStateService,
            CrmSyncFailureClassifier failureClassifier
    ) {

        this.leadRepository =
                leadRepository;

        this.internalCrmClient =
                internalCrmClient;

        this.crmSyncStateService =
                crmSyncStateService;

        this.failureClassifier =
                failureClassifier;
    }

    public void syncLead(
            UUID leadPublicId
    ) {

        CrmSyncStateService.CrmSyncClaim claim =
                crmSyncStateService
                        .claimForProcessing(
                                leadPublicId,
                                CrmDestination.INTERNAL_CRM
                        );

        if (!claim.claimed()) {
            return;
        }

        try {

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

            InternalCrmSyncResult result =
                    internalCrmClient
                            .createLead(
                                    lead
                            );

            crmSyncStateService
                    .markSynced(
                            leadPublicId,
                            CrmDestination.INTERNAL_CRM,
                            result.externalContactId(),
                            result.externalOpportunityId()
                    );

        } catch (RuntimeException exception) {

            handleFailure(
                    leadPublicId,
                    claim.attemptCount(),
                    exception
            );

            /*
             * Re-throw after FAILED state was safely
             * committed in its own transaction.
             *
             * This gives event listeners/workers accurate
             * failure logging.
             */
            throw exception;
        }
    }

    private void handleFailure(
            UUID leadPublicId,
            int attemptCount,
            RuntimeException exception
    ) {

        CrmSyncFailure failure =
                failureClassifier
                        .classify(
                                exception
                        );

        LocalDateTime retryAt =
                failure.retryable()
                        ? calculateNextRetryAt(
                        attemptCount
                )
                        : null;

        crmSyncStateService
                .markFailed(
                        leadPublicId,
                        CrmDestination.INTERNAL_CRM,
                        failure.message(),
                        retryAt
                );
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
}