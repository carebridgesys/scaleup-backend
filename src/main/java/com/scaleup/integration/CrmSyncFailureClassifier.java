package com.scaleup.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CrmSyncFailureClassifier {

    public CrmSyncFailure classify(
            RuntimeException exception
    ) {

        if (
                exception
                        instanceof ResourceAccessException
        ) {

            return new CrmSyncFailure(
                    true,
                    "CRM provider network request failed."
            );
        }

        if (
                exception
                        instanceof RestClientResponseException responseException
        ) {

            int status =
                    responseException
                            .getStatusCode()
                            .value();

            if (status == 408) {

                return new CrmSyncFailure(
                        true,
                        "CRM provider request timed out (HTTP 408)."
                );
            }

            if (status == 429) {

                return new CrmSyncFailure(
                        true,
                        "CRM provider rate limited the request (HTTP 429)."
                );
            }

            if (
                    status >= 500
                            && status <= 599
            ) {

                return new CrmSyncFailure(
                        true,
                        "CRM provider temporarily failed (HTTP "
                                + status
                                + ")."
                );
            }

            /*
             * 400 / 401 / 403 / 404 etc.
             *
             * Retrying these automatically usually
             * cannot fix the underlying problem.
             */
            return new CrmSyncFailure(
                    false,
                    "CRM provider rejected the request (HTTP "
                            + status
                            + ")."
            );
        }

        /*
         * Missing mappings, credentials, configuration,
         * invalid state, etc. should not loop repeatedly.
         */
        return new CrmSyncFailure(
                false,
                "CRM synchronization failed due to an application or configuration error."
        );
    }
}