package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.Classification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ValidateFraud {

    @JsonProperty("order_id")
    private UUID orderId;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("analyzedAt")
    private LocalDateTime analyzedAt;

    @JsonProperty("classification")
    private Classification classification;

    @JsonProperty("occurrences")
    private Occurrence occurrences;
}
