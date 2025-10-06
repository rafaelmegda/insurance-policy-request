package com.company.insurance_request.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PolicyTestFactory {
    public static Policy createPolicy(UUID customerId) {
        return new Policy(
                UUID.randomUUID(),
                customerId,
                1L,
                null,
                null,
                List.of("24H_TOWING"),
                BigDecimal.valueOf(250.75),
                BigDecimal.valueOf(50000.0),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
