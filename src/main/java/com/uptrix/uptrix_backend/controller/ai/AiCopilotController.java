package com.uptrix.uptrix_backend.controller.ai;

import com.uptrix.uptrix_backend.dto.ai.AiChatRequestDto;
import com.uptrix.uptrix_backend.dto.ai.AiChatResponseDto;
import com.uptrix.uptrix_backend.service.ai.AiCopilotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiCopilotController {

    private final AiCopilotService aiCopilotService;

    public AiCopilotController(AiCopilotService aiCopilotService) {
        this.aiCopilotService = aiCopilotService;
    }

    @Operation(summary = "Chat with AI Copilot", description = "Send a chat request to the AI copilot and receive a response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat response returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(@RequestBody AiChatRequestDto request) {
        AiChatResponseDto res = aiCopilotService.chat(request);
        return ResponseEntity.ok(res);
    }
}
