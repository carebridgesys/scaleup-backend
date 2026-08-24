package com.scaleup.ai.extraction;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RuleBasedCaregiverTranscriptExtractionService
        implements CaregiverTranscriptExtractionService {

    private static final Pattern YEARS_EXPERIENCE_PATTERN =
            Pattern.compile(
                    "\\b(\\d{1,2})\\s*(?:\\+\\s*)?(?:years?|yrs?)\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern DESIRED_HOURS_PATTERN =
            Pattern.compile(
                    "\\b(\\d{1,3})\\s*(?:hours?|hrs?)\\s*(?:per\\s*week|a\\s*week|weekly)?\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ZIP_PATTERN =
            Pattern.compile(
                    "\\b\\d{5}(?:-\\d{4})?\\b"
            );

    private static final Set<String> CERTIFICATIONS =
            Set.of(
                    "CNA",
                    "HHA",
                    "CPR",
                    "FIRST AID"
            );

    private static final List<String> KNOWN_SERVICE_AREAS =
            List.of(
                    "Harrisburg",
                    "Lancaster",
                    "Dauphin County",
                    "Cumberland County",
                    "York",
                    "Lebanon"
            );

    @Override
    public CaregiverTranscriptExtraction extract(
            String transcript
    ) {

        if (
                transcript == null
                        || transcript.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Caregiver transcript must not be blank."
            );
        }

        String normalized =
                transcript.trim();

        return new CaregiverTranscriptExtraction(
                extractYearsExperience(
                        normalized
                ),
                extractCertifications(
                        normalized
                ),
                extractAvailability(
                        normalized
                ),
                extractTransportation(
                        normalized
                ),
                extractPreferredSchedule(
                        normalized
                ),
                extractDesiredHours(
                        normalized
                ),
                extractServiceArea(
                        normalized
                )
        );
    }

    private Integer extractYearsExperience(
            String transcript
    ) {

        Matcher numericMatcher =
                YEARS_EXPERIENCE_PATTERN
                        .matcher(transcript);

        if (numericMatcher.find()) {

            Integer value =
                    Integer.valueOf(
                            numericMatcher.group(1)
                    );

            if (value >= 0 && value <= 80) {
                return value;
            }
        }

        String lower =
                transcript.toLowerCase(
                        Locale.ROOT
                );

        Map<String, Integer> writtenNumbers =
                Map.ofEntries(
                        Map.entry("zero", 0),
                        Map.entry("one", 1),
                        Map.entry("two", 2),
                        Map.entry("three", 3),
                        Map.entry("four", 4),
                        Map.entry("five", 5),
                        Map.entry("six", 6),
                        Map.entry("seven", 7),
                        Map.entry("eight", 8),
                        Map.entry("nine", 9),
                        Map.entry("ten", 10),
                        Map.entry("eleven", 11),
                        Map.entry("twelve", 12),
                        Map.entry("thirteen", 13),
                        Map.entry("fourteen", 14),
                        Map.entry("fifteen", 15),
                        Map.entry("sixteen", 16),
                        Map.entry("seventeen", 17),
                        Map.entry("eighteen", 18),
                        Map.entry("nineteen", 19),
                        Map.entry("twenty", 20)
                );

        for (
                Map.Entry<String, Integer> entry
                : writtenNumbers.entrySet()
        ) {

            Pattern writtenPattern =
                    Pattern.compile(
                            "\\b"
                                    + Pattern.quote(
                                    entry.getKey()
                            )
                                    + "\\s+(?:years?|yrs?)\\b",
                            Pattern.CASE_INSENSITIVE
                    );

            if (
                    writtenPattern
                            .matcher(lower)
                            .find()
            ) {
                return entry.getValue();
            }
        }

        return null;
    }

    private String extractCertifications(
            String transcript
    ) {

        String upper =
                transcript.toUpperCase(
                        Locale.ROOT
                );

        Set<String> matches =
                new LinkedHashSet<>();

        for (String certification : CERTIFICATIONS) {

            if (
                    containsCertification(
                            upper,
                            certification
                    )
            ) {
                matches.add(
                        certification
                );
            }
        }

        if (matches.isEmpty()) {
            return null;
        }

        return String.join(
                ", ",
                matches
        );
    }

    private boolean containsCertification(
            String transcript,
            String certification
    ) {

        if ("FIRST AID".equals(certification)) {
            return transcript.contains(
                    "FIRST AID"
            );
        }

        Pattern pattern =
                Pattern.compile(
                        "\\b"
                                + Pattern.quote(certification)
                                + "\\b",
                        Pattern.CASE_INSENSITIVE
                );

        return pattern
                .matcher(transcript)
                .find();
    }

    private Integer extractDesiredHours(
            String transcript
    ) {

        Matcher matcher =
                DESIRED_HOURS_PATTERN
                        .matcher(transcript);

        while (matcher.find()) {

            Integer value =
                    Integer.valueOf(
                            matcher.group(1)
                    );

            if (
                    value >= 0
                            && value <= 168
            ) {
                return value;
            }
        }

        return null;
    }

    private String extractServiceArea(
            String transcript
    ) {

        Set<String> areas =
                new LinkedHashSet<>();

        String lower =
                transcript.toLowerCase(
                        Locale.ROOT
                );

        for (String knownArea : KNOWN_SERVICE_AREAS) {

            if (
                    lower.contains(
                            knownArea.toLowerCase(
                                    Locale.ROOT
                            )
                    )
            ) {
                areas.add(
                        knownArea
                );
            }
        }

        Matcher zipMatcher =
                ZIP_PATTERN.matcher(
                        transcript
                );

        while (zipMatcher.find()) {
            areas.add(
                    zipMatcher.group()
            );
        }

        if (areas.isEmpty()) {
            return null;
        }

        return String.join(
                ", ",
                areas
        );
    }

    private String extractAvailability(
            String transcript
    ) {

        String lower =
                transcript.toLowerCase(
                        Locale.ROOT
                );

        List<String> availability =
                new ArrayList<>();

        addIfPresent(
                lower,
                availability,
                "weekday",
                "Weekdays"
        );

        addIfPresent(
                lower,
                availability,
                "weekend",
                "Weekends"
        );

        addIfPresent(
                lower,
                availability,
                "morning",
                "Mornings"
        );

        addIfPresent(
                lower,
                availability,
                "evening",
                "Evenings"
        );

        addIfPresent(
                lower,
                availability,
                "night",
                "Nights"
        );

        if (
                lower.contains("flexible")
        ) {
            availability.add(
                    "Flexible"
            );
        }

        if (availability.isEmpty()) {
            return null;
        }

        return String.join(
                ", ",
                new LinkedHashSet<>(
                        availability
                )
        );
    }

    private String extractPreferredSchedule(
            String transcript
    ) {

        String lower =
                transcript.toLowerCase(
                        Locale.ROOT
                );

        if (
                lower.contains("full time")
                        || lower.contains("full-time")
        ) {
            return "Full Time";
        }

        if (
                lower.contains("part time")
                        || lower.contains("part-time")
        ) {
            return "Part Time";
        }

        if (
                lower.contains("weekend")
        ) {
            return "Weekends";
        }

        if (
                lower.contains("evening")
        ) {
            return "Evenings";
        }

        if (
                lower.contains("night")
        ) {
            return "Nights";
        }

        return null;
    }

    private String extractTransportation(
            String transcript
    ) {

        String lower =
                transcript.toLowerCase(
                        Locale.ROOT
                );

        if (
                lower.contains(
                        "reliable transportation"
                )
                        || lower.contains(
                        "own transportation"
                )
                        || lower.contains(
                        "have a car"
                )
                        || lower.contains(
                        "own a car"
                )
        ) {
            return "Yes";
        }

        if (
                lower.contains(
                        "no transportation"
                )
                        || lower.contains(
                        "do not have transportation"
                )
                        || lower.contains(
                        "don't have transportation"
                )
                        || lower.contains(
                        "do not have a car"
                )
                        || lower.contains(
                        "don't have a car"
                )
        ) {
            return "No";
        }

        return null;
    }

    private void addIfPresent(
            String source,
            List<String> values,
            String needle,
            String value
    ) {

        if (source.contains(needle)) {
            values.add(value);
        }
    }
}