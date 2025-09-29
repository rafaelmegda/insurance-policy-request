package com.company.insurance_request.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    VIDA,
    AUTO,
    RESIDENCIAL,
    VIAGEM,
    EMPRESARIAL;

    @JsonCreator
    public static Category fromValue(String value) {
        return value == null ? null : Category.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
