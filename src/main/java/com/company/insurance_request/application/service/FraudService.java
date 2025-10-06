package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Fraud;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.FraudUseCase;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.FraudPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import com.company.insurance_request.domain.FraudResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService implements FraudUseCase {

    private final FraudPort fraudPort;
    private final PolicyService policyService;
    private final OrderTopicPublisherPort publisher;
    private final PolicyEventMapper policyEventMapper;
    private final FraudResult fraudResult;

    @Override
    public void processFraud(OrderTopicEvent event) throws JsonProcessingException {

        if(event.status() == Status.RECEIVED){

            Fraud fraud = fraudPort.validate(event);
            log.info("Fraud classification {} for policy {}", fraud.getClassification(), event.policyId());

            if (fraud.getClassification() == null) {
                log.warn("Fraud validation failed or returned null for policy: {}", event.policyId());
                return;
            }

            boolean approved = fraudResult.isValidated(
                    fraud.getClassification(),
                    event.category(),
                    event.insuredAmount()
            );

            if (approved) {
                log.info("Policy {} approved after fraud validation", event.policyId());
                policyService.updateStatus(event.policyId(), Status.VALIDATED);

                publisher.publishReceived(event, Status.VALIDATED.toValue());

                policyService.updateStatus(event.policyId(), Status.PENDING);
                log.info("Policy {} in status {} awaiting payment and subscription analysis", Status.PENDING, event.policyId());

            } else {
                log.info("Policy {} rejected due to fraud rules, updating status to {}", event.policyId(), Status.REJECTED);
                policyService.updateStatus(event.policyId(), Status.REJECTED);
            }
        }
    }
}
