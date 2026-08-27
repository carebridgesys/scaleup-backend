package com.scaleup.integration.agencycrm;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.lead.LeadType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AgencyHighLevelPipelineMappingService {

    private final AgencyRepository
            agencyRepository;

    private final AgencyHighLevelConnectionRepository
            connectionRepository;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    public AgencyHighLevelPipelineMappingService(
            AgencyRepository agencyRepository,
            AgencyHighLevelConnectionRepository connectionRepository,
            HighLevelPipelineMappingRepository pipelineMappingRepository
    ) {

        this.agencyRepository =
                agencyRepository;

        this.connectionRepository =
                connectionRepository;

        this.pipelineMappingRepository =
                pipelineMappingRepository;
    }

    @Transactional
    public List<AgencyPipelineMappingResponse>
    configureMappings(
            UUID agencyPublicId,
            AgencyHighLevelPipelineMappingsRequest request
    ) {

        Agency agency =
                agencyRepository
                        .findByPublicId(
                                agencyPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Agency was not found."
                                )
                        );

        AgencyHighLevelConnection connection =
                connectionRepository
                        .findByAgencyPublicId(
                                agencyPublicId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HighLevel connection must be configured before pipeline mappings."
                                )
                        );

        if (
                connection.getConnectionStatus()
                        != AgencyCrmConnectionStatus.ACTIVE
        ) {

            throw new IllegalStateException(
                    "HighLevel connection must be active before configuring pipeline mappings."
            );
        }

        String locationId =
                connection
                        .getLocationId();

        upsertMapping(
                agency,
                locationId,
                LeadType.CLIENT,
                request.client()
        );

        upsertMapping(
                agency,
                locationId,
                LeadType.CAREGIVER,
                request.caregiver()
        );

        /*
         * This transitional flag still indicates
         * that the agency is enabled for HighLevel routing.
         */
        agency.enableHighLevelSync();

        agencyRepository.save(
                agency
        );

        return getMappings(
                agencyPublicId
        );
    }

    @Transactional(readOnly = true)
    public List<AgencyPipelineMappingResponse>
    getMappings(
            UUID agencyPublicId
    ) {

        AgencyHighLevelConnection connection =
                connectionRepository
                        .findByAgencyPublicId(
                                agencyPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "HighLevel connection was not found for agency."
                                )
                        );

        return pipelineMappingRepository
                .findAllByAgencyPublicIdAndLocationIdOrderByLeadTypeAsc(
                        agencyPublicId,
                        connection.getLocationId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void upsertMapping(
            Agency agency,
            String locationId,
            LeadType leadType,
            AgencyPipelineMappingRequest request
    ) {

        HighLevelPipelineMapping mapping =
                pipelineMappingRepository
                        .findByAgencyPublicIdAndLocationIdAndLeadType(
                                agency.getPublicId(),
                                locationId,
                                leadType
                        )
                        .orElse(null);

        if (mapping == null) {

            mapping =
                    new HighLevelPipelineMapping(
                            agency,
                            locationId,
                            leadType,
                            request.pipelineId(),
                            request.initialStageId(),
                            request.pipelineName(),
                            request.initialStageName()
                    );

        } else {

            mapping.updateMapping(
                    request.pipelineId(),
                    request.initialStageId(),
                    request.pipelineName(),
                    request.initialStageName()
            );
        }

        pipelineMappingRepository.save(
                mapping
        );
    }

    private AgencyPipelineMappingResponse toResponse(
            HighLevelPipelineMapping mapping
    ) {

        return new AgencyPipelineMappingResponse(
                mapping.getLeadType().name(),
                mapping.getPipelineId(),
                mapping.getInitialStageId(),
                mapping.getPipelineName(),
                mapping.getInitialStageName(),
                mapping.isActive()
        );
    }
}