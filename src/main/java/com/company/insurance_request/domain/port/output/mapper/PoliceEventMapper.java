package com.company.insurance_request.domain.port.output.mapper;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.model.Policy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PoliceEventMapper implements PoliceEventMapperPort{
    
    @Override
    public PolicieStatusEvent toStatusEvent(Policy policie) {
        return new PolicieStatusEvent(
                policie.getId(),
                policie.getCustomerId(),
                policie.getProductId(),
                policie.getCategory(),
                policie.getCoverages(),
                policie.getAssistances(),
                policie.getTotalMonthlyPremiumAmount(),
                policie.getInsuredAmount(),
                policie.getPaymentMethod(),
                policie.getSalesChannel(),
                policie.getStatus(),
                policie.getCreatedAt(),
                policie.getFinishedAt(),
                LocalDateTime.now()
        );
    }
}
