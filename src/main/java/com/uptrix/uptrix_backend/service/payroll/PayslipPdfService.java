package com.uptrix.uptrix_backend.service.payroll;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.uptrix.uptrix_backend.dto.payroll.PayrollPreviewDto;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;

@Service
public class PayslipPdfService {

    private final PayrollCalculationService payrollCalculationService;

    public PayslipPdfService(PayrollCalculationService payrollCalculationService) {
        this.payrollCalculationService = payrollCalculationService;
    }

    public byte[] generatePayslipPdf(Long employeeId, int year, int month) {
        try {
            PayrollPreviewDto preview =
                    payrollCalculationService.previewEmployeeMonth(employeeId, year, month);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Payslip", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Employee & period info
            Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            document.add(new Paragraph("Employee: " + preview.getEmployeeName() + " (" + preview.getEmployeeCode() + ")", infoFont));
            document.add(new Paragraph("Period: " + preview.getYear() + " / " + preview.getMonth(), infoFont));
            document.add(new Paragraph(" ", infoFont));

            // Earnings table
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            PdfPTable earningTable = new PdfPTable(new float[]{4, 2, 2});
            earningTable.setWidthPercentage(100);

            addHeaderCell(earningTable, "Earning Component", headerFont);
            addHeaderCell(earningTable, "Code", headerFont);
            addHeaderCell(earningTable, "Amount", headerFont);

            preview.getEarnings().forEach(e -> {
                earningTable.addCell(new PdfPCell(new Phrase(
                        (e.getComponentName() != null ? e.getComponentName() : e.getComponentCode())
                )));
                earningTable.addCell(new PdfPCell(new Phrase(e.getComponentCode())));
                earningTable.addCell(new PdfPCell(new Phrase(
                        e.getAmount() != null ? e.getAmount().toString() : "0.00"
                )));
            });

            document.add(new Paragraph("Earnings", headerFont));
            document.add(earningTable);

            document.add(new Paragraph(" "));

            // Deductions table
            PdfPTable deductionTable = new PdfPTable(new float[]{4, 2, 2});
            deductionTable.setWidthPercentage(100);

            addHeaderCell(deductionTable, "Deduction Component", headerFont);
            addHeaderCell(deductionTable, "Code", headerFont);
            addHeaderCell(deductionTable, "Amount", headerFont);

            preview.getDeductions().forEach(d -> {
                deductionTable.addCell(new PdfPCell(new Phrase(
                        d.getName() != null ? d.getName() : d.getCode()
                )));
                deductionTable.addCell(new PdfPCell(new Phrase(d.getCode())));
                deductionTable.addCell(new PdfPCell(new Phrase(
                        d.getAmount() != null ? d.getAmount().toString() : "0.00"
                )));
            });

            document.add(new Paragraph("Deductions", headerFont));
            document.add(deductionTable);

            document.add(new Paragraph(" "));

            // Summary
            Font totalFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            document.add(new Paragraph("Gross Earnings: " + preview.getGrossEarnings(), totalFont));
            document.add(new Paragraph("Total Deductions: " + preview.getTotalDeductions(), totalFont));
            document.add(new Paragraph("Net Pay: " + preview.getNetPay(), totalFont));

            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate payslip PDF", ex);
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        table.addCell(cell);
    }
}
