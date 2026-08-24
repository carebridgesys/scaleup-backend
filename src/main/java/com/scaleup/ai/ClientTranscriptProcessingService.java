package com.scaleup.ai;

import com.scaleup.ai.extraction.ClientTranscriptExtraction;
import com.scaleup.ai.extraction.ClientTranscriptExtractionService;
import com.scaleup.ai.qualification.ClientQualificationResult;
import com.scaleup.ai.qualification.ClientQualificationService;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.clientlead.ClientLeadDetailsRepository;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientTranscriptProcessingService {

    private final ClientLeadDetailsRepository
            clientLeadDetailsRepository;

    private final ClientTranscriptExtractionService
            extractionService;

    private final ClientQualificationService
            qualificationService;

    public ClientTranscriptProcessingService(
            ClientLeadDetailsRepository clientLeadDetailsRepository,
            ClientTranscriptExtractionService extractionService,
            ClientQualificationService qualificationService
    ) {
        this.clientLeadDetailsRepository =
                clientLeadDetailsRepository;

        this.extractionService =
                extractionService;

        this.qualificationService =
                qualificationService;
    }

    @Transactional
    public ClientLeadDetails process(
            Lead lead,
            String transcript
    ) {

        if (
                lead.getLeadType()
                        != LeadType.CLIENT
        ) {
            throw new IllegalArgumentException(
                    "Client transcript processing requires a CLIENT lead."
            );
        }

        ClientTranscriptExtraction extraction =
                extractionService.extract(
                        transcript
                );

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

        /*
         * Merge transcript information with any
         * existing client intake information.
         *
         * Missing transcript information must never
         * erase previously collected values.
         */
        details.updateIntakeInformation(
                firstNonBlank(
                        extraction.serviceNeeded(),
                        details.getServiceNeeded()
                ),
                firstNonBlank(
                        extraction.careStartTimeline(),
                        details.getCareStartTimeline()
                ),
                firstNonBlank(
                        extraction.payerType(),
                        details.getPayerType()
                ),
                firstNonBlank(
                        extraction.decisionMaker(),
                        details.getDecisionMaker()
                )
        );

        /*
         * Generate qualification readiness information
         * from the complete structured profile.
         */
        ClientQualificationResult result =
                qualificationService.evaluate(
                        details
                );

        details.updateAiQualification(
                result.score(),
                result.summary()
        );

        return clientLeadDetailsRepository
                .saveAndFlush(
                        details
                );
    }

    private String firstNonBlank(
            String preferred,
            String existing
    ) {

        if (
                preferred != null
                        && !preferred.isBlank()
        ) {
            return preferred.trim();
        }

        return existing;
    }
}