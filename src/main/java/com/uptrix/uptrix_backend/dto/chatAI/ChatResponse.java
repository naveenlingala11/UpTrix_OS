package com.uptrix.uptrix_backend.dto.chatAI;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ChatResponse {
    private String reply;

    public ChatResponse() {}

    public ChatResponse(String reply) {
        this.reply = reply;
    }
}