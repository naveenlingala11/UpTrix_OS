package com.uptrix.uptrix_backend.dto.ai;

public class AiChatMessageDto {

    /**
     * "user", "assistant", or "system"
     */
    private String role;

    /**
     * Natural language content
     */
    private String content;

    public AiChatMessageDto() {
    }

    public AiChatMessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
