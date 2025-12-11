package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.service.payroll.PayslipPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll/payslip")
public class PayrollPayslipController {

    private final PayslipPdfService payslipPdfService;

    public PayrollPayslipController(PayslipPdfService payslipPdfService) {
        this.payslipPdfService = payslipPdfService;
    }

    @Operation(summary = "Download payslip PDF", description = "Generate and download payslip PDF for employee/month",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF returned"),
            @ApiResponse(responseCode = "404", description = "Payslip not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<byte[]> downloadPayslip(
            @RequestParam Long employeeId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        byte[] pdf = payslipPdfService.generatePayslipPdf(employeeId, year, month);

        String filename = "payslip-" + employeeId + "-" + year + "-" + month + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
