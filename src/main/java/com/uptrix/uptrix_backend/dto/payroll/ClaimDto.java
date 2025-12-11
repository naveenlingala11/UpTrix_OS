package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ClaimDto {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
}
