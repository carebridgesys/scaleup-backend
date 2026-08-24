package com.scaleup.integration.agencycrm;

public record AgencyCrmSyncResult(

        String externalContactId,
        String externalOpportunityId

) {
}