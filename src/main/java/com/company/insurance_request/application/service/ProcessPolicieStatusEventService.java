package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.port.input.ProcessPolicieStatusEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessPolicieStatusEventService implements ProcessPolicieStatusEventUseCase {

    @Override
    public void process(PolicieStatusEvent event) {
        log.info("Iniciando validação de fraude para a apólice: {}", event.policieId());
        //Todo lógica de validação a partir do retorno da API de fraudes
    }


}
