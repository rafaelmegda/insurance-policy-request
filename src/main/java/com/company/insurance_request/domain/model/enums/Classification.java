package com.company.insurance_request.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Classification {
    REGULAR,
    HIGH_RISK,
    PREFERENTIAL,
    NO_INFORMATION;

    @JsonCreator
    public static Classification fromString(String key) {
        return key == null ? null : Classification.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
