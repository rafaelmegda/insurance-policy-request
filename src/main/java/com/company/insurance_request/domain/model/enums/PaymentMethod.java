package com.company.insurance_request.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    BOLETO,
    PIX;

    @JsonCreator
    public static PaymentMethod fromString(String key) {
        return key == null ? null : PaymentMethod.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
