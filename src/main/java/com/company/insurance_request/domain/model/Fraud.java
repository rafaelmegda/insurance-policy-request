package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.Classification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class Fraud {

    @JsonProperty("policy_id")
    private UUID policyId;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("analyzedAt")
    private LocalDateTime analyzedAt;

    @JsonProperty("classification")
    private Classification classification;

    @JsonProperty("occurrences")
    private List<Occurrence> occurrences;
}
