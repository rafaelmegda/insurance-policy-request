package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.port.input.SubscriptionTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscriptionTopicListner {

    private final SubscriptionTopicUseCase subscriptionTopicUseCase;

    public SubscriptionTopicListner(SubscriptionTopicUseCase subscriptionTopicUseCase) {
        this.subscriptionTopicUseCase = subscriptionTopicUseCase;
    }

    // TODO DEFINIR A FILA
    @RabbitListener(queues = "${messaging.queue.policy-status:insurance.policy.status.q}" )
    public void onMessage(SubscriptionTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Received message with routing key {}: message: {}", routingKey, event);
            subscriptionTopicUseCase.processMessageSubscription(event);
        }catch (Exception e){
            log.error("Error listener message to policy_id: {} - status: {} - error: {}", event.policieId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
