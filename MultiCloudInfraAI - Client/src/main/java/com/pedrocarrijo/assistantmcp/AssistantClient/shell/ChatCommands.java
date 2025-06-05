package com.pedrocarrijo.assistantmcp.AssistantClient.shell;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpTransport;

@ShellComponent
public class ChatCommands {

    private final ChatClient chatClient;

    private ToolCallbackProvider tools;
    ListToolsResult toolsResult ;

    public ChatCommands(ChatClient.Builder builder, ToolCallbackProvider tools) {
        this.tools = tools;
        this.chatClient = builder
                .defaultTools(tools)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
        System.out.println(tools);
    }


    @ShellMethod(key = "chat")
    public String interactiveChat(@ShellOption(defaultValue = "Hi I am the MCP Client") String prompt) {
        try {
            return this.chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            System.err.println("Failed to access MCP tools: " + e.getMessage());
            return "I was unable to access the MCP Server right now. Please try again in a few moments.";
        }
    }
}
