package com.scaleup.integration.agencycrm;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgencyHighLevelConnectionRepository
        extends JpaRepository<AgencyHighLevelConnection, Long> {

    @EntityGraph(attributePaths = "agency")
    Optional<AgencyHighLevelConnection>
    findByAgencyPublicId(
            UUID agencyPublicId
    );
}