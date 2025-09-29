package com.company.insurance_request.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SalesChannel {
    MOBILE,
    WEB,
    WHATSAPP,
    TELEFONE;

    @JsonCreator
    public static SalesChannel fromValue(String value) {
        return value == null ? null : SalesChannel.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
