package com.company.insurance_request.domain.port.output;


import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.model.ValidateFraud;

public interface ValidateFraudPort {
    ValidateFraud validate(PolicieStatusEvent event);
}
