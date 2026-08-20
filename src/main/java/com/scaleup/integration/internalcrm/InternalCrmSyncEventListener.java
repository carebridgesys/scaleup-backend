package com.scaleup.integration.internalcrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InternalCrmSyncEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    InternalCrmSyncEventListener.class
            );

    private final InternalCrmSyncService
            internalCrmSyncService;

    public InternalCrmSyncEventListener(
            InternalCrmSyncService internalCrmSyncService
    ) {
        this.internalCrmSyncService =
                internalCrmSyncService;
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            InternalCrmSyncRequestedEvent event
    ) {

        try {

            log.info(
                    "Starting automatic Internal CRM sync for lead {}",
                    event.leadId()
            );

            internalCrmSyncService.syncLead(
                    event.leadId()
            );

            log.info(
                    "Automatic Internal CRM sync finished for lead {}",
                    event.leadId()
            );

        } catch (Exception ex) {

            /*
             * Do not rethrow.
             *
             * The original public lead transaction has
             * already committed successfully.
             *
             * InternalCrmSyncService is responsible for
             * marking the CRM sync record FAILED and
             * recording the retry/error information.
             */
            log.error(
                    "Automatic Internal CRM sync failed for lead {}: {}",
                    event.leadId(),
                    ex.getMessage()
            );
        }
    }
}