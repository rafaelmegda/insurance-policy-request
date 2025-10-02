package com.company.insurance_request.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Coverage {

    //todo - Padronizar objeto
    private BigDecimal roubo;
    private BigDecimal perdaTotal;
    private BigDecimal colisaoComTerceiros;
}
