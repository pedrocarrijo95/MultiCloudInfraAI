package com.pcarrijo.project.quarkusclientserver.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import com.pcarrijo.project.quarkusclientserver.ai.ToolsService;

@RegisterAiService(tools = ToolsService.class)
public interface MyAiService {
    @SystemMessage("You are a professional DevOps Engineer")
    String chat(@UserMessage String input);
}
