package com.company.insurance_request.domain.port.output.mapper;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class PolicyEventMapper implements PolicyEventMapperPort {
    
    @Override
    public OrderTopicEvent toStatusEvent(Policy policy) {
        return new OrderTopicEvent(
                policy.getPolicyId(),
                policy.getCustomerId(),
                policy.getProductId(),
                policy.getCategory(),
                policy.getInsuredAmount(),
                policy.getStatus(),
                LocalDateTime.now()
        );
    }
}
