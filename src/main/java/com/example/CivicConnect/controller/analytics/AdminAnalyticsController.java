package com.example.CivicConnect.controller.analytics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.WardRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final ComplaintRepository complaintRepository;
    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficerProfileRepository officerProfileRepository;

    /**
     * Get comprehensive city-wide analytics dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getCityWideDashboard() {
        
        List<Complaint> allComplaints = complaintRepository.findAll();

        // Overall statistics
        long totalComplaints = allComplaints.size();
        long assigned = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED)
                .count();
        long inProgress = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS)
                .count();
        long resolved = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED)
                .count();
        long approved = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.APPROVED)
                .count();
        long closed = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
                .count();

        // Pending approvals (ready to close)
        long pendingApprovals = resolved;
        long readyToClose = approved;

        // SLA statistics
        long slaBreached = allComplaints.stream()
                .filter(c -> c.getSla() != null && 
                        c.getSla().getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.BREACHED)
                .count();
        long slaWarning = allComplaints.stream()
                .filter(c -> c.getSla() != null && 
                        c.getSla().getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.WARNING)
                .count();
        long slaOnTrack = allComplaints.stream()
                .filter(c -> c.getSla() != null && 
                        c.getSla().getStatus() == com.example.CivicConnect.entity.enums.SLAStatus.ON_TRACK)
                .count();

        // Calculate city-wide completion rate
        double completionRate = totalComplaints > 0 
                ? ((double) (approved + closed) / totalComplaints) * 100 
                : 0;

        // Ward-wise breakdown
        Map<String, Long> wardWise = allComplaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getWard().getAreaName(),
                        Collectors.counting()
                ));

        Map<String, Long> wardPending = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED || 
                             c.getStatus() == ComplaintStatus.IN_PROGRESS)
                .collect(Collectors.groupingBy(
                        c -> c.getWard().getAreaName(),
                        Collectors.counting()
                ));

        Map<String, Long> wardClosed = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
                .collect(Collectors.groupingBy(
                        c -> c.getWard().getAreaName(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> wardPerformance = wardWise.entrySet().stream()
                .map(entry -> {
                    String wardName = entry.getKey();
                    long total = entry.getValue();
                    long pending = wardPending.getOrDefault(wardName, 0L);
                    long wardClosedCount = wardClosed.getOrDefault(wardName, 0L);
                    double wardCompletionRate = total > 0 ? ((double) wardClosedCount / total) * 100 : 0;

                    return Map.<String, Object>of(
                            "wardName", wardName,
                            "total", total,
                            "pending", pending,
                            "closed", wardClosedCount,
                            "completionRate", String.format("%.1f%%", wardCompletionRate)
                    );
                })
                .sorted((m1, m2) -> Long.compare((Long) m2.get("total"), (Long) m1.get("total")))
                .collect(Collectors.toList());

        // Department-wise breakdown
        Map<String, Long> deptWise = allComplaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.counting()
                ));

        Map<String, Long> deptPending = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED || 
                             c.getStatus() == ComplaintStatus.IN_PROGRESS)
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.counting()
                ));

        Map<String, Long> deptClosed = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> departmentPerformance = deptWise.entrySet().stream()
                .map(entry -> {
                    String deptName = entry.getKey();
                    long total = entry.getValue();
                    long pending = deptPending.getOrDefault(deptName, 0L);
                    long deptClosedCount = deptClosed.getOrDefault(deptName, 0L);
                    double deptCompletionRate = total > 0 ? ((double) deptClosedCount / total) * 100 : 0;

                    return Map.<String, Object>of(
                            "departmentName", deptName,
                            "total", total,
                            "pending", pending,
                            "closed", deptClosedCount,
                            "completionRate", String.format("%.1f%%", deptCompletionRate)
                    );
                })
                .sorted((m1, m2) -> Long.compare((Long) m2.get("total"), (Long) m1.get("total")))
                .collect(Collectors.toList());

        // Recent activity (last 7 days)
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        long recentComplaints = allComplaints.stream()
                .filter(c -> c.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
        long recentClosed = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED && 
                             c.getUpdatedAt().isAfter(sevenDaysAgo))
                .count();

        // Officer statistics
        long totalWardOfficers = officerProfileRepository
                .countByUser_Role(com.example.CivicConnect.entity.enums.RoleName.WARD_OFFICER);
        long totalDeptOfficers = officerProfileRepository
                .countByUser_Role(com.example.CivicConnect.entity.enums.RoleName.DEPARTMENT_OFFICER);

        Map<String, Object> response = Map.<String, Object>of(
                "overallStatistics", Map.<String, Object>of(
                        "totalComplaints", totalComplaints,
                        "assigned", assigned,
                        "inProgress", inProgress,
                        "resolved", resolved,
                        "approved", approved,
                        "closed", closed,
                        "completionRate", String.format("%.1f%%", completionRate),
                        "pendingApprovals", pendingApprovals,
                        "readyToClose", readyToClose
                ),
                "slaStatistics", Map.<String, Object>of(
                        "breached", slaBreached,
                        "warning", slaWarning,
                        "onTrack", slaOnTrack,
                        "total", slaBreached + slaWarning + slaOnTrack
                ),
                "wardPerformance", wardPerformance,
                "departmentPerformance", departmentPerformance,
                "recentActivity", Map.<String, Object>of(
                        "last7Days", recentComplaints,
                        "closedLast7Days", recentClosed
                ),
                "resources", Map.<String, Object>of(
                        "totalWards", wardRepository.count(),
                        "totalDepartments", departmentRepository.count(),
                        "wardOfficers", totalWardOfficers,
                        "departmentOfficers", totalDeptOfficers
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly trends for the entire city
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<?> getMonthlyTrends() {
        
        // Get complaints from last 12 months
        java.time.LocalDateTime twelveMonthsAgo = java.time.LocalDateTime.now().minusMonths(12);
        List<Complaint> complaints = complaintRepository
                .findByCreatedAtAfter(twelveMonthsAgo);

        // Group by month
        Map<String, Long> monthlyRegistered = complaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().getYear() + "-" + 
                             String.format("%02d", c.getCreatedAt().getMonthValue()),
                        Collectors.counting()
                ));

        Map<String, Long> monthlyClosed = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
                .collect(Collectors.groupingBy(
                        c -> c.getUpdatedAt().getYear() + "-" + 
                             String.format("%02d", c.getUpdatedAt().getMonthValue()),
                        Collectors.counting()
                ));

        return ResponseEntity.ok(Map.<String, Object>of(
                "monthlyRegistered", monthlyRegistered,
                "monthlyClosed", monthlyClosed
        ));
    }

    /**
     * Get priority-wise distribution
     */
    @GetMapping("/priority-distribution")
    public ResponseEntity<?> getPriorityDistribution() {
        List<Complaint> allComplaints = complaintRepository.findAll();

        Map<String, Long> priorityDistribution = allComplaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getPriority().name(),
                        Collectors.counting()
                ));

        return ResponseEntity.ok(priorityDistribution);
    }

    /**
     * Get top performing officers
     */
    @GetMapping("/top-performers")
    public ResponseEntity<?> getTopPerformers() {
        List<Complaint> closedComplaints = complaintRepository
                .findByStatus(ComplaintStatus.CLOSED);

        Map<String, Long> officerPerformance = closedComplaints.stream()
                .filter(c -> c.getAssignedOfficer() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getAssignedOfficer().getName(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> topPerformers = officerPerformance.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .map(entry -> Map.<String, Object>of(
                        "officerName", entry.getKey(),
                        "closedComplaints", entry.getValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.<String, Object>of(
                "topPerformers", topPerformers
        ));
    }
}
