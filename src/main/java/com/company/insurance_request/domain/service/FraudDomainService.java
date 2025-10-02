package com.company.insurance_request.domain.service;

import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.Classification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class FraudDomainService {

    private static final BigDecimal REGULAR_VIDA_RESIDENCIAL = new BigDecimal("500000");
    private static final BigDecimal REGULAR_AUTO = new BigDecimal("350000");
    private static final BigDecimal REGULAR_OTHER = new BigDecimal("255000");

    private static final BigDecimal HIGH_RISK_AUTO = new BigDecimal("250000");
    private static final BigDecimal HIGH_RISK_RESIDENCIAL = new BigDecimal("150000");
    private static final BigDecimal HIGH_RISK_OTHER = new BigDecimal("125000");

    private static final BigDecimal PREFERENTIAL_VIDA = new BigDecimal("800000");
    private static final BigDecimal PREFERENTIAL_AUTO_RESIDENCIAL = new BigDecimal("450000");
    private static final BigDecimal PREFERENTIAL_OTHER = new BigDecimal("375000");

    private static final BigDecimal NOINFO_VIDA_RESIDENCIAL = new BigDecimal("200000");
    private static final BigDecimal NOINFO_AUTO = new BigDecimal("75000");
    private static final BigDecimal NOINFO_OTHER = new BigDecimal("55000");

    public boolean isValidated(Classification classification, Category category, BigDecimal insuredAmount) {
        switch (classification) {
            case REGULAR:
                return checkRegular(category, insuredAmount);
            case HIGH_RISK:
                return checkHighRisk(category, insuredAmount);
            case PREFERENTIAL:
                return checkPreferential(category, insuredAmount);
            case NO_INFORMATION:
                return checkNoInfo(category, insuredAmount);
            default:
                log.warn("Unknown classification: {} - Policy classified as rejected", classification);
                return false;
        }
    }

    private boolean checkRegular(Category category, BigDecimal insuredAmount) {
        if (category == Category.LIFE || category == Category.RESIDENTIAL) {
            return insuredAmount.compareTo(REGULAR_VIDA_RESIDENCIAL) <= 0;
        } else if (category == Category.AUTO) {
            return insuredAmount.compareTo(REGULAR_AUTO) <= 0;
        } else {
            return insuredAmount.compareTo(REGULAR_OTHER) <= 0;
        }
    }

    private boolean checkHighRisk(Category category, BigDecimal insuredAmount) {
        if (category == Category.AUTO) {
            return insuredAmount.compareTo(HIGH_RISK_AUTO) <= 0;
        } else if (category == Category.RESIDENTIAL) {
            return insuredAmount.compareTo(HIGH_RISK_RESIDENCIAL) <= 0;
        } else {
            return insuredAmount.compareTo(HIGH_RISK_OTHER) <= 0;
        }
    }

    private boolean checkPreferential(Category category, BigDecimal insuredAmount) {
        if (category == Category.LIFE) {
            return insuredAmount.compareTo(PREFERENTIAL_VIDA) < 0;
        } else if (category == Category.AUTO || category == Category.RESIDENTIAL) {
            return insuredAmount.compareTo(PREFERENTIAL_AUTO_RESIDENCIAL) < 0;
        } else {
            return insuredAmount.compareTo(PREFERENTIAL_OTHER) <= 0;
        }
    }

    private boolean checkNoInfo(Category category, BigDecimal insuredAmount) {
        if (category == Category.LIFE || category == Category.RESIDENTIAL) {
            return insuredAmount.compareTo(NOINFO_VIDA_RESIDENCIAL) <= 0;
        } else if (category == Category.AUTO) {
            return insuredAmount.compareTo(NOINFO_AUTO) <= 0;
        } else {
            return insuredAmount.compareTo(NOINFO_OTHER) <= 0;
        }
    }
}
