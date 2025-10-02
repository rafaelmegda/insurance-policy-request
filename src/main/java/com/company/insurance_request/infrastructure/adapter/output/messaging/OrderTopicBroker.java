package com.company.insurance_request.infrastructure.adapter.output.messaging;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.port.output.OrderTopicBrokerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTopicBroker implements OrderTopicBrokerPort {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;
    private final ObjectMapper objectMapper;

    public OrderTopicBroker(RabbitTemplate rabbitTemplate, TopicExchange topicExchange, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderTopicEvent event, String routingKey) throws JsonProcessingException {
        try{
            byte[] body = objectMapper.writeValueAsBytes(event);
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbitTemplate.send(topicExchange.getName(), routingKey, new Message(body, props));
        }catch (Exception e){
            log.error("Error publish message to policy_id : {} - {}", event.policieId(), e.getMessage());
            throw e;
        }
    }
}
