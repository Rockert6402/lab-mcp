package com.tdea.mcp_server_banco.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdea.mcp_server_banco.dto.TransactionResponse;
import com.tdea.mcp_server_banco.dto.TransferResponse;
import com.tdea.mcp_server_banco.entity.Account;
import com.tdea.mcp_server_banco.entity.BankTransaction;
import com.tdea.mcp_server_banco.entity.TransactionType;
import com.tdea.mcp_server_banco.exception.BankException;
import com.tdea.mcp_server_banco.repository.AccountRepository;
import com.tdea.mcp_server_banco.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int MAX_HISTORY = 50;

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional
    public TransactionResponse deposit(String accountNumber, BigDecimal amount, String description) {
        BigDecimal value = MoneyUtils.requirePositive(amount, "amount");
        Account account = lockAccount(accountNumber);
        account.setBalance(account.getBalance().add(value));
        return TransactionResponse.from(record(newReference(), account, TransactionType.DEPOSIT, value, null,
                MoneyUtils.optionalText(description, 200)));
    }

    @Transactional
    public TransactionResponse withdraw(String accountNumber, BigDecimal amount, String description) {
        BigDecimal value = MoneyUtils.requirePositive(amount, "amount");
        Account account = lockAccount(accountNumber);
        requireFunds(account, value);
        account.setBalance(account.getBalance().subtract(value));
        return TransactionResponse.from(record(newReference(), account, TransactionType.WITHDRAWAL, value, null,
                MoneyUtils.optionalText(description, 200)));
    }

    /**
     * Transferencia atómica: o se aplican el débito y el crédito, o ninguno (rollback).
     * Las cuentas se bloquean siempre en el mismo orden para evitar interbloqueos.
     */
    @Transactional
    public TransferResponse transfer(String fromAccount, String toAccount, BigDecimal amount, String description) {
        BigDecimal value = MoneyUtils.requirePositive(amount, "amount");
        String from = MoneyUtils.requireText(fromAccount, "fromAccount", 20);
        String to = MoneyUtils.requireText(toAccount, "toAccount", 20);
        if (from.equals(to)) {
            throw new BankException("La cuenta origen y destino no pueden ser la misma.");
        }

        boolean fromFirst = from.compareTo(to) < 0;
        Account first = lockAccount(fromFirst ? from : to);
        Account second = lockAccount(fromFirst ? to : from);
        Account source = fromFirst ? first : second;
        Account target = fromFirst ? second : first;

        requireFunds(source, value);
        source.setBalance(source.getBalance().subtract(value));
        target.setBalance(target.getBalance().add(value));

        String reference = newReference();
        String note = MoneyUtils.optionalText(description, 200);
        BankTransaction out = record(reference, source, TransactionType.TRANSFER_OUT, value, target.getAccountNumber(), note);
        record(reference, target, TransactionType.TRANSFER_IN, value, source.getAccountNumber(), note);

        return new TransferResponse(reference, source.getAccountNumber(), target.getAccountNumber(),
                value, source.getBalance(), out.getCreatedAt().toString());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String accountNumber, Integer limit) {
        Account account = accountService.findAccount(accountNumber);
        int size = (limit == null || limit <= 0) ? 10 : Math.min(limit, MAX_HISTORY);
        return transactionRepository
                .findByAccountAccountNumberOrderByCreatedAtDescIdDesc(account.getAccountNumber(), PageRequest.of(0, size))
                .stream().map(TransactionResponse::from).toList();
    }

    // Usado también por LoanService para el desembolso
    public BankTransaction record(String reference, Account account, TransactionType type, BigDecimal amount,
                           String counterpart, String description) {
        return transactionRepository.save(BankTransaction.builder()
                .reference(reference)
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .counterpartAccount(counterpart)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public Account lockAccount(String accountNumber) {
        String number = MoneyUtils.requireText(accountNumber, "accountNumber", 20);
        return accountRepository.findForUpdate(number)
                .orElseThrow(() -> new BankException("La cuenta " + number + " no existe."));
    }

    static String newReference() {
        return "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static void requireFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BankException("Fondos insuficientes en la cuenta " + account.getAccountNumber()
                    + ". Saldo disponible: " + account.getBalance());
        }
    }
}
