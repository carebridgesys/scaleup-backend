package com.scaleup.integration.internalcrm;

import com.scaleup.integration.CrmDestination;
import com.scaleup.integration.CrmSyncStatus;
import com.scaleup.integration.LeadCrmSyncRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class InternalCrmRetryWorker {

    private static final Logger log =
            LoggerFactory.getLogger(
                    InternalCrmRetryWorker.class
            );

    private static final int MAX_ATTEMPTS = 8;

    private static final int BATCH_SIZE = 25;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final InternalCrmSyncService
            internalCrmSyncService;

    public InternalCrmRetryWorker(
            LeadCrmSyncRepository leadCrmSyncRepository,
            InternalCrmSyncService internalCrmSyncService
    ) {
        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.internalCrmSyncService =
                internalCrmSyncService;
    }

    @Scheduled(
            fixedDelayString =
                    "${crm.internal.retry.worker-delay-ms:60000}"
    )
    public void retryFailedInternalCrmSyncs() {

        LocalDateTime now =
                LocalDateTime.now();

        List<UUID> leadIds =
                leadCrmSyncRepository
                        .findRetryEligibleLeadIds(
                                CrmDestination.INTERNAL_CRM,
                                CrmSyncStatus.FAILED,
                                now,
                                MAX_ATTEMPTS,
                                PageRequest.of(
                                        0,
                                        BATCH_SIZE
                                )
                        );

        if (leadIds.isEmpty()) {
            return;
        }

        log.info(
                "Found {} Internal CRM sync(s) eligible for retry.",
                leadIds.size()
        );

        for (UUID leadId : leadIds) {

            try {

                log.info(
                        "Retrying Internal CRM sync for lead {}",
                        leadId
                );

                internalCrmSyncService
                        .syncLead(leadId);

            } catch (Exception exception) {

                /*
                 * One bad lead must not stop other
                 * eligible leads in this batch.
                 */
                log.error(
                        "Internal CRM retry worker could not process lead {}: {}",
                        leadId,
                        exception.getMessage()
                );
            }
        }
    }
}