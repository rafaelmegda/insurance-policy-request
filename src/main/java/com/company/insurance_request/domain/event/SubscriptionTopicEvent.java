package com.company.insurance_request.domain.event;

import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionTopicEvent(
        @JsonProperty("policy_id") UUID policyId,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("category") Category category,
        @JsonProperty("insured_amount") BigDecimal insuredAmount,
        @JsonProperty("status") Status status,
        @JsonProperty("timestamp") LocalDateTime timestamp
){

}
