package com.tdea.mcp_server_banco.dto;

import java.math.BigDecimal;

public record TransferResponse(
        String reference,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        BigDecimal fromBalanceAfter,
        String createdAt) {
}
