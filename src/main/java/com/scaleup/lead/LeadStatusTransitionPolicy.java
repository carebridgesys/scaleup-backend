package com.scaleup.lead;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class LeadStatusTransitionPolicy {

    private final Map<LeadStatus, Set<LeadStatus>>
            allowedTransitions;

    public LeadStatusTransitionPolicy() {

        Map<LeadStatus, Set<LeadStatus>> transitions =
                new EnumMap<>(LeadStatus.class);

        transitions.put(
                LeadStatus.NEW,
                EnumSet.of(
                        LeadStatus.CONTACT_ATTEMPTED,
                        LeadStatus.CONTACTED,
                        LeadStatus.QUALIFIED,
                        LeadStatus.NOT_QUALIFIED,
                        LeadStatus.LOST
                )
        );

        transitions.put(
                LeadStatus.CONTACT_ATTEMPTED,
                EnumSet.of(
                        LeadStatus.CONTACTED,
                        LeadStatus.FOLLOW_UP_NEEDED,
                        LeadStatus.NOT_QUALIFIED,
                        LeadStatus.LOST
                )
        );

        transitions.put(
                LeadStatus.CONTACTED,
                EnumSet.of(
                        LeadStatus.QUALIFIED,
                        LeadStatus.FOLLOW_UP_NEEDED,
                        LeadStatus.NOT_QUALIFIED,
                        LeadStatus.LOST
                )
        );

        transitions.put(
                LeadStatus.FOLLOW_UP_NEEDED,
                EnumSet.of(
                        LeadStatus.CONTACT_ATTEMPTED,
                        LeadStatus.CONTACTED,
                        LeadStatus.QUALIFIED,
                        LeadStatus.NOT_QUALIFIED,
                        LeadStatus.LOST
                )
        );

        transitions.put(
                LeadStatus.QUALIFIED,
                EnumSet.of(
                        LeadStatus.CONVERTED,
                        LeadStatus.FOLLOW_UP_NEEDED,
                        LeadStatus.NOT_QUALIFIED,
                        LeadStatus.LOST
                )
        );

        transitions.put(
                LeadStatus.CONVERTED,
                EnumSet.noneOf(LeadStatus.class)
        );

        transitions.put(
                LeadStatus.NOT_QUALIFIED,
                EnumSet.noneOf(LeadStatus.class)
        );

        transitions.put(
                LeadStatus.LOST,
                EnumSet.noneOf(LeadStatus.class)
        );

        this.allowedTransitions =
                Map.copyOf(transitions);
    }

    public boolean isAllowed(
            LeadStatus currentStatus,
            LeadStatus targetStatus
    ) {

        if (currentStatus == null || targetStatus == null) {
            return false;
        }

        if (currentStatus == targetStatus) {
            return true;
        }

        return allowedTransitions
                .getOrDefault(
                        currentStatus,
                        Set.of()
                )
                .contains(targetStatus);
    }
}