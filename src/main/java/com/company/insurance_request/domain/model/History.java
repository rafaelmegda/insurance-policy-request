package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class History {

    @JsonProperty("status")
    private Status status;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
