package com.uptrix.uptrix_backend.service.ai;

import com.uptrix.uptrix_backend.dto.ai.AiAgentType;
import com.uptrix.uptrix_backend.dto.ai.AiChatMessageDto;
import com.uptrix.uptrix_backend.dto.ai.AiChatRequestDto;
import com.uptrix.uptrix_backend.dto.ai.AiChatResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiCopilotService {

    private final AiProviderClient aiProviderClient;

    public AiCopilotService(AiProviderClient aiProviderClient) {
        this.aiProviderClient = aiProviderClient;
    }

    public AiChatResponseDto chat(AiChatRequestDto request) {

        AiAgentType agentType = request.getAgentType() != null
                ? request.getAgentType()
                : AiAgentType.GENERIC;

        String module = request.getModule();
        Long companyId = request.getCompanyId();
        Long userId = request.getUserId();
        List<AiChatMessageDto> messages = request.getMessages();
        Double temperature = request.getTemperature();

        String reply = aiProviderClient.completeChat(
                agentType,
                module,
                companyId,
                userId,
                messages,
                temperature
        );

        String agentName = switch (agentType) {
            case HR -> "HR Agent";
            case PAYROLL -> "Payroll Agent";
            case COMPLIANCE -> "Compliance Agent";
            case IT_HELPDESK -> "IT Helpdesk Agent";
            case RECRUITMENT -> "Recruitment Agent";
            default -> "Uptrix Copilot";
        };

        String hint = "Agent: " + agentName
                + (module != null ? (" · Module: " + module) : "")
                + (companyId != null ? (" · Tenant #" + companyId) : "");

        return new AiChatResponseDto(reply, agentName, hint);
    }
}
