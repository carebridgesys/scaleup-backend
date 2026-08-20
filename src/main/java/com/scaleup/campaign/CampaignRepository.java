package com.scaleup.campaign;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository
        extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByPublicId(
            UUID publicId
    );

    Optional<Campaign> findByLandingPageKey(
            String landingPageKey
    );

    Optional<Campaign> findByAgencyPublicIdAndSlug(
            UUID agencyPublicId,
            String slug
    );

    Page<Campaign> findByAgencyPublicId(
            UUID agencyPublicId,
            Pageable pageable
    );

    Page<Campaign> findByAgencyPublicIdAndCampaignType(
            UUID agencyPublicId,
            CampaignType campaignType,
            Pageable pageable
    );

    boolean existsByLandingPageKey(
            String landingPageKey
    );

    boolean existsByAgencyPublicIdAndSlug(
            UUID agencyPublicId,
            String slug
    );
}