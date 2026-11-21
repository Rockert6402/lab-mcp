package com.tdea.mcp_server_banco.dto;

import java.math.BigDecimal;

import com.tdea.mcp_server_banco.entity.LoanRequest;

public record LoanResponse(
        Long loanId,
        String accountNumber,
        BigDecimal amount,
        Integer termMonths,
        BigDecimal annualRate,
        BigDecimal monthlyPayment,
        BigDecimal monthlyIncome,
        String purpose,
        String status,
        String decisionReason,
        String createdAt) {

    public static LoanResponse from(LoanRequest loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getAccount().getAccountNumber(),
                loan.getAmount(),
                loan.getTermMonths(),
                loan.getAnnualRate(),
                loan.getMonthlyPayment(),
                loan.getMonthlyIncome(),
                loan.getPurpose(),
                loan.getStatus().name(),
                loan.getDecisionReason(),
                loan.getCreatedAt().toString());
    }
}
