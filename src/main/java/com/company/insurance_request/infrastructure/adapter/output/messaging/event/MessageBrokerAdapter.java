package com.company.insurance_request.infrastructure.adapter.output.messaging.event;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.port.output.MessageBrokerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageBrokerAdapter implements MessageBrokerPort {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;
    private final ObjectMapper objectMapper;

    public MessageBrokerAdapter(RabbitTemplate rabbitTemplate, TopicExchange topicExchange, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(PolicieStatusEvent event, String routingKey) {
        try{
            byte[] body = objectMapper.writeValueAsBytes(event);
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbitTemplate.send(topicExchange.getName(), routingKey, new Message(body, props));
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }


    }
}
