package com.uptrix.uptrix_backend.dto.chatAI;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ChatRequest {

    /**
     * Latest user message.
     */
    private String message;

    /**
     * Optional history: list of { role: "user"|"assistant"|"system", content: "..." }
     * This was already planned in your original design.
     */
    private List<Map<String, String>> history;

    /**
     * Optional: which AI agent should respond.
     * e.g. GENERIC, HR, PAYROLL, COMPLIANCE, IT_HELPDESK, RECRUITMENT
     */
    private String agentType;

    /**
     * Optional: module context (helps us build better system prompts).
     * e.g. ATTENDANCE, SHIFTS, LEAVES, HR, HELPDESK, PAYROLL, RECRUITMENT, GLOBAL
     */
    private String module;

    /**
     * Tenant context – which company this chat belongs to.
     */
    private Long companyId;

    /**
     * Optional: current user id (for future personalization).
     */
    private Long userId;

    /**
     * Optional: model temperature override; default will be 0.2 if null.
     */
    private Double temperature;
}
