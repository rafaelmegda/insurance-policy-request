package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.application.service.PolicyAggregationService;
import com.company.insurance_request.domain.AggregationResult;
import com.company.insurance_request.domain.model.AggregationMessage;
import com.company.insurance_request.domain.model.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.jdbc.store.JdbcMessageStore;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.sql.DataSource;
import java.awt.geom.Area;
import java.util.UUID;

@Slf4j
@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class AggregatorIntegrationConfig {

    @Bean(name = "paymentTopicChannel")
    public MessageChannel paymentChannel() {
        return new DirectChannel();
    }

    @Bean(name = "subscriptionTopicChannel")
    public MessageChannel subscriptionChannel() {
        return new DirectChannel();
    }

    @Bean(name = "aggregatorInput")
    public MessageChannel aggregatorInput() {
        return new DirectChannel();
    }

    @Bean(name = "aggregatedOutput")
    public MessageChannel aggregatedOutput() {
        return new DirectChannel();
    }

    @Bean
    public MessageGroupStore messageGroupStore(DataSource ds) {
        return new JdbcMessageStore(ds);
    }

    @Bean
    @ServiceActivator(inputChannel = "paymentTopicChannel")
    public MessageHandler paymentToAggregatorBridge() {
        return message -> {
            try {
                AggregationMessage payload = (AggregationMessage) message.getPayload();
                log.info("Message received in topic payment policy id: {} with status: {} to aggregator", payload.getPolicyId(), payload.getStatus());
                boolean sent = aggregatorInput().send(message);
                log.info("Message of policy id {} sent to aggregator payment: {}", payload.getPolicyId(), sent);
            }catch (Exception ex) {
                log.error("Error sending message to aggregator payment: {}", ex.getMessage());
            }
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "subscriptionTopicChannel")
    public MessageHandler subscriptionToAggregatorBridge() {
        return message -> {
            try {
                AggregationMessage payload = (AggregationMessage) message.getPayload();
                log.info("Message received in topic subscription policy id: {} with status: {} to aggregator", payload.getPolicyId(), payload.getStatus());
                boolean sent = aggregatorInput().send(message);
                log.info("Message of policy id {} sent to aggregator subscription: {}", payload.getPolicyId(), sent);
            } catch (Exception ex) {
                log.error("Error sending message to aggregator subscription: {}", ex.getMessage());
            }
        };
    }

    @Bean
    public AggregatingMessageHandler aggregatorHandler(MessageGroupStore messageGroupStore) {
        AggregatingMessageHandler handler = getAggregatingMessageHandler();

        handler.setCorrelationStrategy(message -> {
            AggregationMessage payload = (AggregationMessage) ((Message<?>) message).getPayload();
            return payload.getPolicyId();
        });

        handler.setReleaseStrategy(this::releaseWhenCompleteOrRejected);
        handler.setMessageStore(messageGroupStore);
        handler.setExpireGroupsUponTimeout(true);
        handler.setGroupTimeoutExpression(new ValueExpression<>(900_000L)); //15 min
        handler.setSendPartialResultOnExpiry(false);
        handler.setOutputChannel(aggregatedOutput());

        return handler;
    }

    private AggregatingMessageHandler getAggregatingMessageHandler() {

        MessageGroupProcessor processor = group -> {
            try {
                UUID policyId = (UUID) group.getGroupId();
                log.info("starting aggregation: policyId={}, groupSize={}", policyId, group.size());

                String paymentStatus = null;
                String subscriptionStatus = null;

                for (Message<?> msg : group.getMessages()) {
                    AggregationMessage p = (AggregationMessage) msg.getPayload();
                    if (p.getEventType() == EventType.PAYMENT) {
                        paymentStatus = p.getStatus();
                    } else if (p.getEventType() == EventType.SUBSCRIPTION) {
                        subscriptionStatus = p.getStatus();
                    }
                }

                AggregationResult result = new AggregationResult(policyId, paymentStatus, subscriptionStatus);
                log.info("Aggregation finalized: policyId={}, paymentStatus={}, subscriptionStatus={}",
                        policyId, paymentStatus, subscriptionStatus);
                return result;
            } catch (Exception e) {
                log.error("Error during aggregation", e);
                throw new IllegalStateException("Failed to aggregate message group", e);
            }
        };

        AggregatingMessageHandler handler = new AggregatingMessageHandler(processor);
        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "aggregatorInput")
    public MessageHandler aggregatorServiceActivator(AggregatingMessageHandler aggregatorHandler) {
        return aggregatorHandler;
    }

    @Bean
    @ServiceActivator(inputChannel = "aggregatedOutput")
    public MessageHandler aggregatedOutputServiceActivator(PolicyAggregationService service) {
        return message -> {
            try {
                AggregationResult result = (AggregationResult) message.getPayload();
                log.info("Processing aggregated result: policyId={}, paymentStatus={}, subscriptionStatus={}",
                        result.policyId(), result.paymentStatus(), result.subscriptionStatus());
                service.finalizeAggregation(result);
                log.info("Aggregated result processed successfully: policyId={}", result);
            } catch (Exception e) {
                log.error("Error processing aggregated result", e);
            }
        };
    }

    private boolean releaseWhenCompleteOrRejected(MessageGroup group) {

        try {
            boolean hasPayment = false, hasSubscription = false, anyRejected = false;

            for (Message<?> msg : group.getMessages()) {
                AggregationMessage p = (AggregationMessage) msg.getPayload();
                if (p.getEventType() == EventType.PAYMENT) {
                    hasPayment = true;
                    if ("REJECTED".equalsIgnoreCase(p.getStatus())) {
                        anyRejected = true;
                    }
                } else if (p.getEventType() == EventType.SUBSCRIPTION) {
                    hasSubscription = true;
                    if ("REJECTED".equalsIgnoreCase(p.getStatus())) {
                        anyRejected = true;
                    }
                }
            }
            boolean ready = anyRejected || (hasPayment && hasSubscription);
            if (ready) {
                String reason = anyRejected ? "a component was REJECTED" : "both components received";
                log.info("Releasing group reason={}, hasPayment={}, hasSubscription={}, anyRejected={}, groupSize={}",
                        reason, hasPayment, hasSubscription, anyRejected, group.size());
            } else {
                log.info("Not ready to release group: hasPayment={}, hasSubscription={}, anyRejected={}, groupSize={}",
                        hasPayment, hasSubscription, anyRejected, group.size());
            }
            return ready;
        } catch (Exception e){
            log.error("Error in release strategy", e);
            return false;
        }
    }
}
