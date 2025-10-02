package com.company.insurance_request.infrastructure.adapter.input.controller;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/policies")
public class PolicyController {

    private final CreatePoliceUseCase createPoliceUseCase;

    public PolicyController(CreatePoliceUseCase createPoliceUseCase) {
        this.createPoliceUseCase = createPoliceUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatePoliceResponse> createPolice(
            @RequestBody CreatePoliceRequest request
    ){
        Policy policySaved = createPoliceUseCase.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreatePoliceResponse(
                        policySaved.getId(),
                        policySaved.getCreatedAt()
                ));
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
