package com.uptrix.uptrix_backend.dto.ai;

import java.util.List;

public class AiChatRequestDto {

    private AiAgentType agentType;
    private String module;          // e.g. "ATTENDANCE", "HR", "HELPDESK"
    private Long companyId;         // tenant context
    private Long userId;            // (optional) current user

    private List<AiChatMessageDto> messages;

    // optional knobs
    private Double temperature;

    public AiChatRequestDto() {
    }

    public AiAgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AiAgentType agentType) {
        this.agentType = agentType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<AiChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<AiChatMessageDto> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
