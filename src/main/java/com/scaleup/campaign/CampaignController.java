package com.scaleup.campaign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/agencies/{agencyPublicId}/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(
            CampaignService campaignService
    ) {
        this.campaignService =
                campaignService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse createCampaign(
            @PathVariable UUID agencyPublicId,
            @RequestBody CreateCampaignRequest request
    ) {
        return campaignService.createCampaign(
                agencyPublicId,
                request
        );
    }
    @GetMapping
    public List<CampaignResponse> getCampaigns(
            @PathVariable UUID agencyPublicId
    ) {
        return campaignService.getCampaigns(
                agencyPublicId
        );
    }
}