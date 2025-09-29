package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Police {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("category")
    private Category category;

    @JsonProperty("coverages")
    private List<Coverage> coverages;

    @JsonProperty("assistances")
    private List<String> assistances;

    @JsonProperty("total_monthly_premium_amount")
    private Double totalMonthlyPremiumAmount;

    @JsonProperty("insured_amount")
    private Double insuredAmount;

    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @JsonProperty("sales_channel")
    private SalesChannel salesChannel;

    @JsonProperty("history")
    private History history;

    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
}
