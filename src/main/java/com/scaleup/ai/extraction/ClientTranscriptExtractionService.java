package com.scaleup.ai.extraction;

public interface ClientTranscriptExtractionService {

    ClientTranscriptExtraction extract(
            String transcript
    );
}