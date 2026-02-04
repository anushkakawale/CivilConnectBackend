package com.example.CivicConnect.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintSlaRepository slaRepository;
    private final UserRepository userRepository;

    // Summary report
    public Map<String, Object> summary(String from, String to) {

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay();

        long total = complaintRepository.countByCreatedAtBetween(startDt, endDt);

        // ✅ Filter SLA breaches by date
        long breached = complaintRepository.findByCreatedAtBetween(startDt, endDt)
                .stream()
                .filter(c -> c.isSlaBreached())
                .count();

        return Map.of(
                "totalComplaints", total,
                "slaCompliance", total == 0 ? "100%" :
                        String.format("%.1f%%", ((double) (total - breached) / total) * 100),
                "breachedSlas", breached
        );
    }

    // Complaints detail report
    public List<Map<String, Object>> complaints(String from, String to) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay();

        return complaintRepository.findByCreatedAtBetween(startDt, endDt)
                .stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getComplaintId(),
                        "title", c.getTitle(),
                        "status", c.getStatus().name(),
                        "priority", c.getPriority().name(),
                        "ward", c.getWard() != null ? "Ward #" + c.getWard().getWardNumber() + " (" + c.getWard().getAreaName() + ")" : "Unknown Ward",
                        "department", c.getDepartment() != null ? c.getDepartment().getName() : "General/Civic Office",
                        "createdAt", c.getCreatedAt().toString(),
                        "slaBreached", c.isSlaBreached()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    // SLA report
    public Map<String, Object> sla(String from, String to) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay();
        
        List<com.example.CivicConnect.entity.complaint.Complaint> complaints = 
            complaintRepository.findByCreatedAtBetween(startDt, endDt);

        long total = complaints.size();
        long breached = complaints.stream().filter(c -> c.isSlaBreached()).count();
        long active = complaints.stream().filter(c -> c.getSla() != null && c.getSla().getStatus() == SLAStatus.ON_TRACK).count();
        long completed = total - breached - active;

        return Map.of(
                "totalSlas", total,
                "active", active,
                "completed", completed,
                "breached", breached
        );
    }


    // User activity
    public Map<String, Object> userActivity() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "activeUsers", userRepository.countByActiveTrue(),
                "inactiveUsers", userRepository.countByActiveFalse()
        );
    }
}
