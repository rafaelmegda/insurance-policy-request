package com.company.insurance_request.infrastructure.adapter.input.controller;

import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/policies")
public class PoliceController {

    private final CreatePoliceUseCase createPoliceUseCase;

    public PoliceController(CreatePoliceUseCase createPoliceUseCase) {
        this.createPoliceUseCase = createPoliceUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatePoliceResponse> createPolice(
            @RequestBody CreatePoliceRequest request
    ){
        Police policeSaved = createPoliceUseCase.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreatePoliceResponse(
                        policeSaved.getId(),
                        policeSaved.getCreatedAt()
                ));
    }
    
    @GetMapping
    public Police getPolice(){
        return Police.builder().build();
    }

    @GetMapping(value = "/{policieId}")
    public Police getPoliceById(){
        return Police.builder().build();
    }


}
