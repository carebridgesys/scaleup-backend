package com.scaleup.ai.extraction;

public interface CaregiverTranscriptExtractionService {

    CaregiverTranscriptExtraction extract(
            String transcript
    );
}