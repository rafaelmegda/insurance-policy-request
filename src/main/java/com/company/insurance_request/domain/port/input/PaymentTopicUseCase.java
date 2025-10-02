package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface PaymentTopicUseCase {
    void processMessagePayment(PaymentTopicEvent paymentTopicEvent) throws JsonProcessingException;
}
