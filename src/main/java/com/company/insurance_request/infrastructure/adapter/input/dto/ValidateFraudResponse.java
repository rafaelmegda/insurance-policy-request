package com.company.insurance_request.infrastructure.adapter.input.dto;

import com.company.insurance_request.domain.model.Occurrence;
import com.company.insurance_request.domain.model.enums.Classification;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValidateFraudResponse (

        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("analyzedAt") LocalDateTime analyzedAt,
        @JsonProperty("classification") Classification classification,
        @JsonProperty("occurrences") List<Occurrence> occurrences
){

}
