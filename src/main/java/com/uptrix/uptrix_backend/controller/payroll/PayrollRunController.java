package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.dto.payroll.MyPayslipDto;
import com.uptrix.uptrix_backend.dto.payroll.PayrollRunDto;
import com.uptrix.uptrix_backend.service.payroll.BankFileExportService;
import com.uptrix.uptrix_backend.service.payroll.PayrollRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll/runs")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;
    private final BankFileExportService bankFileExportService;

    public PayrollRunController(PayrollRunService payrollRunService,
                                BankFileExportService bankFileExportService) {
        this.payrollRunService = payrollRunService;
        this.bankFileExportService = bankFileExportService;
    }

    @Operation(summary = "List payroll runs", description = "List payroll runs, optionally filtered by companyId",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<List<PayrollRunDto>> listRuns(
            @RequestParam(required = false) Long companyId
    ) {
        List<PayrollRunDto> runs = payrollRunService.listRuns(companyId);
        return ResponseEntity.ok(runs);
    }

    public static final class ApproveRequest {
        public Long approverEmployeeId;
    }

    @Operation(summary = "Approve payroll run", description = "Approve a payroll run (HR/Finance action)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{runId}/approve")
    public ResponseEntity<PayrollRunDto> approveRun(
            @PathVariable Long runId,
            @RequestBody(required = false) ApproveRequest request
    ) {
        Long approverId = request != null ? request.approverEmployeeId : null;
        PayrollRunDto dto = payrollRunService.approveRun(runId, approverId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Lock payroll run", description = "Lock a payroll run to prevent further edits",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{runId}/lock")
    public ResponseEntity<PayrollRunDto> lockRun(@PathVariable Long runId) {
        PayrollRunDto dto = payrollRunService.lockRun(runId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Download bank file (CSV) for run", description = "Generate bank transfer CSV for a payroll run",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{runId}/bank-file")
    public ResponseEntity<byte[]> downloadBankFile(@PathVariable Long runId) {
        byte[] csv = bankFileExportService.generateBankFileCsv(runId);
        String filename = "bank-file-run-" + runId + ".csv";

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csv);
    }

    @Operation(summary = "List payslips for employee", description = "List generated payslips for an employee",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/employee-payslips")
    public ResponseEntity<List<MyPayslipDto>> listEmployeePayslips(
            @RequestParam Long employeeId
    ) {
        List<MyPayslipDto> list = payrollRunService.listPayslipsForEmployee(employeeId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Download bank CSV for run", description = "Alternate endpoint to download bank CSV file",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/runs/{runId}/bank-csv")
    public ResponseEntity<byte[]> downloadBankCsv(@PathVariable Long runId) {
        byte[] csv = payrollRunService.exportRunBankCsv(runId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("payroll-run-"+ runId + "-bank.csv").build());
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

}
