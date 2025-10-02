package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OrderTopicBrokerPort {
    void publish(OrderTopicEvent event, String routingKey) throws JsonProcessingException;
}
