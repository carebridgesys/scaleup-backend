package com.scaleup.agency;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgencyService {

    private final AgencyRepository agencyRepository;

    public AgencyService(
            AgencyRepository agencyRepository
    ) {
        this.agencyRepository = agencyRepository;
    }

    @Transactional(readOnly = true)
    public List<AgencyResponse> getAgencies() {
        return agencyRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.ASC,
                                "name"
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AgencyResponse toResponse(
            Agency agency
    ) {
        return new AgencyResponse(
                agency.getPublicId(),
                agency.getName(),
                agency.getSlug(),
                agency.getHighLevelLocationId(),
                agency.isHighLevelSyncEnabled(),
                agency.isActive(),
                agency.getCreatedAt(),
                agency.getUpdatedAt()
        );
    }
}