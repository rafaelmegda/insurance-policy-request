package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.model.AggregationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class PaymentTopicListener {

    private final MessageChannel paymentTopicChannel;

    public PaymentTopicListener(@Qualifier("paymentTopicChannel") MessageChannel paymentTopicChannel) {
        this.paymentTopicChannel = paymentTopicChannel;
    }

    @RabbitListener(queues = "${messaging.queue.payment}" )
    public void onMessage(PaymentTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Message received status: {} in queue.payment to policy_id: {}", event.status(), event.policyId());
            paymentTopicChannel.send(
                    MessageBuilder.withPayload(AggregationMessage.from(event))
                            .setHeader(AmqpHeaders.RECEIVED_ROUTING_KEY, routingKey)
                            .build()
            );
        } catch(NoSuchElementException e){
            log.warn("Policy {} not found for payment event: {} - {}", event.policyId(), event, e.getMessage());
        }
        catch (Exception e){
            log.error("Error listener message queue.payment to policy_id: {} - status: {} - error: {}", event.policyId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
