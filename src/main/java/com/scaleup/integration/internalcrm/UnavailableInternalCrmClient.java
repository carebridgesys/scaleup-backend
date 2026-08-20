package com.scaleup.integration.internalcrm;

import com.scaleup.lead.Lead;
import org.springframework.stereotype.Component;

@Component
public class UnavailableInternalCrmClient
        implements InternalCrmClient {

    @Override
    public InternalCrmSyncResult createLead(
            Lead lead
    ) {
        throw new IllegalStateException(
                "Internal CRM integration is not configured."
        );
    }
}