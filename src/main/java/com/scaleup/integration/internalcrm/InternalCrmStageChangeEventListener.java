package com.scaleup.integration.internalcrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InternalCrmStageChangeEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    InternalCrmStageChangeEventListener.class
            );

    private final InternalCrmOpportunityStageResolver
            stageResolver;

    private final HighLevelInternalCrmOpportunityStageService
            stageService;

    public InternalCrmStageChangeEventListener(
            InternalCrmOpportunityStageResolver stageResolver,
            HighLevelInternalCrmOpportunityStageService stageService
    ) {

        this.stageResolver =
                stageResolver;

        this.stageService =
                stageService;
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(
            InternalCrmStageRefreshRequestedEvent event
    ) {

        try {

            stageResolver
                    .resolveCurrentStage(
                            event.leadId()
                    )
                    .ifPresent(stage -> {

                        stageService.moveToStage(
                                event.leadId(),
                                stage
                        );

                        log.info(
                                "Internal CRM opportunity stage refreshed. leadId={}, stage={}",
                                event.leadId(),
                                stage
                        );
                    });

        } catch (Exception exception) {

            log.error(
                    "Internal CRM opportunity stage refresh failed. leadId={}, error={}",
                    event.leadId(),
                    exception.getMessage()
            );
        }
    }
}