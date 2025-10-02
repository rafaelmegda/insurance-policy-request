package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class History {

    //todo - padronizar objeto
    private Status status;
    private LocalDateTime timestamp;
}
