package com.scaleup.onboarding;

public class AgencyOnboardingRequest {

    private String agencyName;
    private String agencySlug;
    private boolean createClientCampaign = true;
    private boolean createCaregiverCampaign = true;

    public AgencyOnboardingRequest() {
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getAgencySlug() {
        return agencySlug;
    }

    public void setAgencySlug(String agencySlug) {
        this.agencySlug = agencySlug;
    }

    public boolean isCreateClientCampaign() {
        return createClientCampaign;
    }

    public void setCreateClientCampaign(
            boolean createClientCampaign
    ) {
        this.createClientCampaign =
                createClientCampaign;
    }

    public boolean isCreateCaregiverCampaign() {
        return createCaregiverCampaign;
    }

    public void setCreateCaregiverCampaign(
            boolean createCaregiverCampaign
    ) {
        this.createCaregiverCampaign =
                createCaregiverCampaign;
    }
}