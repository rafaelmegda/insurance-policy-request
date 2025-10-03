package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.port.input.SubscriptionTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscriptionTopicListener {

    private final SubscriptionTopicUseCase subscriptionTopicUseCase;

    public SubscriptionTopicListener(@Qualifier("subscriptionTopicService") SubscriptionTopicUseCase subscriptionTopicUseCase) {
        this.subscriptionTopicUseCase = subscriptionTopicUseCase;
    }

    @RabbitListener(queues = "${messaging.queue.subscription}" )
    public void onMessage(SubscriptionTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Subscription received policyId: {} - routing : {} - event: {}", event.policyId(), routingKey, event);
            subscriptionTopicUseCase.processMessageSubscription(event);
        }catch (Exception e){
            log.error("Error listener message of queue subscription to policy_id: {} - status: {} - error: {}", event.policyId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
