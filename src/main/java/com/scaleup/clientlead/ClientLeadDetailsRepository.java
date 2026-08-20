package com.scaleup.clientlead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientLeadDetailsRepository
        extends JpaRepository<ClientLeadDetails, Long> {

    Optional<ClientLeadDetails> findByLeadPublicId(
            UUID leadPublicId
    );

    List<ClientLeadDetails> findAllByLeadPublicIdIn(
            Collection<UUID> leadPublicIds
    );

    boolean existsByLeadPublicId(
            UUID leadPublicId
    );
}