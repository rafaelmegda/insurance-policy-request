package com.company.insurance_request.domain.model.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OccurrencesType {
    FRAUD,
    SUSPICION;

    @JsonCreator
    public static OccurrencesType fromString(String key) {
        return key == null ? null : OccurrencesType.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
