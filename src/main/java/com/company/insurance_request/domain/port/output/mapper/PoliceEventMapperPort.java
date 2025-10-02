package com.company.insurance_request.domain.port.output.mapper;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.model.Policy;

public interface PoliceEventMapperPort {
    PolicieStatusEvent toStatusEvent(Policy policie);
}
