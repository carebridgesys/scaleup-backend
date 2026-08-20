package com.scaleup.integration.internalcrm;

import com.scaleup.lead.Lead;

public interface InternalCrmClient {

    InternalCrmSyncResult createLead(
            Lead lead
    );
}