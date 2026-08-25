package com.scaleup.integration.highlevel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "highlevel")
public class HighLevelProperties {

    private String baseUrl;

    private InternalCrm internalCrm =
            new InternalCrm();

    private Http http =
            new Http();

    private Webhook webhook =
            new Webhook();

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
        this.internalCrm =
                internalCrm != null
                        ? internalCrm
                        : new InternalCrm();
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(
            Http http
    ) {
        this.http =
                http != null
                        ? http
                        : new Http();
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(
            Webhook webhook
    ) {
        this.webhook =
                webhook != null
                        ? webhook
                        : new Webhook();
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
            this.locationId =
                    locationId;
        }
    }

    public static class Http {

        private int connectTimeoutMs =
                5000;

        private int readTimeoutMs =
                15000;

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(
                int connectTimeoutMs
        ) {
            this.connectTimeoutMs =
                    connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(
                int readTimeoutMs
        ) {
            this.readTimeoutMs =
                    readTimeoutMs;
        }
    }

    public static class Webhook {

        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(
                String secret
        ) {
            this.secret = secret;
        }
    }
}