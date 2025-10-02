package com.company.insurance_request.infrastructure.adapter.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record CreatePoliceResponse (

        @JsonProperty("id_policy")
        Long id,

        @JsonProperty("created_at")
        LocalDateTime createdAt
){

    }

