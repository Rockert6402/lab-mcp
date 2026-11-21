package com.tdea.mcp_server_banco.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.tdea.mcp_server_banco.exception.BankException;

/** Validaciones comunes de montos y textos que llegan desde el modelo. */
final class MoneyUtils {

    private MoneyUtils() { }

    static BigDecimal requirePositive(BigDecimal amount, String field) {
        if (amount == null || amount.signum() <= 0) {
            throw new BankException("El campo '" + field + "' debe ser mayor que cero.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BankException("El campo '" + field + "' admite máximo 2 decimales.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BankException("El campo '" + field + "' es obligatorio.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BankException("El campo '" + field + "' supera " + maxLength + " caracteres.");
        }
        return trimmed;
    }

    static String optionalText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
