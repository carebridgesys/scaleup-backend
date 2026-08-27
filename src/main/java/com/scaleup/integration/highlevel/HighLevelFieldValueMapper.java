package com.scaleup.integration.highlevel;

import org.springframework.stereotype.Component;

@Component
public class HighLevelFieldValueMapper {

    public String mapServiceNeeded(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {

            case "Personal Care",
                 "Companion Care",
                 "Respite Care",
                 "Homemaker Services",
                 "Other" ->
                    normalized;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported service needed value: "
                                    + normalized
                    );
        };
    }

    public String mapCareStartTimeline(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {

            case "Immediately" ->
                    "Immediately";

            case "Within 1 Week" ->
                    "Within 1 Week";

            case "Within 2 Weeks" ->
                    "Within 2 Weeks";

            case "Within 30 Days" ->
                    "Within 30 Days";

            case "Researching / Not Sure",
                 "Researching/Not Sure" ->
                    "Researching/Not Sure";

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported care start timeline: "
                                    + normalized
                    );
        };
    }

    public String mapPayerType(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {

            case "Private Pay",
                 "Medicaid",
                 "Medicare",
                 "Long-Term Care Insurance",
                 "Veterans Benefits",
                 "Not Sure",
                 "Other" ->
                    normalized;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported payer type: "
                                    + normalized
                    );
        };
    }

    public String mapDecisionMaker(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {

            case "Self" ->
                    "Self";

            case "Spouse" ->
                    "Spouse";

            case "Child / Family Member",
                 "Child / Family member" ->
                    "Child / Family member";

            case "Guardian" ->
                    "Guardian";

            case "Other" ->
                    "Other";

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported decision maker: "
                                    + normalized
                    );
        };
    }

    public String mapPreferredContactMethod(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {

            case "PHONE",
                 "Phone",
                 "Phone Call" ->
                    "Phone Call";

            case "TEXT",
                 "Text",
                 "Text Message" ->
                    "Text Message";

            case "EMAIL",
                 "Email" ->
                    "Email";

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported preferred contact method: "
                                    + normalized
                    );
        };
    }

    public String mapLeadSource(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized.toUpperCase()) {

            case "FACEBOOK" ->
                    "Facebook";

            case "GOOGLE" ->
                    "Google";

            case "INSTAGRAM" ->
                    "Instagram";

            case "REFERRAL" ->
                    "Referral";

            case "WEBSITE" ->
                    "Website";

            case "LANDING_PAGE" ->
                    "Website";

            case "OTHER" ->
                    "Other";

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported lead source: "
                                    + normalized
                    );
        };
    }
    public String mapAvailability(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {
            case "Full Time",
                 "Part Time",
                 "Weekdays",
                 "Weekends",
                 "Evenings",
                 "Overnight",
                 "Flexible" -> normalized;

            default -> throw new IllegalArgumentException(
                    "Unsupported caregiver availability: " + normalized
            );
        };
    }

    public String mapTransportation(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {
            case "Own Vehicle",
                 "Reliable Transportation",
                 "Public Transportation",
                 "No Transportation" -> normalized;

            default -> throw new IllegalArgumentException(
                    "Unsupported transportation value: " + normalized
            );
        };
    }

    public String mapPreferredSchedule(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {
            case "Day Shift",
                 "Evening Shift",
                 "Overnight Shift",
                 "Weekends",
                 "Flexible" -> normalized;

            default -> throw new IllegalArgumentException(
                    "Unsupported preferred schedule: " + normalized
            );
        };
    }

    public String mapBackgroundCheckStatus(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {
            case "Not Started",
                 "Pending",
                 "Cleared",
                 "Issue Found" -> normalized;

            default -> throw new IllegalArgumentException(
                    "Unsupported background check status: " + normalized
            );
        };
    }

    public String mapInterviewStatus(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return switch (normalized) {
            case "Not Scheduled",
                 "Scheduled",
                 "Completed",
                 "No Show",
                 "Reschedule Needed" -> normalized;

            default -> throw new IllegalArgumentException(
                    "Unsupported interview status: " + normalized
            );
        };
    }
}