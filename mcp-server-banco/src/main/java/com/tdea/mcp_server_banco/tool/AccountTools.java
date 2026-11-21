package com.tdea.mcp_server_banco.tool;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.tdea.mcp_server_banco.dto.AccountResponse;
import com.tdea.mcp_server_banco.entity.AccountType;
import com.tdea.mcp_server_banco.service.AccountService;

import lombok.RequiredArgsConstructor;

/** Tools MCP de cuentas. Solo adaptan parámetros; la lógica vive en los servicios. */
@Component
@RequiredArgsConstructor
public class AccountTools {

    private final AccountService accountService;

    @Tool(name = "createAccount",
          description = "Abre una cuenta bancaria nueva y devuelve su número. Tipos: SAVINGS (ahorros) o CHECKING (corriente).")
    public AccountResponse createAccount(
            @ToolParam(description = "Nombre completo del titular") String ownerName,
            @ToolParam(description = "Documento de identidad del titular, solo dígitos") String ownerDocument,
            @ToolParam(description = "SAVINGS para ahorros o CHECKING para corriente") AccountType accountType,
            @ToolParam(description = "Depósito inicial en pesos, 0 si no hay", required = false) BigDecimal initialDeposit) {
        return accountService.createAccount(ownerName, ownerDocument, accountType, initialDeposit);
    }

    @Tool(name = "getAccount", description = "Consulta una cuenta por su número: titular, tipo y saldo actual.")
    public AccountResponse getAccount(
            @ToolParam(description = "Número de la cuenta") String accountNumber) {
        return accountService.getAccount(accountNumber);
    }

    @Tool(name = "listAccounts", description = "Lista todas las cuentas del banco con su saldo.")
    public List<AccountResponse> listAccounts() {
        return accountService.listAccounts();
    }
}
