package com.company.insurance_request.infrastructure.adapter.input.mapper;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyResponse;

public final class PolicyResponseMapper {

    public static PolicyResponse toResponse(Policy policy){
        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getCustomerId(),
                policy.getProductId(),
                policy.getCategory(),
                policy.getSalesChannel(),
                policy.getPaymentMethod(),
                policy.getStatus(),
                policy.getCreatedAt(),
                policy.getFinishedAt(),
                policy.getTotalMonthlyPremiumAmount(),
                policy.getInsuredAmount(),
                policy.getCoverages(),
                policy.getAssistances(),
                policy.getHistory()
        );
    }
}
