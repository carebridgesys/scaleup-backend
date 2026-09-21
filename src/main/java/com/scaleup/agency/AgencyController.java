package com.scaleup.agency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    public AgencyController(
            AgencyService agencyService
    ) {
        this.agencyService = agencyService;
    }

    @GetMapping
    public List<AgencyResponse> getAgencies() {
        return agencyService.getAgencies();
    }
}