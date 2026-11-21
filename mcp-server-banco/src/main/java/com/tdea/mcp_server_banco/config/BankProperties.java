package com.tdea.mcp_server_banco.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Reglas de negocio configurables desde application.properties (prefijo bank.*). */
@ConfigurationProperties(prefix = "bank")
public record BankProperties(Loan loan, boolean seedDemoData) {

    public record Loan(BigDecimal annualRate, BigDecimal maxAmount, BigDecimal maxPaymentRatio) { }
}
