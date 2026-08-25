package com.scaleup.agency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgencyRepository
        extends JpaRepository<Agency, Long> {

    Optional<Agency> findByPublicId(UUID publicId);

    Optional<Agency> findBySlug(String slug);



    boolean existsByPublicId(UUID publicId);

    boolean existsBySlug(String slug);


}