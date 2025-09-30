package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Police {

    // TODO não vem no JSON inicial, avaliar se deve ficar
    // TODO - AVALIAR SUBSTITUIR OS ENUMS POR OBJETOS PARA CRIAR TABELAS
    @JsonProperty("id")
    private Long id;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("category")
    private Category category;

    @JsonProperty("coverages")
    private List<Coverage> coverages;

    @JsonProperty("assistances")
    private List<String> assistances;

    @JsonProperty("total_monthly_premium_amount")
    private BigDecimal totalMonthlyPremiumAmount;

    @JsonProperty("insured_amount")
    private BigDecimal insuredAmount;

    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @JsonProperty("sales_channel")
    private SalesChannel salesChannel;

    @JsonProperty("history")
    private History history;

    @JsonProperty("status")
    private Status status;

    // TODO não vem no JSON inicial, avaliar se deve ficar
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // TODO não vem no JSON inicial, avaliar se deve ficar
    @JsonProperty("finished_at")
    private LocalDateTime finishedAt;
}
