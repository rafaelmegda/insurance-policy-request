package com.company.insurance_request.infrastructure.adapter.input.dto;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PolicyRequest(
        
    @JsonProperty("customer_id") UUID customerId,
    @JsonProperty("product_id") Long productId,
    @JsonProperty("category") Category category,
    @JsonProperty("coverages") List<Coverage> coverages,
    @JsonProperty("assistances") List<String> assistances,
    @JsonProperty("total_monthly_premium_amount") BigDecimal totalMonthlyPremiumAmount,
    @JsonProperty("insured_amount") BigDecimal insuredAmount,
    @JsonProperty("payment_method") PaymentMethod paymentMethod,
    @JsonProperty("sales_channel") SalesChannel salesChannel
){
    public Policy toDomain(){
        return Policy.builder()
                .customerId(this.customerId)
                .productId(this.productId)
                .category(this.category)
                .coverages(this.coverages)
                .assistances(this.assistances)
                .totalMonthlyPremiumAmount(this.totalMonthlyPremiumAmount)
                .insuredAmount(this.insuredAmount)
                .paymentMethod(this.paymentMethod)
                .salesChannel(this.salesChannel)
                .build();
    }
}
