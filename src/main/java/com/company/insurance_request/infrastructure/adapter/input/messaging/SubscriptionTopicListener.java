package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.model.AggregationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.MessageBuilder;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class SubscriptionTopicListener {

    private final MessageChannel subscriptionTopicChannel;

    public SubscriptionTopicListener(@Qualifier("subscriptionTopicChannel") MessageChannel subscriptionTopicChannel) {
        this.subscriptionTopicChannel = subscriptionTopicChannel;
    }

    @RabbitListener(queues = "${messaging.queue.subscription}" )
    public void onMessage(SubscriptionTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Message received status: {} in queue.subscription to policy_id: {}", event.status(), event.policyId());

            subscriptionTopicChannel.send(
                    MessageBuilder.withPayload(AggregationMessage.from(event))
                            .setHeader(AmqpHeaders.RECEIVED_ROUTING_KEY, routingKey)
                            .build()
            );
        }catch(NoSuchElementException e){
            log.warn("Policy {} not found for subscription event: {} - {}", event.policyId(), event, e.getMessage());
        }catch (Exception e){
            log.error("Error listener message of queue subscription to policy_id: {} - status: {} - error: {}", event.policyId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
