package com.scaleup.ai.qualification;

import com.scaleup.clientlead.ClientLeadDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleBasedClientQualificationService
        implements ClientQualificationService {

    @Override
    public ClientQualificationResult evaluate(
            ClientLeadDetails details
    ) {

        if (details == null) {
            throw new IllegalArgumentException(
                    "Client lead details must not be null."
            );
        }

        int score = 0;

        List<String> summaryParts =
                new ArrayList<>();

        /*
         * Service need
         * Maximum: 30
         */
        if (
                hasText(
                        details.getServiceNeeded()
                )
        ) {

            score += 30;

            summaryParts.add(
                    "service need identified as "
                            + details.getServiceNeeded()
            );
        }

        /*
         * Care start timeline
         * Maximum: 25
         */
        if (
                hasText(
                        details.getCareStartTimeline()
                )
        ) {

            score += 25;

            summaryParts.add(
                    "care timeline reported as "
                            + details.getCareStartTimeline()
            );
        }

        /*
         * Payer information
         * Maximum: 25
         */
        if (
                hasText(
                        details.getPayerType()
                )
        ) {

            score += 25;

            summaryParts.add(
                    "payer information reported as "
                            + details.getPayerType()
            );
        }

        /*
         * Decision maker
         * Maximum: 20
         */
        if (
                hasText(
                        details.getDecisionMaker()
                )
        ) {

            score += 20;

            summaryParts.add(
                    "decision maker reported as "
                            + details.getDecisionMaker()
            );
        }

        String summary =
                buildSummary(
                        score,
                        summaryParts
                );

        return new ClientQualificationResult(
                score,
                summary
        );
    }

    private String buildSummary(
            int score,
            List<String> summaryParts
    ) {

        StringBuilder summary =
                new StringBuilder(
                        "Client intake information reviewed."
                );

        if (!summaryParts.isEmpty()) {

            summary.append(
                    " Reported information includes "
            );

            summary.append(
                    String.join(
                            "; ",
                            summaryParts
                    )
            );

            summary.append(".");
        }

        summary.append(
                " Qualification readiness score: "
        );

        summary.append(
                score
        );

        summary.append(
                "/100."
        );

        summary.append(
                " This score summarizes available intake information and should be reviewed by the receiving agency."
        );

        return summary.toString();
    }

    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.isBlank();
    }
}