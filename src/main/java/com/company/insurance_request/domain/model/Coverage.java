package com.company.insurance_request.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Coverage {
    private Double roubo;
    private Double perdaTotal;
    private Double colisaoComTerceiros;
}
