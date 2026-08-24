package com.scaleup.ai;

import com.scaleup.ai.extraction.CaregiverTranscriptExtraction;
import com.scaleup.ai.extraction.CaregiverTranscriptExtractionService;
import com.scaleup.ai.screening.CaregiverScreeningResult;
import com.scaleup.ai.screening.CaregiverScreeningService;
import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.caregiverlead.CaregiverLeadDetailsRepository;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaregiverTranscriptProcessingService {

    private final CaregiverLeadDetailsRepository
            caregiverLeadDetailsRepository;

    private final CaregiverTranscriptExtractionService
            extractionService;

    private final CaregiverScreeningService
            screeningService;

    public CaregiverTranscriptProcessingService(
            CaregiverLeadDetailsRepository caregiverLeadDetailsRepository,
            CaregiverTranscriptExtractionService extractionService,
            CaregiverScreeningService screeningService
    ) {

        this.caregiverLeadDetailsRepository =
                caregiverLeadDetailsRepository;

        this.extractionService =
                extractionService;

        this.screeningService =
                screeningService;
    }

    @Transactional
    public CaregiverLeadDetails process(
            Lead lead,
            String transcript
    ) {

        if (lead.getLeadType() != LeadType.CAREGIVER) {

            throw new IllegalArgumentException(
                    "Caregiver transcript processing requires a CAREGIVER lead."
            );
        }

        CaregiverTranscriptExtraction extraction =
                extractionService.extract(
                        transcript
                );

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

        /*
         * Merge extracted values with existing caregiver data.
         *
         * Missing transcript information must not erase
         * information that was previously collected.
         */

        details.updateExperience(
                firstNonNull(
                        extraction.yearsExperience(),
                        details.getYearsExperience()
                ),
                firstNonBlank(
                        extraction.certifications(),
                        details.getCertifications()
                )
        );

        details.updateAvailability(
                firstNonBlank(
                        extraction.availability(),
                        details.getAvailability()
                ),
                firstNonBlank(
                        extraction.preferredSchedule(),
                        details.getPreferredSchedule()
                ),
                firstNonNull(
                        extraction.desiredHoursPerWeek(),
                        details.getDesiredHoursPerWeek()
                )
        );

        details.updateTransportation(
                firstNonBlank(
                        extraction.transportation(),
                        details.getTransportation()
                )
        );

        details.updateServiceArea(
                firstNonBlank(
                        extraction.serviceArea(),
                        details.getServiceArea()
                )
        );

        /*
         * Generate screening readiness information
         * from the complete structured caregiver profile.
         */
        CaregiverScreeningResult screeningResult =
                screeningService.evaluate(
                        details
                );

        details.updateAiScreening(
                screeningResult.score(),
                screeningResult.summary()
        );

        return caregiverLeadDetailsRepository
                .saveAndFlush(
                        details
                );
    }

    private <T> T firstNonNull(
            T preferred,
            T existing
    ) {

        if (preferred != null) {
            return preferred;
        }

        return existing;
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