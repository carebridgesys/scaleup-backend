package com.scaleup.ai;

import com.scaleup.ai.dto.AiQualificationFinalizeRequest;
import com.scaleup.ai.dto.AiQualificationFinalizeResponse;
import com.scaleup.ai.dto.CaregiverScreeningPayload;
import com.scaleup.ai.dto.ClientQualificationPayload;
import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.caregiverlead.CaregiverLeadDetailsRepository;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.clientlead.ClientLeadDetailsRepository;
import com.scaleup.common.exception.InvalidRequestException;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import com.scaleup.integration.agencycrm.AgencyCrmSyncRequestedEvent;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AiQualificationService {

    private final LeadRepository
            leadRepository;

    private final LeadAiContactRepository
            leadAiContactRepository;

    private final ClientLeadDetailsRepository
            clientLeadDetailsRepository;

    private final CaregiverLeadDetailsRepository
            caregiverLeadDetailsRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final ApplicationEventPublisher
            eventPublisher;

    public AiQualificationService(
            LeadRepository leadRepository,
            LeadAiContactRepository leadAiContactRepository,
            ClientLeadDetailsRepository clientLeadDetailsRepository,
            CaregiverLeadDetailsRepository caregiverLeadDetailsRepository,
            LeadCrmSyncRepository leadCrmSyncRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.leadRepository =
                leadRepository;

        this.leadAiContactRepository =
                leadAiContactRepository;

        this.clientLeadDetailsRepository =
                clientLeadDetailsRepository;

        this.caregiverLeadDetailsRepository =
                caregiverLeadDetailsRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.eventPublisher =
                eventPublisher;
    }

    @Transactional
    public AiQualificationFinalizeResponse finalizeQualification(
            UUID leadId,
            AiQualificationFinalizeRequest request
    ) {

        Lead lead =
                leadRepository
                        .findByPublicId(
                                leadId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Lead was not found."
                                )
                        );

        validateAiContactCompleted(
                leadId
        );

        LeadCrmSync agencyCrmSync =
                getAgencyCrmSync(
                        leadId
                );

        validateAgencySyncState(
                agencyCrmSync
        );

        QualificationResult result =
                switch (lead.getLeadType()) {

                    case CLIENT ->
                            finalizeClientQualification(
                                    lead,
                                    request
                            );

                    case CAREGIVER ->
                            finalizeCaregiverScreening(
                                    lead,
                                    request
                            );
                };

        /*
         * The agency is eligible for synchronization
         * only after qualification data has been
         * successfully persisted.
         */
        agencyCrmSync.markPending();

        leadCrmSyncRepository
                .saveAndFlush(
                        agencyCrmSync
                );

        /*
         * Publish inside the transaction.
         *
         * AgencyCrmSyncEventListener uses
         * TransactionPhase.AFTER_COMMIT,
         * so the actual agency transfer will
         * not begin unless this transaction
         * successfully commits.
         */
        eventPublisher.publishEvent(
                new AgencyCrmSyncRequestedEvent(
                        lead.getPublicId()
                )
        );

        return new AiQualificationFinalizeResponse(
                lead.getPublicId(),
                lead.getLeadType(),
                result.score(),
                result.summary(),
                agencyCrmSync.getSyncStatus(),
                LocalDateTime.now()
        );
    }

    private void validateAiContactCompleted(
            UUID leadId
    ) {

        LeadAiContact aiContact =
                leadAiContactRepository
                        .findByLeadPublicId(
                                leadId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "AI contact record was not found."
                                )
                        );

        if (
                aiContact.getStatus()
                        != AiContactStatus.COMPLETED
        ) {
            throw new InvalidRequestException(
                    "AI qualification cannot be finalized until the AI contact is COMPLETED."
            );
        }
    }

    private LeadCrmSync getAgencyCrmSync(
            UUID leadId
    ) {

        return leadCrmSyncRepository
                .findByLeadPublicIdAndDestination(
                        leadId,
                        CrmDestination.AGENCY_CRM
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Agency CRM sync record was not found."
                        )
                );
    }

    private void validateAgencySyncState(
            LeadCrmSync agencyCrmSync
    ) {

        CrmSyncStatus status =
                agencyCrmSync.getSyncStatus();

        if (
                status == CrmSyncStatus.SYNCED
                        || status == CrmSyncStatus.PROCESSING
        ) {
            throw new InvalidRequestException(
                    "AI qualification cannot be finalized while the agency CRM sync status is "
                            + status
                            + "."
            );
        }
    }

    private QualificationResult finalizeClientQualification(
            Lead lead,
            AiQualificationFinalizeRequest request
    ) {

        if (
                request.caregiverScreening()
                        != null
        ) {
            throw new InvalidRequestException(
                    "Caregiver screening data cannot be submitted for a CLIENT lead."
            );
        }

        ClientQualificationPayload payload =
                request.clientQualification();

        if (payload == null) {
            throw new InvalidRequestException(
                    "Client qualification data is required for a CLIENT lead."
            );
        }

        ClientLeadDetails details =
                clientLeadDetailsRepository
                        .findByLeadPublicId(
                                lead.getPublicId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Client lead details were not found."
                                )
                        );

        details.updateIntakeInformation(
                payload.serviceNeeded(),
                payload.careStartTimeline(),
                payload.payerType(),
                payload.decisionMaker()
        );

        details.updateAiQualification(
                payload.aiQualificationScore(),
                payload.aiSummary()
        );

        ClientLeadDetails savedDetails =
                clientLeadDetailsRepository
                        .saveAndFlush(
                                details
                        );

        return new QualificationResult(
                savedDetails
                        .getAiQualificationScore(),
                savedDetails
                        .getAiSummary()
        );
    }

    private QualificationResult finalizeCaregiverScreening(
            Lead lead,
            AiQualificationFinalizeRequest request
    ) {

        if (
                request.clientQualification()
                        != null
        ) {
            throw new InvalidRequestException(
                    "Client qualification data cannot be submitted for a CAREGIVER lead."
            );
        }

        CaregiverScreeningPayload payload =
                request.caregiverScreening();

        if (payload == null) {
            throw new InvalidRequestException(
                    "Caregiver screening data is required for a CAREGIVER lead."
            );
        }

        CaregiverLeadDetails details =
                caregiverLeadDetailsRepository
                        .findByLeadPublicId(
                                lead.getPublicId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Caregiver lead details were not found."
                                )
                        );

        details.updateExperience(
                payload.yearsExperience(),
                payload.certifications()
        );

        details.updateAvailability(
                payload.availability(),
                payload.preferredSchedule(),
                payload.desiredHoursPerWeek()
        );

        details.updateTransportation(
                payload.transportation()
        );

        details.updateServiceArea(
                payload.serviceArea()
        );

        details.updateAiScreening(
                payload.aiScreeningScore(),
                payload.aiScreeningSummary()
        );

        CaregiverLeadDetails savedDetails =
                caregiverLeadDetailsRepository
                        .saveAndFlush(
                                details
                        );

        return new QualificationResult(
                savedDetails
                        .getAiScreeningScore(),
                savedDetails
                        .getAiScreeningSummary()
        );
    }

    private record QualificationResult(
            Integer score,
            String summary
    ) {
    }
}