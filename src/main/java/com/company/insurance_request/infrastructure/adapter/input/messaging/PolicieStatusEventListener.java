package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.port.input.ProcessPolicieStatusEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PolicieStatusEventListener {

    private final ProcessPolicieStatusEventUseCase processPolicieStatusEventUseCase;

    public PolicieStatusEventListener(ProcessPolicieStatusEventUseCase processPolicieStatusEventUseCase) {
        this.processPolicieStatusEventUseCase = processPolicieStatusEventUseCase;
    }

    @RabbitListener(queues = "${messaging.queue.policy-status:insurance.policy.status.q}" )
    public void onMessage(PolicieStatusEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Received message with routing key {}: {}", routingKey, event);
        processPolicieStatusEventUseCase.process(event);
    }
}
