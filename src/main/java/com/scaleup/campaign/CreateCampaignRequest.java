package com.scaleup.campaign;

public class CreateCampaignRequest {

    private String campaignName;
    private String campaignSlug;
    private CampaignType campaignType;
    private String source;
    private String externalCampaignId;

    public CreateCampaignRequest() {
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCampaignSlug() {
        return campaignSlug;
    }

    public void setCampaignSlug(String campaignSlug) {
        this.campaignSlug = campaignSlug;
    }

    public CampaignType getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(CampaignType campaignType) {
        this.campaignType = campaignType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalCampaignId() {
        return externalCampaignId;
    }

    public void setExternalCampaignId(String externalCampaignId) {
        this.externalCampaignId = externalCampaignId;
    }
}