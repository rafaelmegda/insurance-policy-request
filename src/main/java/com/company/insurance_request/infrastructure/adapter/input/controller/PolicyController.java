package com.company.insurance_request.infrastructure.adapter.input.controller;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/policies")
public class PolicyController {

    private final CreatePoliceUseCase createPoliceUseCase;

    public PolicyController(CreatePoliceUseCase createPoliceUseCase) {
        this.createPoliceUseCase = createPoliceUseCase;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> createPolice(
            @RequestBody PolicyRequest request
    ) {
        log.info("Starting insurance policy application processing {}", request);

        try{
            Policy policySaved = createPoliceUseCase.create(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new PolicyResponse(
                            policySaved.getPolicyId(),
                            policySaved.getCreatedAt()
                    ));
        }catch (Exception ex){
            log.error("Error processing insurance policy application {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
    
    @GetMapping
    public Policy getPolice(){
        return Policy.builder().build();
    }

    @GetMapping(value = "/{policieId}")
    public Policy getPoliceById(){
        return Policy.builder().build();
    }


}
