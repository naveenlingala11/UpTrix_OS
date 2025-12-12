package com.uptrix.uptrix_backend.controller.ai;

import com.uptrix.uptrix_backend.dto.ai.HrJdRequestDto;
import com.uptrix.uptrix_backend.dto.ai.HrJdResponseDto;
import com.uptrix.uptrix_backend.dto.chatAI.ChatRequest;
import com.uptrix.uptrix_backend.dto.chatAI.ChatResponse;
import com.uptrix.uptrix_backend.entity.Department;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.DepartmentRepository;
import com.uptrix.uptrix_backend.service.OpenAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai/hr")
@CrossOrigin(origins = "*")
public class HrAiController {

    private final OpenAiService openAiService;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;

    public HrAiController(OpenAiService openAiService,
                          CompanyRepository companyRepository,
                          DepartmentRepository departmentRepository) {
        this.openAiService = openAiService;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
    }

    @Operation(summary = "Generate job description (HR)", description = "Generate a job description using AI powered by OpenAI.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job description generated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "AI service error")
    })
    @PostMapping("/jd")
    public ResponseEntity<HrJdResponseDto> generateJobDescription(@RequestBody HrJdRequestDto dto) {

        Long companyId = dto.getCompanyId();
        Long deptId = dto.getDepartmentId();

        // 🔹 Company context
        String companyName = "your company";
        if (companyId != null) {
            Optional<Company> cOpt = companyRepository.findById(companyId);
            if (cOpt.isPresent() && cOpt.get().getName() != null) {
                companyName = cOpt.get().getName();
            }
        }

        // 🔹 Department context
        String departmentName = null;
        String departmentCode = null;
        if (deptId != null) {
            Optional<Department> dOpt = departmentRepository.findById(deptId);
            if (dOpt.isPresent()) {
                Department d = dOpt.get();
                departmentName = d.getName();
                departmentCode = d.getCode();
            }
        }

        // 🔹 Build prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert HR and recruitment specialist working inside the Uptrix HROS platform. ");
        prompt.append("Write a clear, attractive job description using the details below. ");
        prompt.append("Structure the output in Markdown with these sections:\\n");
        prompt.append("1. Role summary\\n");
        prompt.append("2. Key responsibilities\\n");
        prompt.append("3. Requirements\\n");
        prompt.append("4. Nice to have\\n");
        prompt.append("5. About the company\\n\\n");

        prompt.append("Company: ").append(companyName).append("\\n");
        if (departmentName != null) {
            prompt.append("Department: ").append(departmentName);
            if (departmentCode != null) {
                prompt.append(" (").append(departmentCode).append(")");
            }
            prompt.append("\\n");
        }

        if (dto.getJobTitle() != null) {
            prompt.append("Job Title: ").append(dto.getJobTitle()).append("\\n");
        }
        if (dto.getSeniorityLevel() != null) {
            prompt.append("Seniority level: ").append(dto.getSeniorityLevel()).append("\\n");
        }
        if (dto.getEmploymentType() != null) {
            prompt.append("Employment type: ").append(dto.getEmploymentType()).append("\\n");
        }
        if (dto.getLocation() != null) {
            prompt.append("Location: ").append(dto.getLocation()).append("\\n");
        }
        if (dto.getWorkMode() != null) {
            prompt.append("Work mode: ").append(dto.getWorkMode()).append("\\n");
        }

        if (dto.getMustHaveSkills() != null && !dto.getMustHaveSkills().isEmpty()) {
            String skills = dto.getMustHaveSkills().stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
            if (!skills.isEmpty()) {
                prompt.append("Must-have skills: ").append(skills).append("\\n");
            }
        }

        if (dto.getNiceToHaveSkills() != null && !dto.getNiceToHaveSkills().isEmpty()) {
            String skills = dto.getNiceToHaveSkills().stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
            if (!skills.isEmpty()) {
                prompt.append("Nice-to-have skills: ").append(skills).append("\\n");
            }
        }

        if (dto.getSummaryNotes() != null && !dto.getSummaryNotes().isEmpty()) {
            prompt.append("Extra notes from HR: ").append(dto.getSummaryNotes()).append("\\n");
        }

        String tone = dto.getTone() != null ? dto.getTone().toUpperCase() : "NEUTRAL";
        prompt.append("\\nTone: Use a ")
                .append(tone.toLowerCase())
                .append(" tone suitable for a job posting. ");
        prompt.append("Avoid mentioning that you are an AI. ");
        prompt.append("Do not fabricate company details you don't know.\\n");

        // 🔹 Use your existing OpenAiService + extended ChatRequest
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(prompt.toString());
        chatRequest.setAgentType("HR");
        chatRequest.setModule("RECRUITMENT");
        chatRequest.setCompanyId(companyId);
        chatRequest.setHistory(null);
        chatRequest.setTemperature(0.4);

        ChatResponse aiResponse = openAiService.chat(chatRequest);
        HrJdResponseDto responseDto = new HrJdResponseDto(aiResponse.getReply());

        return ResponseEntity.ok(responseDto);
    }
}
