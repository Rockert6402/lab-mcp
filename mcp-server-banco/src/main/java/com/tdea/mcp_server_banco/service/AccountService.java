package com.tdea.mcp_server_banco.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdea.mcp_server_banco.dto.AccountResponse;
import com.tdea.mcp_server_banco.entity.Account;
import com.tdea.mcp_server_banco.entity.AccountType;
import com.tdea.mcp_server_banco.entity.BankTransaction;
import com.tdea.mcp_server_banco.entity.TransactionType;
import com.tdea.mcp_server_banco.exception.BankException;
import com.tdea.mcp_server_banco.repository.AccountRepository;
import com.tdea.mcp_server_banco.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    @Transactional
    public AccountResponse createAccount(String ownerName, String ownerDocument,
                                         AccountType type, BigDecimal initialDeposit) {
        String name = MoneyUtils.requireText(ownerName, "ownerName", 120);
        String document = MoneyUtils.requireText(ownerDocument, "ownerDocument", 20);
        if (!document.matches("\\d{4,20}")) {
            throw new BankException("El documento debe tener solo dígitos (4 a 20).");
        }
        if (type == null) {
            throw new BankException("El tipo de cuenta es obligatorio: SAVINGS (ahorros) o CHECKING (corriente).");
        }
        BigDecimal opening = (initialDeposit == null || initialDeposit.signum() == 0)
                ? BigDecimal.ZERO.setScale(2)
                : MoneyUtils.requirePositive(initialDeposit, "initialDeposit");

        Account account = accountRepository.save(Account.builder()
                .accountNumber(generateAccountNumber())
                .ownerName(name)
                .ownerDocument(document)
                .type(type)
                .balance(opening)
                .createdAt(LocalDateTime.now())
                .build());

        // El depósito inicial queda registrado como movimiento para que el extracto cuadre
        if (opening.signum() > 0) {
            transactionRepository.save(BankTransaction.builder()
                    .reference(TransactionService.newReference())
                    .account(account)
                    .type(TransactionType.DEPOSIT)
                    .amount(opening)
                    .balanceAfter(opening)
                    .description("Depósito inicial de apertura")
                    .createdAt(account.getCreatedAt())
                    .build());
        }
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber) {
        return AccountResponse.from(findAccount(accountNumber));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return accountRepository.findAll().stream().map(AccountResponse::from).toList();
    }

    public Account findAccount(String accountNumber) {
        String number = MoneyUtils.requireText(accountNumber, "accountNumber", 20);
        return accountRepository.findByAccountNumber(number)
                .orElseThrow(() -> new BankException("La cuenta " + number + " no existe."));
    }

    // Formato: 10 dígitos; se reintenta en el caso raro de colisión
    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", RANDOM.nextLong(1_000_000_000L, 10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
