package com.tdea.mcp_server_banco.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tdea.mcp_server_banco.entity.LoanRequest;

import jakarta.persistence.LockModeType;

public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {

    List<LoanRequest> findByAccountAccountNumberOrderByCreatedAtDesc(String accountNumber);

    // Evita desembolsar dos veces el mismo crédito en llamadas concurrentes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanRequest l where l.id = :id")
    Optional<LoanRequest> findForUpdate(@Param("id") Long id);
}
