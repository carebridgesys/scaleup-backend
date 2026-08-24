package com.scaleup.integration.highlevel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "highlevel")
public class HighLevelProperties {

    private String baseUrl;

    private InternalCrm internalCrm =
            new InternalCrm();

    private Map<String, AgencyCrm> agencies =
            new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(
            String baseUrl
    ) {
        this.baseUrl = baseUrl;
    }

    public InternalCrm getInternalCrm() {
        return internalCrm;
    }

    public void setInternalCrm(
            InternalCrm internalCrm
    ) {
        this.internalCrm = internalCrm;
    }

    public Map<String, AgencyCrm> getAgencies() {
        return agencies;
    }

    public void setAgencies(
            Map<String, AgencyCrm> agencies
    ) {
        this.agencies =
                agencies != null
                        ? agencies
                        : new HashMap<>();
    }

    public AgencyCrm getAgencyConfiguration(
            String agencySlug
    ) {

        if (
                agencySlug == null
                        || agencySlug.isBlank()
        ) {
            return null;
        }

        return agencies.get(
                agencySlug.trim()
        );
    }

    public static class InternalCrm {

        private String token;
        private String locationId;

        public String getToken() {
            return token;
        }

        public void setToken(
                String token
        ) {
            this.token = token;
        }

        public String getLocationId() {
            return locationId;
        }

        public void setLocationId(
                String locationId
        ) {
            this.locationId = locationId;
        }
    }

    public static class AgencyCrm {

        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(
                String token
        ) {
            this.token = token;
        }
    }
}