// java
package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.AggregatorDomain;
import com.company.insurance_request.domain.model.AggregatorMessaging;
import com.company.insurance_request.domain.model.enums.EventType;
import com.company.insurance_request.domain.model.enums.Status;
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
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class AggregatorService {

    private final PolicyService policyService;

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

    // Bridges: conectam os listeners ao agregador
    @Bean
    @ServiceActivator(inputChannel = "paymentTopicChannel")
    public MessageHandler paymentToAggregatorBridge() {
        BridgeHandler bridge = new BridgeHandler();
        bridge.setOutputChannel(aggregatorInput());
        return bridge;
    }

    @Bean
    @ServiceActivator(inputChannel = "subscriptionTopicChannel")
    public MessageHandler subscriptionToAggregatorBridge() {
        BridgeHandler bridge = new BridgeHandler();
        bridge.setOutputChannel(aggregatorInput());
        return bridge;
    }

    // Aggregator handler inscrito no canal 'aggregatorInput'
    @Bean
    public AggregatingMessageHandler aggregatorHandler(MessageGroupStore messageGroupStore) {
        MessageGroupProcessor processor = AggregatorDomain::from;

        AggregatingMessageHandler handler = new AggregatingMessageHandler(processor);

        handler.setCorrelationStrategy(message -> {
            AggregatorMessaging payload = (AggregatorMessaging) ((Message<?>) message).getPayload();
            return payload.getPolicyId();
        });

        handler.setReleaseStrategy(this::releaseWhenCompleteOrRejected);
        handler.setMessageStore(messageGroupStore);
        handler.setExpireGroupsUponTimeout(true);
        handler.setGroupTimeoutExpression(new ValueExpression<>(500000L)); // 5s //TODO DEFINIR UM TEMPO MELHOR
        handler.setSendPartialResultOnExpiry(false);
        // TODO SE MANDA PARA UM CANAL DE SAIDA
        // descarta o resultado agregado para evitar exceções e reprocessamento
        handler.setOutputChannelName("nullChannel");

        return handler;
    }

    @Bean
    @ServiceActivator(inputChannel = "aggregatorInput")
    public MessageHandler aggregatorServiceActivator(AggregatingMessageHandler aggregatorHandler) {
        return aggregatorHandler;
    }

    private boolean releaseWhenCompleteOrRejected(MessageGroup group) {
        boolean hasPayment = false, hasSubscription = false, anyRejected = false;
        UUID policyId = (UUID) group.getGroupId();

        for (Message<?> msg : group.getMessages()) {
            // TODO INCLUIR OS LOGS DE CADA EVENTO APROVADO OU REJEITADO
            AggregatorMessaging p = (AggregatorMessaging) msg.getPayload();
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
        // Atualiza o status da apólice com base nos resultados agregados
        boolean ready = anyRejected || (hasPayment && hasSubscription);
        if (ready && policyId != null) {
            try{
                if (anyRejected) {
                    policyService.updateStatus(policyId, Status.REJECTED);
                } else {
                    policyService.updateStatus(policyId, Status.APPROVED);
                }
            }catch (NoSuchElementException e){
                // TODO AVALIAR O QUE FAZER COM A MENSAGEM
                log.warn("Policy {} not found when trying to update status - {}", policyId, e.getMessage());
            }catch (Exception e){
                log.error("Error updating policy {} status - {}", policyId, e.getMessage());
            }
        }
        return ready;
    }
}
