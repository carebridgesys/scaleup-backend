package com.scaleup.integration.agencycrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AgencyCrmSyncEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AgencyCrmSyncEventListener.class
            );

    private final AgencyCrmSyncService
            agencyCrmSyncService;

    public AgencyCrmSyncEventListener(
            AgencyCrmSyncService agencyCrmSyncService
    ) {
        this.agencyCrmSyncService =
                agencyCrmSyncService;
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            AgencyCrmSyncRequestedEvent event
    ) {

        try {

            log.info(
                    "Starting automatic Agency CRM sync for lead {}",
                    event.leadId()
            );

            agencyCrmSyncService.syncLead(
                    event.leadId()
            );

            log.info(
                    "Automatic Agency CRM sync finished for lead {}",
                    event.leadId()
            );

        } catch (Exception ex) {

            /*
             * Do not rethrow.
             *
             * The AI qualification transaction has
             * already committed successfully.
             *
             * AgencyCrmSyncService is responsible for
             * marking the CRM sync record FAILED and
             * recording retry/error information.
             */
            log.error(
                    "Automatic Agency CRM sync failed for lead {}: {}",
                    event.leadId(),
                    ex.getMessage()
            );
        }
    }
}