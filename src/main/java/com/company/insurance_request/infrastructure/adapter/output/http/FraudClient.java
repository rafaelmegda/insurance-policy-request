package com.company.insurance_request.infrastructure.adapter.output.http;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Fraud;
import com.company.insurance_request.domain.port.output.FraudPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class FraudClient implements FraudPort {

    @Value("${fraud.api.base-url}")
    private String baseUrl;

    @Value("${fraud.api.path}")
    private String path;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Fraud validate(OrderTopicEvent event) {
        try{
            log.info("Calling Fraud API for customerId: {}", event.customerId());
            Fraud response = null;
             response = restTemplate.getForObject(
                    baseUrl + path + event.customerId(),
                    Fraud.class
            );
            return response;
        }catch (Exception ex){
            log.error("Error calling Fraud API for customerId: {} - {}", event.customerId(), ex.getMessage());
            return null;
        }
    }
}
