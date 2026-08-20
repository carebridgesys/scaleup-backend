package com.scaleup.integration;

import com.scaleup.lead.Lead;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadCrmSyncRepository
        extends JpaRepository<LeadCrmSync, Long> {

    Optional<LeadCrmSync>
    findByLeadPublicIdAndDestination(
            UUID leadPublicId,
            CrmDestination destination
    );

    List<LeadCrmSync>
    findAllByLeadPublicId(
            UUID leadPublicId
    );

    List<LeadCrmSync>
    findAllByDestinationAndSyncStatus(
            CrmDestination destination,
            CrmSyncStatus syncStatus
    );

    boolean existsByLeadAndDestination(
            Lead lead,
            CrmDestination destination
    );

    @Query("""
            select s.lead.publicId
            from LeadCrmSync s
            where s.destination = :destination
              and s.syncStatus = :syncStatus
              and s.nextAttemptAt is not null
              and s.nextAttemptAt <= :now
              and s.attemptCount < :maxAttempts
            order by s.nextAttemptAt asc
            """)
    List<UUID> findRetryEligibleLeadIds(
            @Param("destination")
            CrmDestination destination,

            @Param("syncStatus")
            CrmSyncStatus syncStatus,

            @Param("now")
            LocalDateTime now,

            @Param("maxAttempts")
            int maxAttempts,

            Pageable pageable
    );
}