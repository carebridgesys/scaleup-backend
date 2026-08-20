package com.scaleup.integration.highlevel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "highlevel")
public class HighLevelProperties {

    private String baseUrl;

    private InternalCrm internalCrm = new InternalCrm();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
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

    public static class InternalCrm {

        private String token;
        private String locationId;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getLocationId() {
            return locationId;
        }

        public void setLocationId(String locationId) {
            this.locationId = locationId;
        }
    }
}