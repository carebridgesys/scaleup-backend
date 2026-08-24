package com.scaleup.integration.agencycrm;

import com.scaleup.lead.Lead;

public interface AgencyCrmClient {

    AgencyCrmSyncResult createLead(
            Lead lead
    );
}