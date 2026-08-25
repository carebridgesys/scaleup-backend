package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
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
public class AgencyCrmSyncService {

    private final LeadRepository
            leadRepository;

    private final AgencyCrmClient
            agencyCrmClient;

    private final CrmSyncStateService
            crmSyncStateService;

    private final CrmSyncFailureClassifier
            failureClassifier;

    public AgencyCrmSyncService(
            LeadRepository leadRepository,
            AgencyCrmClient agencyCrmClient,
            CrmSyncStateService crmSyncStateService,
            CrmSyncFailureClassifier failureClassifier
    ) {

        this.leadRepository =
                leadRepository;

        this.agencyCrmClient =
                agencyCrmClient;

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
                                CrmDestination.AGENCY_CRM
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

            Agency agency =
                    lead.getAgency();

            validateAgency(
                    lead,
                    agency
            );

            AgencyCrmSyncResult result =
                    agencyCrmClient
                            .createLead(
                                    lead
                            );

            crmSyncStateService
                    .markSynced(
                            leadPublicId,
                            CrmDestination.AGENCY_CRM,
                            result.externalContactId(),
                            result.externalOpportunityId()
                    );

        } catch (RuntimeException exception) {

            handleFailure(
                    leadPublicId,
                    claim.attemptCount(),
                    exception
            );

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
                        CrmDestination.AGENCY_CRM,
                        failure.message(),
                        retryAt
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
                    "Agency is inactive."
            );
        }

        if (!agency.isHighLevelSyncEnabled()) {

            throw new IllegalStateException(
                    "HighLevel synchronization is disabled for agency."
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
}