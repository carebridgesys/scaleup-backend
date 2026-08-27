package com.scaleup.integration.internalcrm;

import com.scaleup.agency.Agency;
import com.scaleup.agency.AgencyRepository;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.highlevel.HighLevelPipelineMapping;
import com.scaleup.integration.highlevel.HighLevelPipelineMappingRepository;
import com.scaleup.integration.highlevel.HighLevelProperties;
import com.scaleup.lead.LeadType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AgencyInternalCrmPipelineMappingService {

    private final AgencyRepository agencyRepository;

    private final HighLevelPipelineMappingRepository
            pipelineMappingRepository;

    private final HighLevelProperties
            highLevelProperties;

    public AgencyInternalCrmPipelineMappingService(
            AgencyRepository agencyRepository,
            HighLevelPipelineMappingRepository pipelineMappingRepository,
            HighLevelProperties highLevelProperties
    ) {

        this.agencyRepository =
                agencyRepository;

        this.pipelineMappingRepository =
                pipelineMappingRepository;

        this.highLevelProperties =
                highLevelProperties;
    }

    @Transactional
    public void configureMappings(
            UUID agencyPublicId,
            InternalCrmPipelineMappingsRequest request
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

        String locationId =
                requireText(
                        highLevelProperties
                                .getInternalCrm()
                                .getLocationId(),
                        "Internal CRM location ID"
                );

        upsert(
                agency,
                locationId,
                LeadType.CLIENT,
                request.client()
        );

        upsert(
                agency,
                locationId,
                LeadType.CAREGIVER,
                request.caregiver()
        );
    }

    private void upsert(
            Agency agency,
            String locationId,
            LeadType leadType,
            InternalCrmPipelineMappingRequest request
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

        mapping.updateLifecycleStages(
                request.attemptingContactStageId(),
                request.contactedStageId(),
                request.qualifiedStageId(),
                request.routedStageId()
        );

        pipelineMappingRepository.save(
                mapping
        );
    }

    private String requireText(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new IllegalStateException(
                    fieldName
                            + " must be configured."
            );
        }

        return value.trim();
    }
}