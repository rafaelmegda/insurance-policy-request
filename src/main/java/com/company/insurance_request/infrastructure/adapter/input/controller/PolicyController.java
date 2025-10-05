package com.company.insurance_request.infrastructure.adapter.input.controller;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.PolicyUseCase;
import com.company.insurance_request.infrastructure.adapter.execption.PolicyIdNotExists;
import com.company.insurance_request.infrastructure.adapter.execption.PolicyStatusUpdateException;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyResponse;
import com.company.insurance_request.infrastructure.adapter.input.mapper.PolicyResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/v1/policies")
public class PolicyController {

    private final PolicyUseCase policyUseCase;

    public PolicyController(PolicyUseCase policyUseCase) {
        this.policyUseCase = policyUseCase;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> createPolice(
            @RequestBody PolicyRequest request
    ) {
        log.info("Starting insurance policy application processing {}", request);

        try{
            Policy policySaved = policyUseCase.create(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(PolicyResponseMapper.toResponse(policySaved)
                    );
        }catch (Exception ex){
            log.error("Error processing insurance policy application {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping()
    public ResponseEntity<List<PolicyResponse>> getPolicyById(
            @RequestParam(name = "policy_id", required = false) UUID policyId,
            @RequestParam(required = false, name = "customer_id") UUID customerId
    ){
        log.info("Starting get insurance policy");

        try{
            List<Policy> policies = policyUseCase.getPolicyById(policyId, customerId);
            if(policies == null || policies.isEmpty()){
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
            }
            List<PolicyResponse> responses = policies.stream()
                    .map(PolicyResponseMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(responses);
        }catch (Exception ex){
            log.error("Error consulting insurance policy application {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @PatchMapping("/{policyId}")
    public ResponseEntity<PolicyResponse> updatePolicyStatus(
            @PathVariable(name = "policyId") UUID policyId,
            @RequestParam(name = "status") String status
    ){
        log.info("Starting update insurance policy status");

        try{
            Policy updated = policyUseCase.updateStatus(policyId, Enum.valueOf(Status.class, status));
            return ResponseEntity.ok(PolicyResponseMapper.toResponse(updated));
        } catch (PolicyStatusUpdateException ex){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (PolicyIdNotExists ex){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        catch (Exception ex){
            log.error("Error updating insurance policy application {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
