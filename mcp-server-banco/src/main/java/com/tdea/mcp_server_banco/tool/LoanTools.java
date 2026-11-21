package com.tdea.mcp_server_banco.tool;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.tdea.mcp_server_banco.dto.LoanResponse;
import com.tdea.mcp_server_banco.service.LoanService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanTools {

    private final LoanService loanService;

    @Tool(name = "requestLoan",
          description = "Crea una solicitud de crédito y la evalúa al instante: se aprueba si la cuota mensual "
                      + "(sistema francés, tasa 18% anual) no supera el 40% del ingreso mensual. Plazo de 6 a 60 meses.")
    public LoanResponse requestLoan(
            @ToolParam(description = "Número de la cuenta donde se abonaría el crédito") String accountNumber,
            @ToolParam(description = "Monto solicitado en pesos") BigDecimal amount,
            @ToolParam(description = "Plazo en meses, entre 6 y 60") Integer termMonths,
            @ToolParam(description = "Ingreso mensual del solicitante en pesos") BigDecimal monthlyIncome,
            @ToolParam(description = "Destino del crédito", required = false) String purpose) {
        return loanService.requestLoan(accountNumber, amount, termMonths, monthlyIncome, purpose);
    }

    @Tool(name = "disburseLoan",
          description = "Desembolsa un crédito APPROVED: abona el monto a la cuenta y lo deja en estado DISBURSED.")
    public LoanResponse disburseLoan(
            @ToolParam(description = "Id de la solicitud de crédito") Long loanId) {
        return loanService.disburseLoan(loanId);
    }

    @Tool(name = "listLoans", description = "Lista las solicitudes de crédito de una cuenta con su estado y cuota.")
    public List<LoanResponse> listLoans(
            @ToolParam(description = "Número de la cuenta") String accountNumber) {
        return loanService.listLoans(accountNumber);
    }
}
