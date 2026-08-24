package com.scaleup.integration.highlevel.webhook;

import com.scaleup.ai.AiContactLifecycleService;
import com.scaleup.ai.AiContactStatus;
import com.scaleup.ai.AiQualificationService;
import com.scaleup.ai.CaregiverTranscriptProcessingService;
import com.scaleup.ai.ClientTranscriptProcessingService;
import com.scaleup.ai.LeadAiContact;
import com.scaleup.ai.LeadAiContactRepository;
import com.scaleup.ai.LeadAiTranscript;
import com.scaleup.ai.LeadAiTranscriptRepository;
import com.scaleup.ai.dto.AiQualificationFinalizeRequest;
import com.scaleup.ai.dto.CaregiverScreeningPayload;
import com.scaleup.ai.dto.ClientQualificationPayload;
import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class HighLevelVoiceAiWebhookService {

    private static final String HIGHLEVEL_PROVIDER =
            "HIGHLEVEL_VOICE_AI";

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final LeadAiContactRepository
            leadAiContactRepository;

    private final AiContactLifecycleService
            aiContactLifecycleService;

    private final LeadAiTranscriptRepository
            leadAiTranscriptRepository;

    private final CaregiverTranscriptProcessingService
            caregiverTranscriptProcessingService;

    private final ClientTranscriptProcessingService
            clientTranscriptProcessingService;

    private final AiQualificationService
            aiQualificationService;

    public HighLevelVoiceAiWebhookService(
            LeadCrmSyncRepository leadCrmSyncRepository,
            LeadAiContactRepository leadAiContactRepository,
            AiContactLifecycleService aiContactLifecycleService,
            LeadAiTranscriptRepository leadAiTranscriptRepository,
            CaregiverTranscriptProcessingService caregiverTranscriptProcessingService,
            ClientTranscriptProcessingService clientTranscriptProcessingService,
            AiQualificationService aiQualificationService
    ) {
        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.leadAiContactRepository =
                leadAiContactRepository;

        this.aiContactLifecycleService =
                aiContactLifecycleService;

        this.leadAiTranscriptRepository =
                leadAiTranscriptRepository;

        this.caregiverTranscriptProcessingService =
                caregiverTranscriptProcessingService;

        this.clientTranscriptProcessingService =
                clientTranscriptProcessingService;

        this.aiQualificationService =
                aiQualificationService;
    }

    @Transactional
    public void processCompletedCall(
            HighLevelVoiceAiWebhookRequest request
    ) {

        String contactId =
                normalizeRequired(
                        request.contactId()
                );

        LeadType webhookLeadType =
                resolveLeadType(
                        request.agentType()
                );

        Optional<LeadCrmSync> syncOptional =
                leadCrmSyncRepository
                        .findFirstByDestinationAndExternalContactId(
                                CrmDestination.INTERNAL_CRM,
                                contactId
                        );

        if (syncOptional.isEmpty()) {

            System.out.println(
                    "No ScaleUp lead found for HighLevel contactId = "
                            + contactId
            );

            System.out.println(
                    "Webhook ignored safely"
            );

            return;
        }

        Lead lead =
                syncOptional
                        .get()
                        .getLead();

        if (
                lead.getLeadType()
                        != webhookLeadType
        ) {

            System.out.println(
                    "HighLevel Voice AI webhook lead type mismatch"
            );

            System.out.println(
                    "contactId = "
                            + contactId
            );

            System.out.println(
                    "webhook agentType = "
                            + webhookLeadType
            );

            System.out.println(
                    "ScaleUp leadType = "
                            + lead.getLeadType()
            );

            System.out.println(
                    "leadPublicId = "
                            + lead.getPublicId()
            );

            return;
        }

        UUID leadPublicId =
                lead.getPublicId();

        System.out.println(
                "Matched ScaleUp lead"
        );

        System.out.println(
                "leadPublicId = "
                        + leadPublicId
        );

        System.out.println(
                "leadType = "
                        + lead.getLeadType()
        );

        System.out.println(
                "HighLevel contactId = "
                        + contactId
        );

        System.out.println(
                "transcript received = "
                        + hasText(
                        request.transcript()
                )
        );

        if (!hasText(request.transcript())) {

            System.out.println(
                    "Webhook processing stopped because transcript is missing"
            );

            return;
        }

        String externalCallId =
                normalizeNullable(
                        request.callId()
                );

        boolean transcriptAlreadyExists =
                externalCallId != null
                        && leadAiTranscriptRepository
                        .findFirstByProviderAndExternalCallId(
                                HIGHLEVEL_PROVIDER,
                                externalCallId
                        )
                        .isPresent();

        /*
         * Persist the transcript only once.
         *
         * We do not return immediately for a duplicate
         * because a previous webhook may have persisted
         * the transcript but failed before qualification
         * or agency routing completed.
         */
        if (!transcriptAlreadyExists) {

            LeadAiTranscript transcript =
                    new LeadAiTranscript(
                            lead,
                            HIGHLEVEL_PROVIDER,
                            externalCallId,
                            webhookLeadType,
                            request.transcript(),
                            request.summary()
                    );

            leadAiTranscriptRepository
                    .saveAndFlush(
                            transcript
                    );

            System.out.println(
                    "AI transcript persisted"
            );

        } else {

            System.out.println(
                    "Duplicate AI transcript detected"
            );

            System.out.println(
                    "Continuing lifecycle/qualification checks safely"
            );
        }

        CaregiverLeadDetails caregiverDetails =
                null;

        ClientLeadDetails clientDetails =
                null;

        /*
         * Apply the correct transcript processor
         * depending on the lead type.
         */
        if (
                webhookLeadType
                        == LeadType.CAREGIVER
        ) {

            caregiverDetails =
                    caregiverTranscriptProcessingService
                            .process(
                                    lead,
                                    request.transcript()
                            );

            logCaregiverDetails(
                    caregiverDetails
            );

        } else if (
                webhookLeadType
                        == LeadType.CLIENT
        ) {

            clientDetails =
                    clientTranscriptProcessingService
                            .process(
                                    lead,
                                    request.transcript()
                            );

            logClientDetails(
                    clientDetails
            );
        }

        /*
         * Ensure the AI lifecycle exists and
         * is at least IN_PROGRESS.
         */
        LeadAiContact aiContact =
                ensureAiContactReady(
                        leadPublicId,
                        request
                );

        /*
         * If another callback already completed
         * the AI lifecycle, do not complete it again.
         *
         * We still attempt automatic qualification
         * if the agency CRM has not yet been released.
         */
        if (
                aiContact.getStatus()
                        == AiContactStatus.COMPLETED
        ) {

            System.out.println(
                    "AI contact already completed for leadPublicId = "
                            + leadPublicId
            );

            finalizeQualificationIfNeeded(
                    lead,
                    clientDetails,
                    caregiverDetails
            );

            return;
        }

        String transcriptReference =
                buildTranscriptReference(
                        request,
                        contactId
                );

        LeadAiContact completedContact =
                aiContactLifecycleService
                        .markCompleted(
                                leadPublicId,
                                transcriptReference
                        );

        System.out.println(
                "AI contact marked COMPLETED"
        );

        System.out.println(
                "aiContactStatus = "
                        + completedContact
                        .getStatus()
        );

        System.out.println(
                "transcriptReference = "
                        + completedContact
                        .getTranscriptReference()
        );

        /*
         * The lifecycle is now COMPLETED,
         * so AiQualificationService is allowed
         * to finalize qualification.
         */
        finalizeQualificationIfNeeded(
                lead,
                clientDetails,
                caregiverDetails
        );
    }

    private void finalizeQualificationIfNeeded(
            Lead lead,
            ClientLeadDetails clientDetails,
            CaregiverLeadDetails caregiverDetails
    ) {

        LeadCrmSync agencyCrmSync =
                leadCrmSyncRepository
                        .findByLeadPublicIdAndDestination(
                                lead.getPublicId(),
                                CrmDestination.AGENCY_CRM
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Agency CRM sync record was not found for lead "
                                                + lead.getPublicId()
                                )
                        );

        /*
         * NOT_REQUIRED means qualification has
         * not yet released this lead.
         *
         * Once it reaches PENDING, PROCESSING,
         * SYNCED or FAILED, it is already in
         * the agency-routing lifecycle.
         */
        if (
                agencyCrmSync.getSyncStatus()
                        != CrmSyncStatus.NOT_REQUIRED
        ) {

            System.out.println(
                    "Automatic qualification skipped"
            );

            System.out.println(
                    "leadPublicId = "
                            + lead.getPublicId()
            );

            System.out.println(
                    "agencyCrmSyncStatus = "
                            + agencyCrmSync
                            .getSyncStatus()
            );

            return;
        }

        if (
                lead.getLeadType()
                        == LeadType.CLIENT
        ) {

            if (clientDetails == null) {
                throw new IllegalStateException(
                        "Client details are required to finalize automatic client qualification."
                );
            }

            finalizeClientQualification(
                    lead,
                    clientDetails
            );

            return;
        }

        if (
                lead.getLeadType()
                        == LeadType.CAREGIVER
        ) {

            if (caregiverDetails == null) {
                throw new IllegalStateException(
                        "Caregiver details are required to finalize automatic caregiver qualification."
                );
            }

            finalizeCaregiverQualification(
                    lead,
                    caregiverDetails
            );
        }
    }

    private void finalizeClientQualification(
            Lead lead,
            ClientLeadDetails details
    ) {

        ClientQualificationPayload clientPayload =
                new ClientQualificationPayload(
                        details.getServiceNeeded(),
                        details.getCareStartTimeline(),
                        details.getPayerType(),
                        details.getDecisionMaker(),
                        details.getAiQualificationScore(),
                        details.getAiSummary()
                );

        AiQualificationFinalizeRequest qualificationRequest =
                new AiQualificationFinalizeRequest(
                        clientPayload,
                        null
                );

        aiQualificationService
                .finalizeQualification(
                        lead.getPublicId(),
                        qualificationRequest
                );

        System.out.println(
                "Client qualification finalized automatically"
        );

        System.out.println(
                "leadPublicId = "
                        + lead.getPublicId()
        );

        System.out.println(
                "agencyCrmSyncStatus = PENDING"
        );
    }

    private void finalizeCaregiverQualification(
            Lead lead,
            CaregiverLeadDetails details
    ) {

        CaregiverScreeningPayload caregiverPayload =
                new CaregiverScreeningPayload(
                        details.getYearsExperience(),
                        details.getCertifications(),
                        details.getAvailability(),
                        details.getTransportation(),
                        details.getPreferredSchedule(),
                        details.getDesiredHoursPerWeek(),
                        details.getServiceArea(),
                        details.getAiScreeningScore(),
                        details.getAiScreeningSummary()
                );

        AiQualificationFinalizeRequest qualificationRequest =
                new AiQualificationFinalizeRequest(
                        null,
                        caregiverPayload
                );

        aiQualificationService
                .finalizeQualification(
                        lead.getPublicId(),
                        qualificationRequest
                );

        System.out.println(
                "Caregiver qualification finalized automatically"
        );

        System.out.println(
                "leadPublicId = "
                        + lead.getPublicId()
        );

        System.out.println(
                "agencyCrmSyncStatus = PENDING"
        );
    }

    private LeadAiContact ensureAiContactReady(
            UUID leadPublicId,
            HighLevelVoiceAiWebhookRequest request
    ) {

        LeadAiContact aiContact =
                leadAiContactRepository
                        .findByLeadPublicId(
                                leadPublicId
                        )
                        .orElseGet(() ->
                                aiContactLifecycleService
                                        .initializeAiContact(
                                                leadPublicId
                                        )
                        );

        if (
                aiContact.getStatus()
                        == AiContactStatus.PENDING
                        || aiContact.getStatus()
                        == AiContactStatus.FAILED
        ) {

            String externalCallId =
                    normalizeNullable(
                            request.callId()
                    );

            if (externalCallId == null) {

                externalCallId =
                        "hl-contact-"
                                + request.contactId();
            }

            return aiContactLifecycleService
                    .markInProgress(
                            leadPublicId,
                            HIGHLEVEL_PROVIDER,
                            externalCallId
                    );
        }

        return aiContact;
    }

    private void logClientDetails(
            ClientLeadDetails details
    ) {

        System.out.println(
                "Client transcript data applied"
        );

        System.out.println(
                "serviceNeeded = "
                        + details.getServiceNeeded()
        );

        System.out.println(
                "careStartTimeline = "
                        + details.getCareStartTimeline()
        );

        System.out.println(
                "payerType = "
                        + details.getPayerType()
        );

        System.out.println(
                "decisionMaker = "
                        + details.getDecisionMaker()
        );

        System.out.println(
                "aiQualificationScore = "
                        + details
                        .getAiQualificationScore()
        );

        System.out.println(
                "aiSummary = "
                        + details.getAiSummary()
        );
    }

    private void logCaregiverDetails(
            CaregiverLeadDetails details
    ) {

        System.out.println(
                "Caregiver transcript data applied"
        );

        System.out.println(
                "yearsExperience = "
                        + details.getYearsExperience()
        );

        System.out.println(
                "certifications = "
                        + details.getCertifications()
        );

        System.out.println(
                "availability = "
                        + details.getAvailability()
        );

        System.out.println(
                "transportation = "
                        + details.getTransportation()
        );

        System.out.println(
                "preferredSchedule = "
                        + details.getPreferredSchedule()
        );

        System.out.println(
                "desiredHoursPerWeek = "
                        + details.getDesiredHoursPerWeek()
        );

        System.out.println(
                "serviceArea = "
                        + details.getServiceArea()
        );

        System.out.println(
                "aiScreeningScore = "
                        + details.getAiScreeningScore()
        );

        System.out.println(
                "aiScreeningSummary = "
                        + details.getAiScreeningSummary()
        );
    }

    private String buildTranscriptReference(
            HighLevelVoiceAiWebhookRequest request,
            String contactId
    ) {

        String callId =
                normalizeNullable(
                        request.callId()
                );

        if (callId != null) {
            return "highlevel-call:"
                    + callId;
        }

        return "highlevel-contact:"
                + contactId;
    }

    private LeadType resolveLeadType(
            String agentType
    ) {

        String normalized =
                normalizeRequired(
                        agentType
                )
                        .toUpperCase(
                                Locale.ROOT
                        );

        try {

            return LeadType.valueOf(
                    normalized
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Unsupported Voice AI agent type: "
                            + normalized
            );
        }
    }

    private String normalizeRequired(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Required webhook value must not be blank."
            );
        }

        return value.trim();
    }

    private String normalizeNullable(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.isBlank();
    }
}