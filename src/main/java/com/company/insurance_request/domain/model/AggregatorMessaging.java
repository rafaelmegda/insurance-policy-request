package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.model.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class AggregatorMessaging implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID policyId;
    private final EventType eventType;
    private final String status;

    public static AggregatorMessaging from(PaymentTopicEvent event) {
        return new AggregatorMessaging(event.policyId(), EventType.PAYMENT,
                event.status() != null ? event.status().name() : null);
    }

    public static AggregatorMessaging from(SubscriptionTopicEvent event) {
        return new AggregatorMessaging(event.policyId(), EventType.SUBSCRIPTION,
                event.status() != null ? event.status().name() : null);
    }
}


