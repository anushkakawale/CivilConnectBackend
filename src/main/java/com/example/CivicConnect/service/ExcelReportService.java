package com.example.CivicConnect.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final ComplaintRepository complaintRepository;

    public byte[] complaintsExcel(LocalDate from, LocalDate to) throws Exception {

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Complaints");

            // Header style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Title", "Status", "Priority", "Ward", "Department", "Created At", "SLA Status"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Complaint> complaints =
                    complaintRepository.findByCreatedAtBetween(
                            from.atStartOfDay(),
                            to.plusDays(1).atStartOfDay()
                    );

            int rowIdx = 1;
            for (Complaint c : complaints) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getComplaintId());
                row.createCell(1).setCellValue(c.getTitle() != null ? c.getTitle() : "N/A");
                row.createCell(2).setCellValue(c.getStatus() != null ? c.getStatus().name() : "N/A");
                row.createCell(3).setCellValue(c.getPriority() != null ? c.getPriority().name() : "N/A");
                row.createCell(4).setCellValue(c.getWard() != null ? c.getWard().getAreaName() : "N/A");
                row.createCell(5).setCellValue(c.getDepartment() != null ? c.getDepartment().getName() : "N/A");
                row.createCell(6).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "N/A");
                row.createCell(7).setCellValue(c.isSlaBreached() ? "BREACHED" : "ON TRACK");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

}
