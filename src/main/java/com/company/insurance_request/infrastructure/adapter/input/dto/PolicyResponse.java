package com.company.insurance_request.infrastructure.adapter.input.dto;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.History;
import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PolicyResponse(

        @JsonProperty("policy_id") UUID id,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("product_id")Long productId,
        @JsonProperty("category") Category category,
        @JsonProperty("sales_channel") SalesChannel salesChannel,
        @JsonProperty("payment_method") PaymentMethod paymentMethod,
        @JsonProperty("status") Status status,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("finished_at") LocalDateTime finishedAt,
        @JsonProperty("total_monthly_premium_amount") BigDecimal totalMonthlyPremiumAmount,
        @JsonProperty("insured_amount") BigDecimal insuredAmount,
        @JsonProperty("coverages") List<Coverage> coverages,
        @JsonProperty("assistances") List<String> assistances,
        @JsonProperty("history") List<History> history
){
    
}



