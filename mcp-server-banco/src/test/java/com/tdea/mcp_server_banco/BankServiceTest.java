package com.tdea.mcp_server_banco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.tdea.mcp_server_banco.dto.AccountResponse;
import com.tdea.mcp_server_banco.dto.LoanResponse;
import com.tdea.mcp_server_banco.entity.AccountType;
import com.tdea.mcp_server_banco.exception.BankException;
import com.tdea.mcp_server_banco.service.AccountService;
import com.tdea.mcp_server_banco.service.LoanService;
import com.tdea.mcp_server_banco.service.TransactionService;

@SpringBootTest
@ActiveProfiles("test")
class BankServiceTest {

    @Autowired private AccountService accountService;
    @Autowired private TransactionService transactionService;
    @Autowired private LoanService loanService;

    private AccountResponse newAccount(String document, String balance) {
        return accountService.createAccount("Cliente " + document, document, AccountType.SAVINGS, new BigDecimal(balance));
    }

    @Test
    void transferMovesMoneyBetweenAccounts() {
        AccountResponse a = newAccount("2001", "100000");
        AccountResponse b = newAccount("2002", "0");

        transactionService.transfer(a.accountNumber(), b.accountNumber(), new BigDecimal("40000"), "arriendo");

        assertThat(accountService.getAccount(a.accountNumber()).balance()).isEqualByComparingTo("60000");
        assertThat(accountService.getAccount(b.accountNumber()).balance()).isEqualByComparingTo("40000");
        assertThat(transactionService.history(b.accountNumber(), 5)).hasSize(1);
    }

    @Test
    void transferWithoutFundsRollsBack() {
        AccountResponse a = newAccount("2003", "1000");
        AccountResponse b = newAccount("2004", "0");

        assertThatThrownBy(() -> transactionService.transfer(a.accountNumber(), b.accountNumber(), new BigDecimal("5000"), null))
                .isInstanceOf(BankException.class)
                .hasMessageContaining("Fondos insuficientes");
        assertThat(accountService.getAccount(a.accountNumber()).balance()).isEqualByComparingTo("1000");
        assertThat(accountService.getAccount(b.accountNumber()).balance()).isEqualByComparingTo("0");
    }

    @Test
    void depositAndWithdrawUpdateBalance() {
        AccountResponse a = newAccount("2005", "0");
        transactionService.deposit(a.accountNumber(), new BigDecimal("250000.50"), "nómina");
        transactionService.withdraw(a.accountNumber(), new BigDecimal("50000"), "cajero");
        assertThat(accountService.getAccount(a.accountNumber()).balance()).isEqualByComparingTo("200000.50");
    }

    @Test
    void loanIsApprovedAndDisbursedOnce() {
        AccountResponse a = newAccount("2006", "0");
        // Cuota de 10M a 24 meses al 18% anual = 499.241,02 <= 40% de 2M
        LoanResponse loan = loanService.requestLoan(a.accountNumber(), new BigDecimal("10000000"), 24,
                new BigDecimal("2000000"), "moto");
        assertThat(loan.status()).isEqualTo("APPROVED");
        assertThat(loan.monthlyPayment()).isEqualByComparingTo("499241.02");

        loanService.disburseLoan(loan.loanId());
        assertThat(accountService.getAccount(a.accountNumber()).balance()).isEqualByComparingTo("10000000");
        assertThatThrownBy(() -> loanService.disburseLoan(loan.loanId())).isInstanceOf(BankException.class);
    }

    @Test
    void loanIsRejectedWhenPaymentExceedsIncomeRatio() {
        AccountResponse a = newAccount("2007", "0");
        LoanResponse loan = loanService.requestLoan(a.accountNumber(), new BigDecimal("10000000"), 24,
                new BigDecimal("1000000"), null);
        assertThat(loan.status()).isEqualTo("REJECTED");
    }
}
