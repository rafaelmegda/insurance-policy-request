package com.company.insurance_request.domain;

import com.company.insurance_request.domain.model.AggregatorMessaging;
import com.company.insurance_request.domain.model.enums.EventType;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;

import java.util.UUID;

public record AggregatorDomain(
        UUID policyId,
        String paymentStatus,
        String subscriptionStatus,
        boolean anyRejected,
        boolean completed
) {
    public static AggregatorDomain from(MessageGroup group) {
        UUID id = null;
        String pay = null;
        String sub = null;
        boolean rejected = false;

        for (Message<?> msg : group.getMessages()) {
            AggregatorMessaging p = (AggregatorMessaging) msg.getPayload();
            if (id == null) id = p.getPolicyId();

            if (p.getEventType() == EventType.PAYMENT) {
                pay = p.getStatus();
                if ("REJECTED".equalsIgnoreCase(pay)) rejected = true;
            } else if (p.getEventType() == EventType.SUBSCRIPTION) {
                sub = p.getStatus();
                if ("REJECTED".equalsIgnoreCase(sub)) rejected = true;
            }
        }
        boolean hasBoth = pay != null && sub != null;
        return new AggregatorDomain(id, pay, sub, rejected, rejected || hasBoth);
    }
}
