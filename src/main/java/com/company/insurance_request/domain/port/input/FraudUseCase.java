package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface FraudUseCase {
    void processFraud(OrderTopicEvent event) throws JsonProcessingException;
}
