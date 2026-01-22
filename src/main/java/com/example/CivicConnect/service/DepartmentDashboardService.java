package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

@Service
public class DepartmentDashboardService {

    private final ComplaintRepository complaintRepository;

    public DepartmentDashboardService(
            ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public List<Complaint> myWork(Long officerId) {
        return complaintRepository.findByAssignedOfficer_UserIdAndStatusIn(
                officerId,
                List.of(
                        ComplaintStatus.ASSIGNED,
                        ComplaintStatus.IN_PROGRESS
                )
        );
    }
}
