package com.company.insurance_request.domain.port.output.mapper;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.model.Police;

public interface PoliceEventMapperPort {
    PolicieStatusEvent toStatusEvent(Police policie);
}
