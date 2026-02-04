package com.example.CivicConnect.service;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledReportService {

    private final PdfReportService pdfService;

    // Every month on 1st day at 9 AM
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlyReport() {

        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(1).withDayOfMonth(1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        byte[] pdf = pdfService.complaintsPdf(start, end);

        // TODO: Email / store / upload
        System.out.println("Monthly admin report generated: " + pdf.length + " bytes");
    }
}
