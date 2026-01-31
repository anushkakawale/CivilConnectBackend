package com.example.CivicConnect.service.citizen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitizenDashboardService {

    private final ComplaintRepository complaintRepository;

    public Map<String, Object> getDashboardData(User citizen) {
        // Get all complaints for this citizen
        List<Complaint> allComplaints = complaintRepository.findByCitizen(citizen);

        // Calculate statistics
        long totalComplaints = allComplaints.size();
        long pendingComplaints = allComplaints.stream()
            .filter(c -> c.getStatus() == ComplaintStatus.SUBMITTED)
            .count();
        long inProgressComplaints = allComplaints.stream()
            .filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS || 
                        c.getStatus() == ComplaintStatus.ASSIGNED ||
                        c.getStatus() == ComplaintStatus.APPROVED)
            .count();
        long resolvedComplaints = allComplaints.stream()
            .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED)
            .count();
        long closedComplaints = allComplaints.stream()
            .filter(c -> c.getStatus() == ComplaintStatus.CLOSED)
            .count();

        // Get recent complaints (last 5)
        List<Map<String, Object>> recentComplaints = allComplaints.stream()
            .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
            .limit(5)
            .map(this::mapComplaintToSummary)
            .collect(Collectors.toList());

        // Complaints by status
        Map<String, Long> complaintsByStatus = new HashMap<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            long count = allComplaints.stream()
                .filter(c -> c.getStatus() == status)
                .count();
            if (count > 0) {
                complaintsByStatus.put(status.name(), count);
            }
        }

        // Complaints by priority
        Map<String, Long> complaintsByPriority = new HashMap<>();
        for (Priority priority : Priority.values()) {
            long count = allComplaints.stream()
                .filter(c -> c.getPriority() == priority)
                .count();
            if (count > 0) {
                complaintsByPriority.put(priority.name(), count);
            }
        }

        // Build response
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalComplaints", totalComplaints);
        dashboard.put("pendingComplaints", pendingComplaints);
        dashboard.put("inProgressComplaints", inProgressComplaints);
        dashboard.put("resolvedComplaints", resolvedComplaints);
        dashboard.put("closedComplaints", closedComplaints);
        dashboard.put("recentComplaints", recentComplaints);
        dashboard.put("complaintsByStatus", complaintsByStatus);
        dashboard.put("complaintsByPriority", complaintsByPriority);

        return dashboard;
    }

    private Map<String, Object> mapComplaintToSummary(Complaint complaint) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("complaintId", complaint.getComplaintId());
        summary.put("title", complaint.getTitle());
        summary.put("status", complaint.getStatus().name());
        summary.put("priority", complaint.getPriority().name());
        summary.put("createdAt", complaint.getCreatedAt());
        summary.put("category", complaint.getCategory()); // category is String, not enum
        return summary;
    }
}
