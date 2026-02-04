package com.example.CivicConnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.repository.WardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final ComplaintRepository complaintRepository;
    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public Map<String, Object> getDashboard() {

        long totalComplaints = complaintRepository.count();
        long totalWards = wardRepository.count();
        long safeTotalWards = Math.max(totalWards, 1);

        long assigned = complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long approved = complaintRepository.countByStatus(ComplaintStatus.APPROVED);
        long closed = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        
        double avg = (double) totalComplaints / safeTotalWards;

        long breached = complaintRepository.countBySlaStatus(SLAStatus.BREACHED);
        long warning = complaintRepository.countBySlaStatus(SLAStatus.WARNING);
        long onTrack = complaintRepository.countBySlaStatus(SLAStatus.ON_TRACK);
        long completed = complaintRepository.countByStatus(ComplaintStatus.CLOSED); 
        long totalSlas = breached + warning + onTrack + completed;
        
        double compliance = totalSlas == 0 ? 100.0 : ((double)(totalSlas - breached) / totalSlas) * 100;

        Map<String, Object> response = new HashMap<>();
        
        // --- TOP LEVEL KEY METRICS ---
        response.put("totalComplaints", totalComplaints);
        response.put("avgComplaintsPerWard", Double.isFinite(avg) ? avg : 0.0);
        response.put("complianceRate", Double.isFinite(compliance) ? compliance : 0.0);
        response.put("activeSlas", breached + warning + onTrack);
        
        // --- BREAKDOWNS ---
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("assigned", assigned);
        statusCounts.put("inProgress", inProgress);
        statusCounts.put("resolved", resolved);
        statusCounts.put("approved", approved);
        statusCounts.put("closed", closed);
        response.put("statusBreakdown", statusCounts);

        Map<String, Long> slaDetails = new HashMap<>();
        slaDetails.put("breached", breached);
        slaDetails.put("warning", warning);
        slaDetails.put("onTrack", onTrack);
        slaDetails.put("completed", completed);
        slaDetails.put("total", totalSlas);
        response.put("slaDetails", slaDetails);

        response.put("byWard", complaintRepository.countByWard().stream()
                .map(objs -> Map.of("name", objs[0] != null ? objs[0] : "Unknown Area", "count", objs[1]))
                .collect(Collectors.toList()));

        response.put("byDepartment", complaintRepository.countByDepartment().stream()
                .map(objs -> Map.of("name", objs[0] != null ? objs[0] : "Other", "count", objs[1]))
                .collect(Collectors.toList()));

        response.put("resources", Map.of(
                "totalWards", totalWards,
                "totalDepartments", departmentRepository.count(),
                "totalWardOfficers", officerProfileRepository.countByUser_Role(RoleName.WARD_OFFICER),
                "totalDeptOfficers", officerProfileRepository.countByUser_Role(RoleName.DEPARTMENT_OFFICER)
        ));

        return response;
    }

    public List<Map<String, Object>> getOfficerWorkload() {
        return complaintRepository.findAll().stream()
                .filter(c -> c.getAssignedOfficer() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getAssignedOfficer(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> workload = new HashMap<>();
                    workload.put("officerId", entry.getKey().getUserId());
                    workload.put("officerName", entry.getKey().getName());
                    workload.put("complaintCount", entry.getValue());
                    workload.put("department", entry.getKey().getRole().name());
                    return workload;
                })
                .collect(Collectors.toList());
    }

}
