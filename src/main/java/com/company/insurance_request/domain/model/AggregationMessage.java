package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.model.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class AggregationMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID policyId;
    private final EventType eventType;
    private final String status;

    public AggregationMessage(UUID policyId, EventType eventType, String status) {
        this.policyId = policyId;
        this.eventType = eventType;
        this.status = status;
    }

    public UUID getPolicyId() { return policyId; }
    public EventType getEventType() { return eventType; }
    public String getStatus() { return status; }

    public static AggregationMessage from(PaymentTopicEvent event) {
        return new AggregationMessage(
                event.policyId(),
                EventType.PAYMENT,
                event.status() != null ? event.status().name() : null
        );
    }

    public static AggregationMessage from(SubscriptionTopicEvent event) {
        return new AggregationMessage(
                event.policyId(),
                EventType.SUBSCRIPTION,
                event.status() != null ? event.status().name() : null
        );
    }
}


