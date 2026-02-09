package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.UserRepository;

@Service
public class AdminDashboardService {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final UserRepository userRepository;

    public AdminDashboardService(
            ComplaintRepository complaintRepository,
            OfficerProfileRepository officerProfileRepository,
            UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.userRepository = userRepository;
    }

    public List<Complaint> readyToClose() {
        return complaintRepository.findByStatus(
                ComplaintStatus.APPROVED
        );
    }

    public java.util.Map<String, Object> getOverallStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalComplaints", complaintRepository.count());
        stats.put("pendingComplaints", complaintRepository.countByStatus(ComplaintStatus.SUBMITTED));
        stats.put("inProgressComplaints", complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        stats.put("resolvedComplaints", complaintRepository.countByStatus(ComplaintStatus.RESOLVED));
        stats.put("closedComplaints", complaintRepository.countByStatus(ComplaintStatus.CLOSED));
        
        stats.put("totalOfficers", officerProfileRepository.count());
        stats.put("activeOfficers", officerProfileRepository.findByActiveTrue().size());
        
        // You can add more specific metrics here
        return stats;
    }
}
