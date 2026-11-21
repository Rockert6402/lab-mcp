package com.tdea.mcp_server_banco.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdea.mcp_server_banco.config.BankProperties;
import com.tdea.mcp_server_banco.dto.LoanResponse;
import com.tdea.mcp_server_banco.entity.Account;
import com.tdea.mcp_server_banco.entity.LoanRequest;
import com.tdea.mcp_server_banco.entity.LoanStatus;
import com.tdea.mcp_server_banco.entity.TransactionType;
import com.tdea.mcp_server_banco.exception.BankException;
import com.tdea.mcp_server_banco.repository.LoanRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final int MIN_TERM = 6;
    private static final int MAX_TERM = 60;
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    private final LoanRequestRepository loanRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BankProperties properties;

    /** Crea la solicitud y la evalúa de inmediato: la cuota no puede superar el % configurado del ingreso. */
    @Transactional
    public LoanResponse requestLoan(String accountNumber, BigDecimal amount, Integer termMonths,
                                    BigDecimal monthlyIncome, String purpose) {
        Account account = accountService.findAccount(accountNumber);
        BigDecimal principal = MoneyUtils.requirePositive(amount, "amount");
        BigDecimal income = MoneyUtils.requirePositive(monthlyIncome, "monthlyIncome");
        BankProperties.Loan rules = properties.loan();

        if (principal.compareTo(rules.maxAmount()) > 0) {
            throw new BankException("El monto máximo de crédito es " + rules.maxAmount() + ".");
        }
        if (termMonths == null || termMonths < MIN_TERM || termMonths > MAX_TERM) {
            throw new BankException("El plazo debe estar entre " + MIN_TERM + " y " + MAX_TERM + " meses.");
        }

        BigDecimal payment = monthlyPayment(principal, rules.annualRate(), termMonths);
        BigDecimal maxPayment = income.multiply(rules.maxPaymentRatio()).setScale(2, RoundingMode.HALF_UP);
        boolean approved = payment.compareTo(maxPayment) <= 0;
        String reason = approved
                ? "Aprobado: la cuota " + payment + " no supera el tope de " + maxPayment + "."
                : "Rechazado: la cuota " + payment + " supera el tope de " + maxPayment
                  + " (" + rules.maxPaymentRatio().movePointRight(2).stripTrailingZeros().toPlainString()
                  + "% del ingreso mensual).";

        LoanRequest loan = loanRepository.save(LoanRequest.builder()
                .account(account)
                .amount(principal)
                .termMonths(termMonths)
                .monthlyIncome(income)
                .annualRate(rules.annualRate())
                .monthlyPayment(payment)
                .purpose(MoneyUtils.optionalText(purpose, 200))
                .status(approved ? LoanStatus.APPROVED : LoanStatus.REJECTED)
                .decisionReason(reason)
                .createdAt(LocalDateTime.now())
                .build());
        return LoanResponse.from(loan);
    }

    /** Abona el crédito aprobado a la cuenta y lo marca como desembolsado (una sola vez). */
    @Transactional
    public LoanResponse disburseLoan(Long loanId) {
        if (loanId == null) {
            throw new BankException("El campo 'loanId' es obligatorio.");
        }
        LoanRequest loan = loanRepository.findForUpdate(loanId)
                .orElseThrow(() -> new BankException("La solicitud de crédito " + loanId + " no existe."));
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BankException("Solo se desembolsan créditos APPROVED. Estado actual: " + loan.getStatus());
        }

        Account account = transactionService.lockAccount(loan.getAccount().getAccountNumber());
        account.setBalance(account.getBalance().add(loan.getAmount()));
        transactionService.record(TransactionService.newReference(), account, TransactionType.LOAN_DISBURSEMENT,
                loan.getAmount(), null, "Desembolso del crédito #" + loan.getId());
        loan.setStatus(LoanStatus.DISBURSED);
        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> listLoans(String accountNumber) {
        Account account = accountService.findAccount(accountNumber);
        return loanRepository.findByAccountAccountNumberOrderByCreatedAtDesc(account.getAccountNumber())
                .stream().map(LoanResponse::from).toList();
    }

    /** Cuota fija (sistema francés): P·i·(1+i)^n / ((1+i)^n − 1), con i = tasa anual / 12. */
    static BigDecimal monthlyPayment(BigDecimal principal, BigDecimal annualRate, int months) {
        MathContext mc = MathContext.DECIMAL64;
        if (annualRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal i = annualRate.divide(TWELVE, mc);
        BigDecimal factor = BigDecimal.ONE.add(i).pow(months, mc);
        return principal.multiply(i).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }
}
