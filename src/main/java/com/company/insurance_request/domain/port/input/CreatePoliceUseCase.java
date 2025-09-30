package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;

public interface CreatePoliceUseCase {
    Police create(CreatePoliceRequest createPoliceRequest);
}
