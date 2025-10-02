package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface SubscriptionTopicUseCase {
    void processMessageSubscription(SubscriptionTopicEvent subscriptionTopicEvent) throws JsonProcessingException;
}
