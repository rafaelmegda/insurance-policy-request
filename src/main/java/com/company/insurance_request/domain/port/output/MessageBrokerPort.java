package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.event.PolicieStatusEvent;

public interface MessageBrokerPort {
    void publish(PolicieStatusEvent event, String routingKey);
}
