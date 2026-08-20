package com.scaleup.internalcrm;

import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.caregiverlead.CaregiverLeadDetailsRepository;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.clientlead.ClientLeadDetailsRepository;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.internalcrm.dto.CaregiverLeadDetailsResponse;
import com.scaleup.internalcrm.dto.ClientLeadDetailsResponse;
import com.scaleup.internalcrm.dto.LeadDetailResponse;
import com.scaleup.internalcrm.dto.LeadSummaryResponse;
import com.scaleup.internalcrm.dto.PageResponse;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import com.scaleup.lead.LeadStatus;
import com.scaleup.lead.LeadType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InternalLeadQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final LeadRepository leadRepository;

    private final ClientLeadDetailsRepository
            clientLeadDetailsRepository;

    private final CaregiverLeadDetailsRepository
            caregiverLeadDetailsRepository;

    public InternalLeadQueryService(
            LeadRepository leadRepository,
            ClientLeadDetailsRepository clientLeadDetailsRepository,
            CaregiverLeadDetailsRepository caregiverLeadDetailsRepository
    ) {
        this.leadRepository =
                leadRepository;

        this.clientLeadDetailsRepository =
                clientLeadDetailsRepository;

        this.caregiverLeadDetailsRepository =
                caregiverLeadDetailsRepository;
    }

    public PageResponse<LeadSummaryResponse> getLeads(
            UUID agencyId,
            UUID campaignId,
            LeadType leadType,
            LeadStatus status,
            int page,
            int size
    ) {

        int safePage =
                Math.max(page, 0);

        int safeSize =
                Math.min(
                        Math.max(size, 1),
                        MAX_PAGE_SIZE
                );

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Specification<Lead> specification =
                buildSpecification(
                        agencyId,
                        campaignId,
                        leadType,
                        status
                );

        Page<Lead> leadPage =
                leadRepository.findAll(
                        specification,
                        pageable
                );

        List<Lead> leads =
                leadPage.getContent();

        if (leads.isEmpty()) {
            return PageResponse.from(
                    leadPage,
                    Collections.emptyList()
            );
        }

        List<UUID> leadIds =
                leads.stream()
                        .map(Lead::getPublicId)
                        .toList();

        Map<UUID, ClientLeadDetails> clientDetails =
                clientLeadDetailsRepository
                        .findAllByLeadPublicIdIn(leadIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        details ->
                                                details.getLead()
                                                        .getPublicId(),
                                        Function.identity()
                                )
                        );

        Map<UUID, CaregiverLeadDetails> caregiverDetails =
                caregiverLeadDetailsRepository
                        .findAllByLeadPublicIdIn(leadIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        details ->
                                                details.getLead()
                                                        .getPublicId(),
                                        Function.identity()
                                )
                        );

        List<LeadSummaryResponse> responses =
                leads.stream()
                        .map(lead ->
                                toSummaryResponse(
                                        lead,
                                        clientDetails.get(
                                                lead.getPublicId()
                                        ),
                                        caregiverDetails.get(
                                                lead.getPublicId()
                                        )
                                )
                        )
                        .toList();

        return PageResponse.from(
                leadPage,
                responses
        );
    }

    public LeadDetailResponse getLead(
            UUID leadId
    ) {

        Lead lead =
                leadRepository
                        .findByPublicId(leadId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Lead was not found."
                                )
                        );

        ClientLeadDetails clientDetails =
                null;

        CaregiverLeadDetails caregiverDetails =
                null;

        if (lead.getLeadType() == LeadType.CLIENT) {
            clientDetails =
                    clientLeadDetailsRepository
                            .findByLeadPublicId(leadId)
                            .orElse(null);
        }

        if (lead.getLeadType() == LeadType.CAREGIVER) {
            caregiverDetails =
                    caregiverLeadDetailsRepository
                            .findByLeadPublicId(leadId)
                            .orElse(null);
        }

        return toDetailResponse(
                lead,
                clientDetails,
                caregiverDetails
        );
    }

    private Specification<Lead> buildSpecification(
            UUID agencyId,
            UUID campaignId,
            LeadType leadType,
            LeadStatus status
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (agencyId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("agency")
                                        .get("publicId"),
                                agencyId
                        )
                );
            }

            if (campaignId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("campaign")
                                        .get("publicId"),
                                campaignId
                        )
                );
            }

            if (leadType != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("leadType"),
                                leadType
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }

    private LeadSummaryResponse toSummaryResponse(
            Lead lead,
            ClientLeadDetails clientDetails,
            CaregiverLeadDetails caregiverDetails
    ) {

        Integer aiScore =
                resolveAiScore(
                        lead,
                        clientDetails,
                        caregiverDetails
                );

        return new LeadSummaryResponse(
                lead.getPublicId(),
                lead.getLeadType(),
                lead.getFirstName(),
                lead.getLastName(),
                lead.getPhone(),
                lead.getEmail(),
                lead.getZipCode(),
                lead.getPreferredContactMethod(),
                lead.getStatus(),
                lead.isConsentGiven(),

                lead.getAgency().getPublicId(),
                lead.getAgency().getName(),

                lead.getCampaign() == null
                        ? null
                        : lead.getCampaign().getPublicId(),

                lead.getCampaign() == null
                        ? null
                        : lead.getCampaign().getName(),

                lead.getCampaign() == null
                        ? lead.getSource()
                        : lead.getCampaign().getSource(),

                lead.getHighLevelSyncStatus(),

                aiScore,

                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }

    private LeadDetailResponse toDetailResponse(
            Lead lead,
            ClientLeadDetails clientDetails,
            CaregiverLeadDetails caregiverDetails
    ) {

        return new LeadDetailResponse(
                lead.getPublicId(),
                lead.getLeadType(),

                lead.getFirstName(),
                lead.getLastName(),

                lead.getPhone(),
                lead.getEmail(),
                lead.getZipCode(),

                lead.getPreferredContactMethod(),

                lead.getSource(),
                lead.getCampaignName(),

                lead.getStatus(),

                lead.isConsentGiven(),
                lead.getConsentTimestamp(),

                lead.getAgency().getPublicId(),
                lead.getAgency().getName(),

                lead.getCampaign() == null
                        ? null
                        : lead.getCampaign().getPublicId(),

                lead.getCampaign() == null
                        ? null
                        : lead.getCampaign().getName(),

                lead.getCampaign() == null
                        ? null
                        : lead.getCampaign().getSource(),

                lead.getHighLevelSyncStatus(),

                lead.getHighLevelContactId(),
                lead.getHighLevelOpportunityId(),

                toClientDetailsResponse(
                        clientDetails
                ),

                toCaregiverDetailsResponse(
                        caregiverDetails
                ),

                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }

    private Integer resolveAiScore(
            Lead lead,
            ClientLeadDetails clientDetails,
            CaregiverLeadDetails caregiverDetails
    ) {

        if (
                lead.getLeadType() == LeadType.CLIENT
                        && clientDetails != null
        ) {
            return clientDetails
                    .getAiQualificationScore();
        }

        if (
                lead.getLeadType() == LeadType.CAREGIVER
                        && caregiverDetails != null
        ) {
            return caregiverDetails
                    .getAiScreeningScore();
        }

        return null;
    }

    private ClientLeadDetailsResponse
    toClientDetailsResponse(
            ClientLeadDetails details
    ) {

        if (details == null) {
            return null;
        }

        return new ClientLeadDetailsResponse(
                details.getServiceNeeded(),
                details.getCareStartTimeline(),
                details.getPayerType(),
                details.getDecisionMaker(),
                details.getAiQualificationScore(),
                details.getAiSummary()
        );
    }

    private CaregiverLeadDetailsResponse
    toCaregiverDetailsResponse(
            CaregiverLeadDetails details
    ) {

        if (details == null) {
            return null;
        }

        return new CaregiverLeadDetailsResponse(
                details.getYearsExperience(),
                details.getCertifications(),
                details.getAvailability(),
                details.getTransportation(),
                details.getPreferredSchedule(),
                details.getDesiredHoursPerWeek(),
                details.getServiceArea(),
                details.getBackgroundCheckStatus(),
                details.getInterviewStatus(),
                details.getAiScreeningScore(),
                details.getAiScreeningSummary()
        );
    }
}