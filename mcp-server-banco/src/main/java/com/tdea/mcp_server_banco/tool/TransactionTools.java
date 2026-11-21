package com.tdea.mcp_server_banco.tool;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.tdea.mcp_server_banco.dto.TransactionResponse;
import com.tdea.mcp_server_banco.dto.TransferResponse;
import com.tdea.mcp_server_banco.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionTools {

    private final TransactionService transactionService;

    @Tool(name = "deposit", description = "Realiza una consignación (depósito) en una cuenta y devuelve el nuevo saldo.")
    public TransactionResponse deposit(
            @ToolParam(description = "Número de la cuenta destino") String accountNumber,
            @ToolParam(description = "Monto en pesos, mayor que cero, máximo 2 decimales") BigDecimal amount,
            @ToolParam(description = "Concepto de la consignación", required = false) String description) {
        return transactionService.deposit(accountNumber, amount, description);
    }

    @Tool(name = "withdraw", description = "Retira dinero de una cuenta. Falla si no hay fondos suficientes.")
    public TransactionResponse withdraw(
            @ToolParam(description = "Número de la cuenta") String accountNumber,
            @ToolParam(description = "Monto en pesos, mayor que cero, máximo 2 decimales") BigDecimal amount,
            @ToolParam(description = "Concepto del retiro", required = false) String description) {
        return transactionService.withdraw(accountNumber, amount, description);
    }

    @Tool(name = "transfer",
          description = "Transfiere dinero entre dos cuentas de forma atómica: se debita y acredita todo o nada. Falla si no hay fondos.")
    public TransferResponse transfer(
            @ToolParam(description = "Número de la cuenta origen") String fromAccount,
            @ToolParam(description = "Número de la cuenta destino") String toAccount,
            @ToolParam(description = "Monto en pesos, mayor que cero, máximo 2 decimales") BigDecimal amount,
            @ToolParam(description = "Concepto de la transferencia", required = false) String description) {
        return transactionService.transfer(fromAccount, toAccount, amount, description);
    }

    @Tool(name = "getTransactions", description = "Extracto: últimos movimientos de una cuenta, del más reciente al más antiguo.")
    public List<TransactionResponse> getTransactions(
            @ToolParam(description = "Número de la cuenta") String accountNumber,
            @ToolParam(description = "Cantidad de movimientos (por defecto 10, máximo 50)", required = false) Integer limit) {
        return transactionService.history(accountNumber, limit);
    }
}
