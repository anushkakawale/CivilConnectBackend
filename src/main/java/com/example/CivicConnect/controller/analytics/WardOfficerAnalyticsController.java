package com.example.CivicConnect.controller.analytics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ward-officer/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('WARD_OFFICER')")
public class WardOfficerAnalyticsController {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    /**
     * Get comprehensive ward analytics dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();

        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();

        // 1️⃣ CARDS (Optimized Count Queries)
        long totalComplaints = complaintRepository.countByWard_WardId(wardId);
        long pendingApproval = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.RESOLVED);
        long approved = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.APPROVED);
        long closed = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.CLOSED);
        long assigned = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.ASSIGNED);
        long inProgressCount = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.IN_PROGRESS);
        long inProgress = assigned + inProgressCount;

        // 2️⃣ SLA STATS
        long slaBreached = complaintRepository.countByWard_WardIdAndSlaBreachedTrue(wardId);
        long slaOnTrack = totalComplaints - slaBreached;

        // 3️⃣ DEPARTMENT PERFORMANCE (Aggregation Query)
        List<Object[]> deptStats = complaintRepository.getWardComplaintsByDepartmentAndStatus(wardId);
        
        List<Map<String, Object>> departmentPerformance = deptStats.stream()
                .collect(Collectors.groupingBy(row -> (String) row[0])) // Group by Dept Name
                .entrySet().stream()
                .map(entry -> {
                    String dept = entry.getKey();
                    long pending = 0;
                    long resolved = 0;
                    long total = 0;
                    
                    for(Object[] row : entry.getValue()) {
                        ComplaintStatus st = (ComplaintStatus) row[1];
                        long count = (Long) row[2];
                        total += count;
                        if(st == ComplaintStatus.ASSIGNED || st == ComplaintStatus.IN_PROGRESS) pending += count;
                        if(st == ComplaintStatus.RESOLVED || st == ComplaintStatus.APPROVED || st == ComplaintStatus.CLOSED) resolved += count;
                    }
                    
                    double rate = total > 0 ? ((double) resolved / total * 100) : 0;
                    return Map.<String, Object>of(
                        "department", dept,
                        "total", total,
                        "pending", pending,
                        "resolved", resolved,
                        "completionRate", String.format("%.0f%%", rate)
                    );
                }).toList();

        // 4️⃣ OFFICER PERFORMANCE (Aggregation Query)
        List<Object[]> officerStats = complaintRepository.getWardComplaintsByOfficerAndStatus(wardId);

        List<Map<String, Object>> officerPerformance = officerStats.stream()
                .collect(Collectors.groupingBy(row -> (Long) row[0])) // Group by User ID
                .entrySet().stream()
                .map(entry -> {
                    List<Object[]> rows = entry.getValue();
                    // Just take name/dept from first row
                    String name = (String) rows.get(0)[1];
                    String dept = (String) rows.get(0)[2];
                    
                    long total = 0;
                    long done = 0;
                    
                    for(Object[] row : rows) {
                        ComplaintStatus st = (ComplaintStatus) row[3];
                        long count = (Long) row[4];
                        total += count;
                        if(st == ComplaintStatus.RESOLVED || st == ComplaintStatus.APPROVED || st == ComplaintStatus.CLOSED) done += count;
                    }
                    long pend = total - done;

                    return Map.<String, Object>of(
                        "officerName", name,
                        "department", dept,
                        "totalAssigned", total,
                        "resolved", done,
                        "pending", pend
                    );
                }).toList();

        // 5️⃣ RECENT ACTIVITY
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        long last7Days = complaintRepository.countByWard_WardIdAndCreatedAtAfter(wardId, sevenDaysAgo);
        long closedLast7Days = complaintRepository.countByWard_WardIdAndStatusAndUpdatedAtAfter(wardId, ComplaintStatus.CLOSED, sevenDaysAgo);

        // FINAL RESPONSE STRUCTURE
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("ward", profile.getWard() != null ? profile.getWard().getAreaName() : "N/A");
        response.put("officer", user.getName());
        
        Map<String, Object> cards = new java.util.HashMap<>();
        cards.put("totalComplaints", totalComplaints);
        cards.put("pendingApproval", pendingApproval);
        cards.put("approved", approved);
        cards.put("inProgress", inProgress);
        cards.put("closed", closed);
        response.put("cards", cards);
        
        Map<String, Object> sla = new java.util.HashMap<>();
        sla.put("breached", slaBreached);
        sla.put("onTrack", slaOnTrack);
        response.put("sla", sla);
        
        response.put("departmentPerformance", departmentPerformance);
        response.put("officerPerformance", officerPerformance);
        
        Map<String, Object> recent = new java.util.HashMap<>();
        recent.put("last7Days", last7Days);
        recent.put("closedLast7Days", closedLast7Days);
        response.put("recentActivity", recent);

        return ResponseEntity.ok(response);
    }


    /**
     * Get work distribution across departments
     */
    @GetMapping("/department-distribution")
    public ResponseEntity<?> getDepartmentDistribution(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();
        List<Complaint> complaints = complaintRepository.findByWard_WardId(wardId);

        Map<String, Map<String, Long>> distributionMap = complaints.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.groupingBy(
                                c -> c.getStatus().name(),
                                Collectors.counting()
                        )
                ));

        List<Map<String, Object>> distribution = distributionMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> deptData = new java.util.HashMap<>();
                    deptData.put("department", entry.getKey());
                    deptData.put("stats", entry.getValue());
                    return deptData;
                }).toList();

        return ResponseEntity.ok(distribution);
    }

    /**
     * Get monthly trends for the ward
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<?> getMonthlyTrends(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        OfficerProfile profile = officerProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();
        
        // Get complaints from last 6 months
        java.time.LocalDateTime sixMonthsAgo = java.time.LocalDateTime.now().minusMonths(6);
        List<Complaint> complaints = complaintRepository
                .findByWard_WardIdAndCreatedAtAfter(wardId, sixMonthsAgo);

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
}
