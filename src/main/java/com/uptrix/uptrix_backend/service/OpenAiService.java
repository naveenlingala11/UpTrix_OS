package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.chatAI.ChatRequest;
import com.uptrix.uptrix_backend.dto.chatAI.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Central AI gateway using OpenAI chat completions.
 * - Uses one endpoint: /api/ai/chat
 * - Supports agents + module context via ChatRequest.
 */
@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponse chat(ChatRequest request) {

        try {
            if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
                return new ChatResponse("Please enter a prompt to continue.");
            }

            // 1) Build system prompt from agent + module + context
            String systemPrompt = buildSystemPrompt(
                    safeUpper(request.getAgentType()),
                    safeUpper(request.getModule()),
                    request.getCompanyId(),
                    request.getUserId()
            );

            // 2) Build OpenAI messages array
            List<Map<String, String>> messages = new ArrayList<>();

            // system message first
            messages.add(Map.of(
                    "role", "system",
                    "content", systemPrompt
            ));

            // history if provided
            if (request.getHistory() != null) {
                for (Map<String, String> h : request.getHistory()) {
                    String role = h.getOrDefault("role", "user");
                    String content = h.getOrDefault("content", "");
                    if (content == null || content.isBlank()) continue;
                    messages.add(Map.of(
                            "role", role,
                            "content", content
                    ));
                }
            }

            // latest user message
            messages.add(Map.of(
                    "role", "user",
                    "content", request.getMessage()
            ));

            // 3) Request body
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 4) Call OpenAI API
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return new ChatResponse("AI service returned non-OK status: " + response.getStatusCode());
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                return new ChatResponse("AI service returned an empty response.");
            }

            // 5) Parse "choices[0].message.content"
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return new ChatResponse("AI did not return any choices.");
            }

            JsonNode firstChoice = choices.get(0);
            String reply = firstChoice.path("message").path("content")
                    .asText("I could not generate a reply.");

            return new ChatResponse(reply.trim());

        } catch (HttpClientErrorException e) {
            System.err.println("❌ OpenAI HTTP error: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            return new ChatResponse("Auth error talking to AI service. Please check server configuration.");
        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("Something went wrong while talking to the AI service.");
        }
    }

    private String safeUpper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private String buildSystemPrompt(String agentType, String module, Long companyId, Long userId) {

        String agentLabel;
        if (agentType == null || agentType.isBlank() || "GENERIC".equals(agentType)) {
            agentLabel = "Uptrix Copilot";
        } else if ("HR".equals(agentType)) {
            agentLabel = "Uptrix HR Agent";
        } else if ("PAYROLL".equals(agentType)) {
            agentLabel = "Uptrix Payroll Agent";
        } else if ("COMPLIANCE".equals(agentType)) {
            agentLabel = "Uptrix Compliance / Policy Agent";
        } else if ("IT_HELPDESK".equals(agentType)) {
            agentLabel = "Uptrix IT Helpdesk Agent";
        } else if ("RECRUITMENT".equals(agentType)) {
            agentLabel = "Uptrix Recruitment Agent";
        } else {
            agentLabel = "Uptrix Copilot (" + agentType + ")";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You are ")
                .append(agentLabel)
                .append(" inside the Uptrix HROS platform. ");

        if (module != null && !module.isBlank() && !"GLOBAL".equals(module)) {
            sb.append("You are currently assisting in the ").append(module).append(" module. ");
        } else {
            sb.append("You can answer across all modules (attendance, shifts, leaves, HR, helpdesk, payroll, recruitment). ");
        }

        if (companyId != null) {
            sb.append("Tenant/Company ID: ").append(companyId).append(". ");
        }
        if (userId != null) {
            sb.append("Respond as a helpful assistant for the signed-in user (id ").append(userId).append("). ");
        }

        sb.append("Be concise, practical and business-friendly. ")
                .append("If the question is about Uptrix features, explain how to achieve it step by step. ")
                .append("Do not fabricate company-specific data if you do not have it; instead, describe how the user can find it in Uptrix.");

        return sb.toString();
    }
}
