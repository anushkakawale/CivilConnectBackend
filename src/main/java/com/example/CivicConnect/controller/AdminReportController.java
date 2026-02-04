package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.AdminReportService;
import com.example.CivicConnect.service.ExcelReportService;
import com.example.CivicConnect.service.PdfReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService reportService;
    private final PdfReportService pdfService;
    private final ExcelReportService excelService;

    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(reportService.summary(from, to));
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> complaints(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(reportService.complaints(from, to));
    }

    @GetMapping("/sla")
    public ResponseEntity<?> sla(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(reportService.sla(from, to));
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(reportService.userActivity());
    }

    @GetMapping("/complaints/pdf")
    public ResponseEntity<byte[]> complaintsPdf(
            @RequestParam String from,
            @RequestParam String to) {

        byte[] pdf = pdfService.complaintsPdf(
                java.time.LocalDate.parse(from),
                java.time.LocalDate.parse(to)
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=complaints.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/complaints/excel")
    public ResponseEntity<byte[]> complaintsExcel(
            @RequestParam String from,
            @RequestParam String to) throws Exception {

        byte[] excel = excelService.complaintsExcel(
                java.time.LocalDate.parse(from),
                java.time.LocalDate.parse(to)
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=complaints.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
