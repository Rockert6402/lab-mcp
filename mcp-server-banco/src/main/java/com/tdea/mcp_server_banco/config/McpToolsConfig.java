package com.tdea.mcp_server_banco.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.tdea.mcp_server_banco.tool.AccountTools;
import com.tdea.mcp_server_banco.tool.LoanTools;
import com.tdea.mcp_server_banco.tool.TransactionTools;

@Configuration
public class McpToolsConfig {

    // Publica todos los métodos @Tool en el servidor MCP (tools/list y tools/call)
    @Bean
    @DependsOn("demoDataLoader")
    public ToolCallbackProvider bankTools(AccountTools accountTools,
                                          TransactionTools transactionTools,
                                          LoanTools loanTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(accountTools, transactionTools, loanTools)
                .build();
    }
}
