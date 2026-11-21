package com.tdea.mcp_server_banco.exception;

/**
 * Error de negocio. Spring AI convierte la excepción lanzada por un @Tool en un
 * resultado de error MCP con este mensaje, así Claude se lo explica al usuario.
 */
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }
}
