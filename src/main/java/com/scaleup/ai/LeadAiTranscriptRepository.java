package com.scaleup.ai;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadAiTranscriptRepository
        extends JpaRepository<LeadAiTranscript, Long> {

    @EntityGraph(attributePaths = "lead")
    List<LeadAiTranscript>
    findAllByLeadPublicIdOrderByReceivedAtDesc(
            UUID leadPublicId
    );

    Optional<LeadAiTranscript>
    findFirstByProviderAndExternalCallId(
            String provider,
            String externalCallId
    );

    Optional<LeadAiTranscript>
    findFirstByEventKey(
            String eventKey
    );

    boolean existsByEventKey(
            String eventKey
    );
}