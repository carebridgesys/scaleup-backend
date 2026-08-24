package com.scaleup.ai.screening;

import com.scaleup.caregiverlead.CaregiverLeadDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RuleBasedCaregiverScreeningService
        implements CaregiverScreeningService {

    @Override
    public CaregiverScreeningResult evaluate(
            CaregiverLeadDetails details
    ) {

        int score = 0;

        List<String> strengths =
                new ArrayList<>();

        List<String> missingInformation =
                new ArrayList<>();

        /*
         * Experience: maximum 30 points.
         */
        Integer yearsExperience =
                details.getYearsExperience();

        if (yearsExperience == null) {

            missingInformation.add(
                    "caregiving experience"
            );

        } else if (yearsExperience >= 5) {

            score += 30;

            strengths.add(
                    yearsExperience
                            + " years of caregiving experience"
            );

        } else if (yearsExperience >= 3) {

            score += 25;

            strengths.add(
                    yearsExperience
                            + " years of caregiving experience"
            );

        } else if (yearsExperience >= 1) {

            score += 18;

            strengths.add(
                    yearsExperience
                            + " year(s) of caregiving experience"
            );

        } else {

            score += 5;
        }

        /*
         * Job-related certifications: maximum 20 points.
         */
        String certifications =
                details.getCertifications();

        if (hasText(certifications)) {

            String upper =
                    certifications.toUpperCase(
                            Locale.ROOT
                    );

            int certificationScore = 5;

            if (upper.contains("CNA")) {
                certificationScore += 7;
            }

            if (upper.contains("HHA")) {
                certificationScore += 5;
            }

            if (upper.contains("CPR")) {
                certificationScore += 3;
            }

            if (upper.contains("FIRST AID")) {
                certificationScore += 3;
            }

            certificationScore =
                    Math.min(
                            certificationScore,
                            20
                    );

            score += certificationScore;

            strengths.add(
                    "reported certifications: "
                            + certifications
            );

        } else {

            missingInformation.add(
                    "certification information"
            );
        }

        /*
         * Availability information: maximum 10 points.
         */
        if (hasText(details.getAvailability())) {

            score += 10;

            strengths.add(
                    "availability provided"
            );

        } else {

            missingInformation.add(
                    "availability"
            );
        }

        /*
         * Transportation information: maximum 15 points.
         *
         * We score having usable transportation information,
         * while a positive reliable-transportation response
         * receives the full value.
         */
        String transportation =
                details.getTransportation();

        if (hasText(transportation)) {

            if (
                    isPositiveTransportation(
                            transportation
                    )
            ) {
                score += 15;

                strengths.add(
                        "reliable transportation reported"
                );

            } else {

                score += 5;
            }

        } else {

            missingInformation.add(
                    "transportation"
            );
        }

        /*
         * Desired work hours: maximum 10 points.
         */
        Integer desiredHours =
                details.getDesiredHoursPerWeek();

        if (desiredHours != null) {

            score += 10;

            strengths.add(
                    "desired hours: "
                            + desiredHours
                            + " per week"
            );

        } else {

            missingInformation.add(
                    "desired weekly hours"
            );
        }

        /*
         * Service area: maximum 10 points.
         */
        if (hasText(details.getServiceArea())) {

            score += 10;

            strengths.add(
                    "service area provided"
            );

        } else {

            missingInformation.add(
                    "service area"
            );
        }

        /*
         * Preferred schedule: maximum 5 points.
         */
        if (hasText(details.getPreferredSchedule())) {

            score += 5;
        }

        score =
                Math.min(
                        score,
                        100
                );

        String summary =
                buildSummary(
                        details,
                        score,
                        strengths,
                        missingInformation
                );

        return new CaregiverScreeningResult(
                score,
                summary
        );
    }

    private String buildSummary(
            CaregiverLeadDetails details,
            int score,
            List<String> strengths,
            List<String> missingInformation
    ) {

        StringBuilder summary =
                new StringBuilder();

        summary.append(
                "Caregiver screening information reviewed. "
        );

        if (!strengths.isEmpty()) {

            summary.append(
                    "Reported information includes "
            );

            summary.append(
                    String.join(
                            "; ",
                            strengths
                    )
            );

            summary.append(". ");
        }

        if (!missingInformation.isEmpty()) {

            summary.append(
                    "Additional information may be needed for "
            );

            summary.append(
                    String.join(
                            ", ",
                            missingInformation
                    )
            );

            summary.append(". ");
        }

        summary.append(
                "Screening readiness score: "
        );

        summary.append(score);

        summary.append(
                "/100. This score summarizes available job-related screening information and should be reviewed by a human before any employment decision."
        );

        return summary.toString();
    }

    private boolean isPositiveTransportation(
            String transportation
    ) {

        String normalized =
                transportation
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return normalized.equals("yes")
                || normalized.contains("reliable")
                || normalized.contains("own vehicle")
                || normalized.contains("own car")
                || normalized.contains("have a car");
    }

    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.isBlank();
    }
}