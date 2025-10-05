package com.company.insurance_request.domain;

import com.company.insurance_request.domain.model.enums.Status;

import java.util.UUID;

public record AggregationResult(
        UUID policyId,
        String paymentStatus,
        String subscriptionStatus
) {
    public boolean completed() {
        return paymentStatus != null && subscriptionStatus != null;
    }

    public boolean anyRejected() {
        return "REJECTED".equalsIgnoreCase(paymentStatus)
                || "REJECTED".equalsIgnoreCase(subscriptionStatus);
    }

    public Status decision() {
        if (anyRejected()) return Status.REJECTED;
        if (completed()) return Status.APPROVED;
        return Status.PENDING;
    }
}
