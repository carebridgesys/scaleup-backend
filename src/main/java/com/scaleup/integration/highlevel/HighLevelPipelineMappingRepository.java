package com.scaleup.integration.highlevel;

import com.scaleup.lead.LeadType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HighLevelPipelineMappingRepository
        extends JpaRepository<HighLevelPipelineMapping, Long> {

    Optional<HighLevelPipelineMapping>
    findByLocationIdAndLeadTypeAndActiveTrue(
            String locationId,
            LeadType leadType
    );
}