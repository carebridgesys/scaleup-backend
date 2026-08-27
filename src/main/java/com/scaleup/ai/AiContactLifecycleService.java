package com.scaleup.ai;

import com.scaleup.common.exception.InvalidRequestException;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import com.scaleup.integration.internalcrm.InternalCrmStageRefreshRequestedEvent;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scaleup.integration.internalcrm.InternalCrmOpportunityStage;
import com.scaleup.integration.internalcrm.InternalCrmStageChangeRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

@Service
public class AiContactLifecycleService {

    private final LeadRepository
            leadRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final LeadAiContactRepository
            leadAiContactRepository;

    private final ApplicationEventPublisher
            eventPublisher;

    public AiContactLifecycleService(
            LeadRepository leadRepository,
            LeadCrmSyncRepository leadCrmSyncRepository,
            LeadAiContactRepository leadAiContactRepository,
            ApplicationEventPublisher eventPublisher
    )
            {
        this.leadRepository =
                leadRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.leadAiContactRepository =
                leadAiContactRepository;

                this.eventPublisher =
                        eventPublisher;
    }

    @Transactional
    public LeadAiContact initializeAiContact(
            UUID leadId
    ) {

        Lead lead =
                leadRepository
                        .findByPublicId(leadId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Lead was not found."
                                )
                        );

        LeadCrmSync internalCrmSync =
                leadCrmSyncRepository
                        .findByLeadPublicIdAndDestination(
                                leadId,
                                CrmDestination.INTERNAL_CRM
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Internal CRM sync record was not found."
                                )
                        );

        if (
                internalCrmSync.getSyncStatus()
                        != CrmSyncStatus.SYNCED
        ) {
            throw new InvalidRequestException(
                    "AI contact cannot begin until the lead has been synced to the internal CRM."
            );
        }

        return leadAiContactRepository
                .findByLeadPublicId(leadId)
                .orElseGet(() ->
                        leadAiContactRepository.save(
                                new LeadAiContact(lead)
                        )
                );
    }

    @Transactional
    public LeadAiContact markInProgress(
            UUID leadId,
            String provider,
            String externalCallId
    ) {

        LeadAiContact contact =
                getRequiredAiContact(
                        leadId
                );

        if (
                contact.getStatus()
                        != AiContactStatus.PENDING
                        && contact.getStatus()
                        != AiContactStatus.FAILED
        ) {
            throw new InvalidRequestException(
                    "AI contact cannot be started from status "
                            + contact.getStatus()
                            + "."
            );
        }

        contact.markInProgress(
                provider,
                externalCallId
        );

        LeadAiContact savedContact =
                leadAiContactRepository
                        .saveAndFlush(
                                contact
                        );

        eventPublisher.publishEvent(
                new InternalCrmStageRefreshRequestedEvent(
                        leadId
                )
        );

        return savedContact;
    }

    @Transactional
    public LeadAiContact markCompleted(
            UUID leadId,
            String transcriptReference
    ) {

        LeadAiContact contact =
                getRequiredAiContact(
                        leadId
                );

        contact.markCompleted(
                transcriptReference
        );

        LeadAiContact savedContact =
                leadAiContactRepository
                        .saveAndFlush(
                                contact
                        );

        eventPublisher.publishEvent(
                new InternalCrmStageRefreshRequestedEvent(
                        leadId
                )
        );

        return savedContact;
    }

    @Transactional
    public LeadAiContact markFailed(
            UUID leadId,
            String error
    ) {

        LeadAiContact contact =
                getRequiredAiContact(
                        leadId
                );

        contact.markFailed(
                error
        );

        return leadAiContactRepository
                .saveAndFlush(contact);
    }

    private LeadAiContact getRequiredAiContact(
            UUID leadId
    ) {

        return leadAiContactRepository
                .findByLeadPublicId(leadId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "AI contact record was not found."
                        )
                );
    }
}