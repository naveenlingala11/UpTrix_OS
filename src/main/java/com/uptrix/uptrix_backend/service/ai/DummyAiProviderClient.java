package com.uptrix.uptrix_backend.service.ai;

import com.uptrix.uptrix_backend.dto.ai.AiAgentType;
import com.uptrix.uptrix_backend.dto.ai.AiChatMessageDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DummyAiProviderClient implements AiProviderClient {

    @Override
    public String completeChat(
            AiAgentType agentType,
            String module,
            Long companyId,
            Long userId,
            List<AiChatMessageDto> messages,
            Double temperature
    ) {
        // Last user message
        String userText = "";
        if (messages != null && !messages.isEmpty()) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                AiChatMessageDto m = messages.get(i);
                if ("user".equalsIgnoreCase(m.getRole())) {
                    userText = m.getContent();
                    break;
                }
            }
        }

        String agentLabel = agentType != null ? agentType.name() : "GENERIC";
        String moduleLabel = module != null ? module : "GLOBAL";

        // Simple echo-style dummy to prove wiring works
        return "[Dummy " + agentLabel + " for " + moduleLabel + "] You said: \"" + userText + "\".\n"
                + "In production, this will call a real AI model with Uptrix data context.";
    }
}
