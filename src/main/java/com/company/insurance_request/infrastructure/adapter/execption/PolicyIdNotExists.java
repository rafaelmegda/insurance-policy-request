package com.company.insurance_request.infrastructure.adapter.execption;

public class PolicyIdNotExists extends RuntimeException{
    public PolicyIdNotExists(String message) {
        super(message);
    }
}