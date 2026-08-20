package com.scaleup.integration.highlevel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HighLevelCustomFieldMappingRepository
        extends JpaRepository<
        HighLevelCustomFieldMapping,
        Long
        > {

    Optional<HighLevelCustomFieldMapping>
    findByLocationIdAndFieldKeyAndActiveTrue(
            String locationId,
            String fieldKey
    );

    List<HighLevelCustomFieldMapping>
    findAllByLocationIdAndActiveTrue(
            String locationId
    );

    boolean existsByLocationIdAndFieldKey(
            String locationId,
            String fieldKey
    );
}