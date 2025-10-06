package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.AggregationResult;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyAggregationService {

    private final PolicyService policyService;
    private final OrderTopicPublisherPort publisher;
    private final PolicyEventMapper policyEventMapper;

    public void finalizeAggregation(AggregationResult result){

        if(result == null){
            log.info("Result of aggregation null. Nothing to process.");
            return;
        }

        UUID policyId = (UUID) result.policyId();
        log.info("Aggregation received: policyId={}, paymentStatus={}, subscriptionStatus={}",
                policyId, result.paymentStatus(), result.subscriptionStatus());

        Status finalStatus = getFinalStatus(result);
        log.info("Requests for policyId {} approval is: {}", policyId, finalStatus);

        try {
            Policy policy = policyService.updateStatus(policyId, finalStatus);
            log.info("Status policy id {} updadet to {}", policyId, finalStatus);

            publisher.publishFinishStatus(policyEventMapper.toStatusEvent(policy), finalStatus.toValue());
            log.info("Publishing event to order topic status {} to policy id: {}", policy.getStatus(), policy.getPolicyId());

        } catch (Exception ex) {
            log.error("Failed to update final status for policyId {}: {}", policyId, ex.getMessage());
        }
    }

    private Status getFinalStatus(AggregationResult result) {
        boolean anyRejected = Status.REJECTED.toValue().equalsIgnoreCase(result.paymentStatus())
                || Status.REJECTED.toValue().equalsIgnoreCase(result.subscriptionStatus());

        boolean completed = result.paymentStatus() != null && result.subscriptionStatus() != null;

        Status finalStatus;
        if (anyRejected) {
            finalStatus = Status.REJECTED;
        } else if (completed) {
            finalStatus = Status.APPROVED;
        } else {
            finalStatus = Status.PENDING;
        }
        return finalStatus;
    }
}
