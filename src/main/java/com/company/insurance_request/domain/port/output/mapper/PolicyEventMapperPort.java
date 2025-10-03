package com.company.insurance_request.domain.port.output.mapper;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Policy;

public interface PolicyEventMapperPort {
    OrderTopicEvent toStatusEvent(Policy policie);
}
