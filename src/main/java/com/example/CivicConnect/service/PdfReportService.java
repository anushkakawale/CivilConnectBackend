package com.example.CivicConnect.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private final ComplaintRepository complaintRepository;

    public byte[] complaintsPdf(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<Complaint> complaints = complaintRepository.findByCreatedAtBetween(start, end);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CivicConnect - Complaints Report")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Report Period: " + from + " to " + to)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            float[] columnWidths = {1, 4, 3, 3, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Headers
            String[] headers = {"ID", "Title", "Status", "Ward", "Created At"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Data
            for (Complaint c : complaints) {
                table.addCell(new Cell().add(new Paragraph(c.getComplaintId().toString())));
                table.addCell(new Cell().add(new Paragraph(c.getTitle())));
                table.addCell(new Cell().add(new Paragraph(c.getStatus().name())));
                table.addCell(new Cell().add(new Paragraph(c.getWard() != null ? c.getWard().getAreaName() : "N/A")));
                table.addCell(new Cell().add(new Paragraph(c.getCreatedAt().toString().substring(0, 16).replace("T", " "))));
            }

            document.add(table);
            document.add(new Paragraph("\nTotal Complaints: " + complaints.size())
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF report: ", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
