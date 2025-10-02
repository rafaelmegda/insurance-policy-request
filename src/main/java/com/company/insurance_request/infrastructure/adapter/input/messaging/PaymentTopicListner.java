package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.port.input.PaymentTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentTopicListner {

    private final PaymentTopicUseCase paymentTopicUseCase;

    public PaymentTopicListner(PaymentTopicUseCase paymentTopicUseCase) {
        this.paymentTopicUseCase = paymentTopicUseCase;
    }

    // TODO DEFINIR A FILA
    @RabbitListener(queues = "${messaging.queue.policy-status:insurance.policy.status.q}" )
    public void onMessage(PaymentTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Received message with routing key {}: message: {}", routingKey, event);
            paymentTopicUseCase.processMessagePayment(event);
        }catch (Exception e){
            log.error("Error listener message to policy_id: {} - status: {} - error: {}", event.policieId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
