package com.tdea.mcp_server_tdea;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerTdeaApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerTdeaApplication.class, args);
    }

    // Registra los métodos @Tool de ShoppingCart en el servidor MCP.
    // La guía usa List<ToolCallback> con ToolCallbacks.from(...); esta es la forma documentada por Spring AI.
    @Bean
    public ToolCallbackProvider shoppingTools(ShoppingCart shoppingCart) {
        return MethodToolCallbackProvider.builder().toolObjects(shoppingCart).build();
    }
}
