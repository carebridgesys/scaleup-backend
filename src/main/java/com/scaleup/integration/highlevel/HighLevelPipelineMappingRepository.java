package com.scaleup.integration.highlevel;

import com.scaleup.lead.LeadType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HighLevelPipelineMappingRepository
        extends JpaRepository<HighLevelPipelineMapping, Long> {

    /*
     * New agency-specific lookup.
     */
    Optional<HighLevelPipelineMapping>
    findByAgencyPublicIdAndLocationIdAndLeadTypeAndActiveTrue(
            UUID agencyPublicId,
            String locationId,
            LeadType leadType
    );

    Optional<HighLevelPipelineMapping>
    findByAgencyPublicIdAndLocationIdAndLeadType(
            UUID agencyPublicId,
            String locationId,
            LeadType leadType
    );

    List<HighLevelPipelineMapping>
    findAllByAgencyPublicIdAndLocationIdOrderByLeadTypeAsc(
            UUID agencyPublicId,
            String locationId
    );

    /*
     * Temporary legacy fallback.
     */
    Optional<HighLevelPipelineMapping>
    findByAgencyIsNullAndLocationIdAndLeadTypeAndActiveTrue(
            String locationId,
            LeadType leadType
    );

    Optional<HighLevelPipelineMapping>
    findByAgencyIsNullAndLocationIdAndLeadType(
            String locationId,
            LeadType leadType
    );
}