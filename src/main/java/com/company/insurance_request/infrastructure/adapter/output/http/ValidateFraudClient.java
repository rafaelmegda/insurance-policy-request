package com.company.insurance_request.infrastructure.adapter.output.http;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.ValidateFraud;
import com.company.insurance_request.domain.port.output.ValidateFraudPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ValidateFraudClient implements ValidateFraudPort {

    @Value("${fraud.api.base-url}")
    private String baseUrl;

    @Value("${fraud.api.path}")
    private String path;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ValidateFraud validate(OrderTopicEvent event) {
        try{
            ValidateFraud response = null;
             response = restTemplate.getForObject(
                    baseUrl + path + event.customerId(),
                    ValidateFraud.class
            );
            return response;
        }catch (Exception ex){
            log.error("Erro ao chamar API de fraude", ex);
            return null;
        }
    }
}
