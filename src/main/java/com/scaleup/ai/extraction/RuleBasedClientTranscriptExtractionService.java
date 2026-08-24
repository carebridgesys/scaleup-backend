package com.scaleup.ai.extraction;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RuleBasedClientTranscriptExtractionService
        implements ClientTranscriptExtractionService {

    @Override
    public ClientTranscriptExtraction extract(
            String transcript
    ) {

        if (
                transcript == null
                        || transcript.isBlank()
        ) {
            return new ClientTranscriptExtraction(
                    null,
                    null,
                    null,
                    null
            );
        }

        String normalized =
                transcript
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .trim();

        return new ClientTranscriptExtraction(
                extractServiceNeeded(
                        normalized
                ),
                extractCareStartTimeline(
                        normalized
                ),
                extractPayerType(
                        normalized
                ),
                extractDecisionMaker(
                        normalized
                )
        );
    }

    private String extractServiceNeeded(
            String transcript
    ) {

        if (
                containsAny(
                        transcript,
                        "personal care",
                        "bathing",
                        "dressing",
                        "toileting",
                        "grooming",
                        "activities of daily living",
                        "adl"
                )
        ) {
            return "Personal Care";
        }

        if (
                containsAny(
                        transcript,
                        "companion care",
                        "companionship",
                        "companion",
                        "socialization"
                )
        ) {
            return "Companion Care";
        }

        if (
                containsAny(
                        transcript,
                        "respite",
                        "respite care",
                        "caregiver relief",
                        "family caregiver relief"
                )
        ) {
            return "Respite Care";
        }

        if (
                containsAny(
                        transcript,
                        "homemaker",
                        "homemaker services",
                        "housekeeping",
                        "meal preparation",
                        "meal prep",
                        "light cleaning"
                )
        ) {
            return "Homemaker Services";
        }

        if (
                containsAny(
                        transcript,
                        "home care",
                        "care at home",
                        "in-home care",
                        "in home care"
                )
        ) {
            return "Other";
        }

        return null;
    }

    private String extractCareStartTimeline(
            String transcript
    ) {

        if (
                containsAny(
                        transcript,
                        "immediately",
                        "right away",
                        "as soon as possible",
                        "asap",
                        "today",
                        "tomorrow"
                )
        ) {
            return "Immediately";
        }

        if (
                containsAny(
                        transcript,
                        "within a week",
                        "within one week",
                        "next week",
                        "in a week"
                )
        ) {
            return "Within 1 Week";
        }

        if (
                containsAny(
                        transcript,
                        "within two weeks",
                        "within 2 weeks",
                        "in two weeks",
                        "in 2 weeks"
                )
        ) {
            return "Within 2 Weeks";
        }

        if (
                containsAny(
                        transcript,
                        "within 30 days",
                        "within a month",
                        "next month",
                        "in a month"
                )
        ) {
            return "Within 30 Days";
        }

        if (
                containsAny(
                        transcript,
                        "researching",
                        "not sure yet",
                        "not sure",
                        "just looking",
                        "exploring options"
                )
        ) {
            return "Researching/Not Sure";
        }

        return null;
    }

    private String extractPayerType(
            String transcript
    ) {

        if (
                containsAny(
                        transcript,
                        "private pay",
                        "pay privately",
                        "out of pocket",
                        "self pay",
                        "self-pay"
                )
        ) {
            return "Private Pay";
        }

        if (
                transcript.contains(
                        "medicaid"
                )
        ) {
            return "Medicaid";
        }

        if (
                transcript.contains(
                        "medicare"
                )
        ) {
            return "Medicare";
        }

        if (
                containsAny(
                        transcript,
                        "long-term care insurance",
                        "long term care insurance",
                        "ltc insurance"
                )
        ) {
            return "Long-Term Care Insurance";
        }

        if (
                containsAny(
                        transcript,
                        "veterans benefits",
                        "veteran benefits",
                        "va benefits",
                        "veterans affairs"
                )
        ) {
            return "Veterans Benefits";
        }

        if (
                containsAny(
                        transcript,
                        "not sure how",
                        "not sure about payment",
                        "not sure about insurance",
                        "not sure who pays"
                )
        ) {
            return "Not Sure";
        }

        return null;
    }

    private String extractDecisionMaker(
            String transcript
    ) {

        if (
                containsAny(
                        transcript,
                        "i am the decision maker",
                        "i make the decision",
                        "i will make the decision",
                        "i'm the decision maker",
                        "i am deciding"
                )
        ) {
            return "Self";
        }

        if (
                containsAny(
                        transcript,
                        "my spouse",
                        "husband",
                        "wife"
                )
        ) {
            return "Spouse";
        }

        if (
                containsAny(
                        transcript,
                        "my son",
                        "my daughter",
                        "my child",
                        "my family",
                        "family member"
                )
        ) {
            return "Child / Family member";
        }

        if (
                transcript.contains(
                        "guardian"
                )
        ) {
            return "Guardian";
        }

        return null;
    }

    private boolean containsAny(
            String source,
            String... values
    ) {

        for (String value : values) {

            if (source.contains(value)) {
                return true;
            }
        }

        return false;
    }
}