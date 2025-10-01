package com.company.insurance_request.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    RECEIVED,
    VALIDADO,
    PENDENTE,
    REJEITADO,
    APROVADO,
    CANCELADO;

    @JsonCreator
    public static Status fromValue(String value) {
        return value == null ? null : Status.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
