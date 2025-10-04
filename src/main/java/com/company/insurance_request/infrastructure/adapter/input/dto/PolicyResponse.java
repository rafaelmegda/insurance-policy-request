package com.company.insurance_request.infrastructure.adapter.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record PolicyResponse(

        @JsonProperty("id_policy") UUID id,
        @JsonProperty("created_at") LocalDateTime createdAt
){
    
}

