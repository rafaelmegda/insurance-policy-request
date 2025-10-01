package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.model.ValidateFraud;
import com.company.insurance_request.domain.port.input.ProcessPolicieStatusEventUseCase;
import com.company.insurance_request.domain.port.output.ValidateFraudPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessPolicieStatusEventService implements ProcessPolicieStatusEventUseCase {

    private final ValidateFraudPort validateFraudPort;

    public ProcessPolicieStatusEventService(ValidateFraudPort validateFraudPort) {
        this.validateFraudPort = validateFraudPort;
    }

    @Override
    public void process(PolicieStatusEvent event) {
        log.info("Iniciando validação de fraude para a apólice: {}", event.policieId());

        ValidateFraud response = validateFraudPort.validate(event);
        log.info("response: {}", response);

    }
}
