package com.pedrocarrijo.assistantmcp.AssistantMCPServer;


import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.pedrocarrijo.assistantmcp.AssistantMCPServer.service.ToolsService;


@SpringBootApplication
public class AssistantMcpServerApplication {

	@Autowired
	ToolsService toolsService;

	@Bean
	public ToolCallbackProvider utilTools() {
		return MethodToolCallbackProvider.builder().toolObjects(toolsService).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(AssistantMcpServerApplication.class, args);
	}


}
