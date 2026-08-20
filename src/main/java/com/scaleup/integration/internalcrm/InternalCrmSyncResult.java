package com.scaleup.integration.internalcrm;

public record InternalCrmSyncResult(

        String externalContactId,
        String externalOpportunityId

) {
}