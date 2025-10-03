package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.Fraud;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.FraudUseCase;
import com.company.insurance_request.domain.port.output.OrderTopicBrokerPort;
import com.company.insurance_request.domain.port.output.FraudPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import com.company.insurance_request.domain.service.FraudDomainService;
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
    private final OrderTopicBrokerPort publiser;
    private final PolicyEventMapper policyEventMapper;
    private final FraudDomainService fraudDomainService;

    @Override
    public void processFraud(OrderTopicEvent event) throws JsonProcessingException {
        log.info("Iniciando validacaoo de fraude para a apolice: {}", event.policieId());

        if(event.status() == Status.RECEIVED){
            Fraud fraud = fraudPort.validate(event);
            log.info("Response Validated fraud: {}", fraud);

            if (fraud == null || fraud.getClassification() == null) {
                log.warn("Fraud response inválida para apólice {}, marcando para reprocessamento (RECEIVED).", event.policieId());

                // TODO: AVALIAR RESILIENCIA CASO FALHE A CHAMADA DA API DE FRAUD
                return;
            }

            boolean approved = fraudDomainService.isValidated(
                    fraud.getClassification(),
                    event.category(),
                    event.insuredAmount()
            );

            if (approved) {
                Policy policy = policyService.updateStatus(event.policieId(), Status.VALIDATED.toValue());
                publiser.publish(policyEventMapper.toStatusEvent(policy), Status.PENDING.toValue());
                log.info("Approved policy: {} awaiting payment and subscription analysis",event.policieId());

            } else {
                Policy policy = policyService.updateStatus(event.policieId(), Status.REJECTED.toValue());
                log.info("Policy {} rejected due to fraud rules", event.policieId());
            }
        }
    }
}
