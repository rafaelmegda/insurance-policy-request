package com.company.insurance_request.domain.event;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionTopicEvent(

        // TODO - padronizar payload sanck_case @JsonProperty

        Long policieId,
        UUID customerId,
        Long productId,
        Category category,
        List<Coverage> coverages,
        List<String> assistances,
        BigDecimal totalMonthlyPremiumAmount,
        BigDecimal insuredAmount,
        PaymentMethod paymentMethod,
        SalesChannel salesChannel,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime finishedAt,
        LocalDateTime timestamp
){

}
