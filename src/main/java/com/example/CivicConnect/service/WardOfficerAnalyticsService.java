package com.example.CivicConnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WardOfficerAnalyticsService {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public Map<String, Object> getDepartmentWiseAnalytics(Long wardOfficerUserId) {
        OfficerProfile wardProfile = officerProfileRepository
                .findByUser_UserId(wardOfficerUserId)
                .orElseThrow();

        Long wardId = wardProfile.getWard().getWardId();

        // Get department-wise complaint counts
        List<Object[]> deptStats = complaintRepository.countByWard_WardIdGroupByDepartment(wardId);
        
        Map<String, Object> analytics = new HashMap<>();
        Map<String, Long> departmentCounts = new HashMap<>();
        
        for (Object[] stat : deptStats) {
            String deptName = (String) stat[0];
            Long count = (Long) stat[1];
            departmentCounts.put(deptName, count);
        }
        
        analytics.put("departmentWiseCounts", departmentCounts);
        analytics.put("totalComplaints", departmentCounts.values().stream().mapToLong(Long::longValue).sum());
        
        return analytics;
    }

    public Map<String, Object> getSlaAnalytics(Long wardOfficerUserId) {
        OfficerProfile wardProfile = officerProfileRepository
                .findByUser_UserId(wardOfficerUserId)
                .orElseThrow();

        Long wardId = wardProfile.getWard().getWardId();

        long totalComplaints = complaintRepository.countByWard_WardId(wardId);
        long slaBreached = complaintRepository.countByWard_WardIdAndSlaBreachedTrue(wardId);
        long pendingApprovals = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.RESOLVED);

        Map<String, Object> slaAnalytics = new HashMap<>();
        slaAnalytics.put("totalComplaints", totalComplaints);
        slaAnalytics.put("slaBreached", slaBreached);
        slaAnalytics.put("slaComplianceRate", totalComplaints > 0 ? 
                ((double)(totalComplaints - slaBreached) / totalComplaints * 100) : 0);
        slaAnalytics.put("pendingApprovals", pendingApprovals);
        
        return slaAnalytics;
    }

    public Map<String, Object> getOfficerWorkloadAnalytics(Long wardOfficerUserId) {
        OfficerProfile wardProfile = officerProfileRepository
                .findByUser_UserId(wardOfficerUserId)
                .orElseThrow();

        Long wardId = wardProfile.getWard().getWardId();

        // Get all department officers in this ward
        List<OfficerProfile> deptOfficers = officerProfileRepository
                .findByWard_WardId(wardId)
                .stream()
                .filter(o -> o.getUser().getRole().name().equals("DEPARTMENT_OFFICER"))
                .toList();

        Map<String, Object> workloadData = new HashMap<>();
        Map<String, Long> officerWorkloads = new HashMap<>();

        for (OfficerProfile officer : deptOfficers) {
            long activeComplaints = complaintRepository
                    .countByAssignedOfficer_UserIdAndStatusIn(
                            officer.getUser().getUserId(),
                            List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
            
            officerWorkloads.put(officer.getUser().getName(), activeComplaints);
        }

        workloadData.put("officerWorkloads", officerWorkloads);
        workloadData.put("totalOfficers", deptOfficers.size());
        workloadData.put("averageWorkload", deptOfficers.isEmpty() ? 0 : 
                officerWorkloads.values().stream().mapToLong(Long::longValue).sum() / deptOfficers.size());

        return workloadData;
    }

    public Map<String, Object> getWardSummary(Long wardOfficerUserId) {
        OfficerProfile wardProfile = officerProfileRepository
                .findByUser_UserId(wardOfficerUserId)
                .orElseThrow();

        Long wardId = wardProfile.getWard().getWardId();

        Map<String, Object> summary = new HashMap<>();
        
        // Overall statistics
        long totalComplaints = complaintRepository.countByWard_WardId(wardId);
        long submitted = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.SUBMITTED);
        long assigned = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.RESOLVED);
        long approved = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.APPROVED);
        long closed = complaintRepository.countByWard_WardIdAndStatus(wardId, ComplaintStatus.CLOSED);

        summary.put("wardName", wardProfile.getWard().getAreaName());
        summary.put("wardNumber", wardProfile.getWard().getWardNumber());
        summary.put("totalComplaints", totalComplaints);
        summary.put("statusBreakdown", Map.of(
                "submitted", submitted,
                "assigned", assigned,
                "inProgress", inProgress,
                "resolved", resolved,
                "approved", approved,
                "closed", closed
        ));

        return summary;
    }
}
