package com.scaleup.publicapi;

import com.scaleup.campaign.Campaign;
import com.scaleup.campaign.CampaignRepository;
import com.scaleup.campaign.CampaignType;
import com.scaleup.caregiverlead.CaregiverLeadDetails;
import com.scaleup.caregiverlead.CaregiverLeadDetailsRepository;
import com.scaleup.clientlead.ClientLeadDetails;
import com.scaleup.clientlead.ClientLeadDetailsRepository;
import com.scaleup.common.exception.InvalidRequestException;
import com.scaleup.common.exception.ResourceNotFoundException;
import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSync;
import com.scaleup.integration.LeadCrmSyncRepository;
import com.scaleup.lead.Lead;
import com.scaleup.lead.LeadRepository;
import com.scaleup.lead.LeadType;
import com.scaleup.publicapi.dto.CaregiverLeadRequest;
import com.scaleup.publicapi.dto.ClientLeadRequest;
import com.scaleup.publicapi.dto.CreateLeadRequest;
import com.scaleup.publicapi.dto.LeadCreatedResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scaleup.integration.internalcrm.InternalCrmSyncRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

@Service
public class PublicLeadService {

    private final CampaignRepository campaignRepository;
    private final LeadRepository leadRepository;

    private final ClientLeadDetailsRepository
            clientLeadDetailsRepository;

    private final CaregiverLeadDetailsRepository
            caregiverLeadDetailsRepository;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final ApplicationEventPublisher eventPublisher;

    public PublicLeadService(
            CampaignRepository campaignRepository,
            LeadRepository leadRepository,
            ClientLeadDetailsRepository clientLeadDetailsRepository,
            CaregiverLeadDetailsRepository caregiverLeadDetailsRepository,
            LeadCrmSyncRepository leadCrmSyncRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.campaignRepository =
                campaignRepository;

        this.leadRepository =
                leadRepository;

        this.clientLeadDetailsRepository =
                clientLeadDetailsRepository;

        this.caregiverLeadDetailsRepository =
                caregiverLeadDetailsRepository;

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.eventPublisher =
                eventPublisher;

    }

    @Transactional
    public LeadCreatedResponse createLead(
            CreateLeadRequest request
    ) {

        String campaignKey =
                normalizeRequired(
                        request.campaignKey(),
                        "Campaign key"
                );

        Campaign campaign =
                campaignRepository
                        .findByLandingPageKey(
                                campaignKey
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Campaign was not found."
                                )
                        );

        LocalDateTime now =
                LocalDateTime.now();

        validateCampaign(
                campaign,
                now
        );

        LeadType leadType =
                resolveLeadType(
                        campaign.getCampaignType()
                );

        validateDetailPayload(
                leadType,
                request
        );

        Lead lead =
                new Lead(
                        campaign.getAgency(),
                        leadType,
                        request.firstName(),
                        request.phone()
                );

        lead.assignCampaign(
                campaign
        );

        lead.updateName(
                request.firstName(),
                request.lastName()
        );

        lead.updateContactInformation(
                request.phone(),
                request.email()
        );

        lead.updateZipCode(
                request.zipCode()
        );

        lead.updatePreferredContactMethod(
                request.preferredContactMethod()
        );

        lead.updateInitialRequestNotes(
                request.initialRequestNotes()
        );

        lead.updateMarketingAttribution(
                campaign.getSource(),
                campaign.getName()
        );

        if (
                Boolean.TRUE.equals(
                        request.consentGiven()
                )
        ) {
            lead.recordConsent(now);
        }

        Lead savedLead =
                leadRepository.save(lead);

        createLeadDetails(
                savedLead,
                request
        );

        createInitialCrmSyncRecords(
                savedLead
        );

        /*
         * Flush so database constraint failures happen
         * before the API response is returned.
         */
        leadRepository.flush();
        clientLeadDetailsRepository.flush();
        caregiverLeadDetailsRepository.flush();
        leadCrmSyncRepository.flush();
        eventPublisher.publishEvent(
                new InternalCrmSyncRequestedEvent(
                        savedLead.getPublicId()
                )
        );

        return new LeadCreatedResponse(
                savedLead.getPublicId(),
                savedLead.getLeadType(),
                savedLead.getStatus().name(),
                savedLead.getCreatedAt()
        );
    }

    private void createLeadDetails(
            Lead lead,
            CreateLeadRequest request
    ) {

        switch (lead.getLeadType()) {

            case CLIENT ->
                    createClientDetails(
                            lead,
                            request.clientDetails()
                    );

            case CAREGIVER ->
                    createCaregiverDetails(
                            lead,
                            request.caregiverDetails()
                    );
        }
    }

    private void createInitialCrmSyncRecords(
            Lead lead
    ) {

        LeadCrmSync internalCrmSync =
                new LeadCrmSync(
                        lead,
                        CrmDestination.INTERNAL_CRM,
                        CrmSyncStatus.PENDING
                );

        LeadCrmSync agencyCrmSync =
                new LeadCrmSync(
                        lead,
                        CrmDestination.AGENCY_CRM,
                        CrmSyncStatus.NOT_REQUIRED
                );

        leadCrmSyncRepository.save(
                internalCrmSync
        );

        leadCrmSyncRepository.save(
                agencyCrmSync
        );
    }

    private void validateCampaign(
            Campaign campaign,
            LocalDateTime now
    ) {

        if (!campaign.getAgency().isActive()) {
            throw new InvalidRequestException(
                    "This campaign is not currently accepting submissions."
            );
        }

        if (!campaign.isCurrentlyActive(now)) {
            throw new InvalidRequestException(
                    "This campaign is not currently accepting submissions."
            );
        }
    }

    private LeadType resolveLeadType(
            CampaignType campaignType
    ) {

        return switch (campaignType) {

            case CLIENT ->
                    LeadType.CLIENT;

            case CAREGIVER ->
                    LeadType.CAREGIVER;
        };
    }

    private void validateDetailPayload(
            LeadType leadType,
            CreateLeadRequest request
    ) {

        if (leadType == LeadType.CLIENT) {

            if (request.caregiverDetails() != null) {
                throw new InvalidRequestException(
                        "Caregiver details cannot be submitted for a client campaign."
                );
            }

            return;
        }

        if (request.clientDetails() != null) {
            throw new InvalidRequestException(
                    "Client details cannot be submitted for a caregiver campaign."
            );
        }
    }

    private void createClientDetails(
            Lead lead,
            ClientLeadRequest request
    ) {

        ClientLeadDetails details =
                new ClientLeadDetails(
                        lead
                );

        if (request != null) {

            details.updateIntakeInformation(
                    request.serviceNeeded(),
                    request.careStartTimeline(),
                    request.payerType(),
                    request.decisionMaker()
            );
        }

        clientLeadDetailsRepository.save(
                details
        );
    }

    private void createCaregiverDetails(
            Lead lead,
            CaregiverLeadRequest request
    ) {

        CaregiverLeadDetails details =
                new CaregiverLeadDetails(
                        lead
                );

        if (request != null) {

            details.updateExperience(
                    request.yearsExperience(),
                    request.certifications()
            );

            details.updateAvailability(
                    request.availability(),
                    request.preferredSchedule(),
                    request.desiredHoursPerWeek()
            );

            details.updateTransportation(
                    request.transportation()
            );

            details.updateServiceArea(
                    request.serviceArea()
            );
        }

        caregiverLeadDetailsRepository.save(
                details
        );
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new InvalidRequestException(
                    fieldName
                            + " must not be blank."
            );
        }

        return value.trim();
    }
}