package com.scaleup.ai;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadAiContactRepository
        extends JpaRepository<LeadAiContact, Long> {

    @EntityGraph(attributePaths = "lead")
    Optional<LeadAiContact> findByLeadPublicId(
            UUID leadPublicId
    );

    boolean existsByLeadPublicId(
            UUID leadPublicId
    );

    List<LeadAiContact> findAllByStatus(
            AiContactStatus status
    );
}