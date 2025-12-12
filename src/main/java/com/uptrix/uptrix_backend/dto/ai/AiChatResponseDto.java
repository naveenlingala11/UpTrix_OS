package com.uptrix.uptrix_backend.dto.ai;

public class AiChatResponseDto {

    private String reply;
    private String agentName;
    private String reasoningHint;  // short explanation / meta info (optional)

    public AiChatResponseDto() {
    }

    public AiChatResponseDto(String reply, String agentName, String reasoningHint) {
        this.reply = reply;
        this.agentName = agentName;
        this.reasoningHint = reasoningHint;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getReasoningHint() {
        return reasoningHint;
    }

    public void setReasoningHint(String reasoningHint) {
        this.reasoningHint = reasoningHint;
    }
}
