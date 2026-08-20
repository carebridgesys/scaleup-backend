package com.scaleup.caregiverlead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaregiverLeadDetailsRepository
        extends JpaRepository<CaregiverLeadDetails, Long> {

    Optional<CaregiverLeadDetails> findByLeadPublicId(
            UUID leadPublicId
    );

    List<CaregiverLeadDetails> findAllByLeadPublicIdIn(
            Collection<UUID> leadPublicIds
    );

    boolean existsByLeadPublicId(
            UUID leadPublicId
    );
}