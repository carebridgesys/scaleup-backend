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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class HighLevelVoiceAiWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    HighLevelVoiceAiWebhookService.class
            );

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

        /*
         * A HighLevel webhook may be delivered for
         * contacts that were not created by ScaleUp.
         *
         * Ignore those callbacks safely.
         */
        if (syncOptional.isEmpty()) {

            log.info(
                    "Ignoring Voice AI webhook because no ScaleUp lead matches the HighLevel contact."
            );

            return;
        }

        Lead lead =
                syncOptional
                        .get()
                        .getLead();

        /*
         * Never allow a CLIENT webhook to process
         * a CAREGIVER lead or vice versa.
         */
        if (
                lead.getLeadType()
                        != webhookLeadType
        ) {

            log.warn(
                    "Ignoring Voice AI webhook because agent type does not match ScaleUp lead type. leadId={}",
                    lead.getPublicId()
            );

            return;
        }

        UUID leadPublicId =
                lead.getPublicId();

        if (!hasText(request.transcript())) {

            log.warn(
                    "Ignoring Voice AI completion webhook because transcript is missing. leadId={}",
                    leadPublicId
            );

            return;
        }

        String externalCallId =
                normalizeNullable(
                        request.callId()
                );

        /*
         * Generate a deterministic identity for this
         * webhook event.
         *
         * When HighLevel supplies a call ID, that is
         * the preferred source of identity.
         *
         * When callId is absent, the identity is based
         * on the matched ScaleUp lead, HighLevel contact,
         * agent type and normalized transcript.
         */
        String eventKey =
                buildEventKey(
                        lead,
                        contactId,
                        webhookLeadType,
                        externalCallId,
                        request.transcript()
                );

        boolean transcriptAlreadyExists =
                leadAiTranscriptRepository
                        .existsByEventKey(
                                eventKey
                        );

        /*
         * Persist each Voice AI transcript exactly once.
         *
         * We intentionally continue processing when the
         * event already exists. A previous webhook may
         * have persisted the transcript and then failed
         * before lifecycle completion, qualification or
         * agency routing.
         */
        if (!transcriptAlreadyExists) {

            LeadAiTranscript transcript =
                    new LeadAiTranscript(
                            lead,
                            HIGHLEVEL_PROVIDER,
                            externalCallId,
                            eventKey,
                            webhookLeadType,
                            request.transcript(),
                            request.summary()
                    );

            leadAiTranscriptRepository
                    .saveAndFlush(
                            transcript
                    );

            log.info(
                    "Voice AI transcript persisted. leadId={}",
                    leadPublicId
            );

        } else {

            log.info(
                    "Duplicate Voice AI webhook detected; continuing recovery checks. leadId={}",
                    leadPublicId
            );
        }

        CaregiverLeadDetails caregiverDetails =
                null;

        ClientLeadDetails clientDetails =
                null;

        /*
         * Re-applying the transcript processor is safe
         * because these services update the existing
         * detail record rather than creating another lead.
         *
         * This is useful if an earlier webhook stopped
         * after transcript persistence.
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
        }

        LeadAiContact aiContact =
                ensureAiContactReady(
                        leadPublicId,
                        request
                );

        /*
         * A replay after AI completion should not
         * transition the lifecycle again.
         *
         * Qualification/routing recovery is still
         * allowed if it never completed.
         */
        if (
                aiContact.getStatus()
                        == AiContactStatus.COMPLETED
        ) {

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
                        contactId,
                        eventKey
                );

        aiContactLifecycleService
                .markCompleted(
                        leadPublicId,
                        transcriptReference
                );

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
         * Only NOT_REQUIRED means qualification has
         * never released this lead for agency routing.
         *
         * PENDING / PROCESSING / SYNCED / FAILED are all
         * already part of the agency CRM lifecycle.
         */
        if (
                agencyCrmSync.getSyncStatus()
                        != CrmSyncStatus.NOT_REQUIRED
        ) {

            log.info(
                    "Voice AI qualification already released to agency CRM lifecycle. leadId={}, status={}",
                    lead.getPublicId(),
                    agencyCrmSync.getSyncStatus()
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

        log.info(
                "Automatic client qualification finalized. leadId={}",
                lead.getPublicId()
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

        log.info(
                "Automatic caregiver qualification finalized. leadId={}",
                lead.getPublicId()
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

            /*
             * If HighLevel omitted callId, create a stable
             * lifecycle reference based on the contact.
             */
            if (externalCallId == null) {

                externalCallId =
                        "hl-contact-"
                                + normalizeRequired(
                                request.contactId()
                        );
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

    private String buildTranscriptReference(
            HighLevelVoiceAiWebhookRequest request,
            String contactId,
            String eventKey
    ) {

        String callId =
                normalizeNullable(
                        request.callId()
                );

        if (callId != null) {

            return "highlevel-call:"
                    + callId;
        }

        /*
         * The event key is more precise than using only
         * the contact ID when callId is unavailable.
         */
        return "highlevel-event:"
                + eventKey;
    }

    private String buildEventKey(
            Lead lead,
            String contactId,
            LeadType agentType,
            String externalCallId,
            String transcript
    ) {

        String identity;

        if (externalCallId != null) {

            identity =
                    HIGHLEVEL_PROVIDER
                            + "|call|"
                            + externalCallId;

        } else {

            identity =
                    HIGHLEVEL_PROVIDER
                            + "|lead|"
                            + lead.getPublicId()
                            + "|contact|"
                            + contactId
                            + "|agent|"
                            + agentType.name()
                            + "|transcript|"
                            + normalizeTranscriptForHash(
                            transcript
                    );
        }

        return sha256(
                identity
        );
    }

    private String normalizeTranscriptForHash(
            String transcript
    ) {

        return normalizeRequired(
                transcript
        )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private String sha256(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(
                            hash
                    );

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );
        }
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