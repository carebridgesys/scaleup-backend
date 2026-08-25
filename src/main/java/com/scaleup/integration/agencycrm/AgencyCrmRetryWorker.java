package com.scaleup.integration.agencycrm;

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
public class AgencyCrmRetryWorker {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AgencyCrmRetryWorker.class
            );

    private static final int MAX_ATTEMPTS = 8;

    private static final int BATCH_SIZE = 25;

    private final LeadCrmSyncRepository
            leadCrmSyncRepository;

    private final AgencyCrmSyncService
            agencyCrmSyncService;

    public AgencyCrmRetryWorker(
            LeadCrmSyncRepository leadCrmSyncRepository,
            AgencyCrmSyncService agencyCrmSyncService
    ) {

        this.leadCrmSyncRepository =
                leadCrmSyncRepository;

        this.agencyCrmSyncService =
                agencyCrmSyncService;
    }

    @Scheduled(
            fixedDelayString =
                    "${crm.agency.retry.worker-delay-ms:60000}"
    )
    public void retryFailedAgencyCrmSyncs() {

        LocalDateTime now =
                LocalDateTime.now();

        List<UUID> leadIds =
                leadCrmSyncRepository
                        .findRetryEligibleLeadIds(
                                CrmDestination.AGENCY_CRM,
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
                "Found {} Agency CRM sync(s) eligible for retry.",
                leadIds.size()
        );

        for (UUID leadId : leadIds) {

            try {

                log.info(
                        "Retrying Agency CRM sync for lead {}",
                        leadId
                );

                agencyCrmSyncService
                        .syncLead(
                                leadId
                        );

            } catch (Exception exception) {

                /*
                 * AgencyCrmSyncService already records
                 * the failure and next retry time.
                 *
                 * Do not let one failed lead stop the
                 * remainder of this retry batch.
                 */
                log.error(
                        "Agency CRM retry worker could not process lead {}: {}",
                        leadId,
                        exception.getMessage()
                );
            }
        }
    }
}