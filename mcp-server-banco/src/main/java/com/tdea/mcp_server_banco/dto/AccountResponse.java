package com.tdea.mcp_server_banco.dto;

import java.math.BigDecimal;

import com.tdea.mcp_server_banco.entity.Account;

/** DTO de salida: la entidad JPA nunca se expone al modelo. */
public record AccountResponse(
        String accountNumber,
        String ownerName,
        String ownerDocument,
        String type,
        BigDecimal balance,
        String createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getOwnerDocument(),
                account.getType().name(),
                account.getBalance(),
                account.getCreatedAt().toString());
    }
}
