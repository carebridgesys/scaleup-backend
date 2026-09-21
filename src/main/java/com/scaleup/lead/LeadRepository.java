package com.scaleup.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository
        extends JpaRepository<Lead, Long>,
        JpaSpecificationExecutor<Lead> {

    /*
     * Agency and campaign are required by downstream
     * CRM synchronization logic.
     *
     * Both relationships are LAZY on Lead, so explicitly
     * fetch them here to prevent LazyInitializationException
     * when the lead is later used by an async CRM worker.
     */
    @EntityGraph(
            attributePaths = {
                    "agency",
                    "campaign"
            }
    )
    Optional<Lead> findByPublicId(
            UUID publicId
    );

    Page<Lead> findByAgencyPublicId(
            UUID agencyPublicId,
            Pageable pageable
    );

    Page<Lead> findByAgencyPublicIdAndLeadType(
            UUID agencyPublicId,
            LeadType leadType,
            Pageable pageable
    );

    Page<Lead> findByAgencyPublicIdAndStatus(
            UUID agencyPublicId,
            LeadStatus status,
            Pageable pageable
    );

    boolean existsByPublicId(
            UUID publicId
    );
}