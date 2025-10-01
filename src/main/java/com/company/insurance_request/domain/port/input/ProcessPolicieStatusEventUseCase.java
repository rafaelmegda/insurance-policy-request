package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.event.PolicieStatusEvent;

public interface ProcessPolicieStatusEventUseCase {
    void process(PolicieStatusEvent event);
}
