package com.tdea.mcp_server_banco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class McpServerBancoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerBancoApplication.class, args);
    }
}
