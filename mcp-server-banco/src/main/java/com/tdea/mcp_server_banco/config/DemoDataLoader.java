package com.tdea.mcp_server_banco.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.tdea.mcp_server_banco.entity.AccountType;
import com.tdea.mcp_server_banco.repository.AccountRepository;
import com.tdea.mcp_server_banco.service.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * Crea dos cuentas de prueba la primera vez que arranca con la BD vacía.
 * Corre antes de registrar las tools (ver @DependsOn en McpToolsConfig) para que
 * la primera llamada de Claude ya las vea.
 */
@Component
@RequiredArgsConstructor
public class DemoDataLoader implements InitializingBean {

    private final BankProperties properties;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @Override
    public void afterPropertiesSet() {
        if (!properties.seedDemoData() || accountRepository.count() > 0) {
            return;
        }
        accountService.createAccount("Juan Pérez", "1001", AccountType.SAVINGS, new BigDecimal("2000000"));
        accountService.createAccount("María Gómez", "1002", AccountType.CHECKING, new BigDecimal("500000"));
    }
}
