package com.tdea.mcp_server_banco.dto;

import java.math.BigDecimal;

import com.tdea.mcp_server_banco.entity.BankTransaction;

public record TransactionResponse(
        String reference,
        String accountNumber,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String counterpartAccount,
        String description,
        String createdAt) {

    public static TransactionResponse from(BankTransaction tx) {
        return new TransactionResponse(
                tx.getReference(),
                tx.getAccount().getAccountNumber(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getCounterpartAccount(),
                tx.getDescription(),
                tx.getCreatedAt().toString());
    }
}
