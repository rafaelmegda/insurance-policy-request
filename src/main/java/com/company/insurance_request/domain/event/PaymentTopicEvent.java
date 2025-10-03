package com.company.insurance_request.domain.event;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentTopicEvent(
        @JsonProperty("policie_id") Long policieId,
        @JsonProperty("customer_id") UUID customerId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("category") Category category,
        @JsonProperty("coverages") List<Coverage> coverages,
        @JsonProperty("assistances") List<String> assistances,
        @JsonProperty("total_monthly_premium_amount") BigDecimal totalMonthlyPremiumAmount,
        @JsonProperty("insured_amount") BigDecimal insuredAmount,
        @JsonProperty("payment_method") PaymentMethod paymentMethod,
        @JsonProperty("sales_channel") SalesChannel salesChannel,
        @JsonProperty("status") Status status,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("finished_at") LocalDateTime finishedAt,
        @JsonProperty("timestamp") LocalDateTime timestamp
){

}
