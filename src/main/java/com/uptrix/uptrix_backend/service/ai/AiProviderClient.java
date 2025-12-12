package com.uptrix.uptrix_backend.service.ai;

import com.uptrix.uptrix_backend.dto.ai.AiAgentType;
import com.uptrix.uptrix_backend.dto.ai.AiChatMessageDto;

import java.util.List;

public interface AiProviderClient {

    /**
     * Given an agent type, context prompt and chat history, return a reply string.
     * Later we can plug OpenAI / Azure / anything here.
     */
    String completeChat(
            AiAgentType agentType,
            String module,
            Long companyId,
            Long userId,
            List<AiChatMessageDto> messages,
            Double temperature
    );
}
