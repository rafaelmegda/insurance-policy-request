package com.company.insurance_request.domain.port.output;


import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Fraud;

public interface FraudPort {
    Fraud validate(OrderTopicEvent event);
}
