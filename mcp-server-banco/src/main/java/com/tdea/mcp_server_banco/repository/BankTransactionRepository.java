package com.tdea.mcp_server_banco.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tdea.mcp_server_banco.entity.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByAccountAccountNumberOrderByCreatedAtDescIdDesc(String accountNumber, Pageable pageable);
}
