package com.scaleup.integration.internalcrm;

import com.scaleup.ai.AiContactStatus;
import com.scaleup.ai.LeadAiContact;
import com.scaleup.ai.LeadAiContactRepository;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class InternalCrmOpportunityStageResolver {

    private final LeadAiContactRepository
            leadAiContactRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    public InternalCrmOpportunityStageResolver(
            LeadAiContactRepository leadAiContactRepository,
            LeadCrmSyncRepository leadCrmSyncRepository
    ) {

        this.leadAiContactRepository =
                leadAiContactRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;
    }

    public Optional<InternalCrmOpportunityStage>
    resolveCurrentStage(
            UUID leadPublicId
    ) {

        LeadCrmSync agencyCrmSync =
                leadCrmSyncRepository
                        .findByLeadPublicIdAndDestination(
                                leadPublicId,
                                CrmDestination.AGENCY_CRM
                        )
                        .orElse(null);

        /*
         * Highest possible stage.
         *
         * Once the agency handoff succeeded,
         * the Internal CRM lead is ROUTED.
         */
        if (
                agencyCrmSync != null
                        && agencyCrmSync.getSyncStatus()
                        == CrmSyncStatus.SYNCED
        ) {

            return Optional.of(
                    InternalCrmOpportunityStage.ROUTED
            );
        }

        LeadAiContact aiContact =
                leadAiContactRepository
                        .findByLeadPublicId(
                                leadPublicId
                        )
                        .orElse(null);

        if (aiContact == null) {
            return Optional.empty();
        }

        /*
         * Qualification changes AGENCY_CRM from
         * NOT_REQUIRED to PENDING.
         *
         * PROCESSING or FAILED also means the lead
         * has already passed qualification and entered
         * the agency-routing lifecycle.
         */
        if (
                aiContact.getStatus()
                        == AiContactStatus.COMPLETED
                        && agencyCrmSync != null
                        && (
                        agencyCrmSync.getSyncStatus()
                                == CrmSyncStatus.PENDING
                                || agencyCrmSync.getSyncStatus()
                                == CrmSyncStatus.PROCESSING
                                || agencyCrmSync.getSyncStatus()
                                == CrmSyncStatus.FAILED
                )
        ) {

            return Optional.of(
                    InternalCrmOpportunityStage.QUALIFIED
            );
        }

        /*
         * AI conversation completed but qualification
         * has not released the lead yet.
         */
        if (
                aiContact.getStatus()
                        == AiContactStatus.COMPLETED
        ) {

            return Optional.of(
                    InternalCrmOpportunityStage.CONTACTED
            );
        }

        if (
                aiContact.getStatus()
                        == AiContactStatus.IN_PROGRESS
        ) {

            return Optional.of(
                    InternalCrmOpportunityStage.ATTEMPTING_CONTACT
            );
        }

        return Optional.empty();
    }
}