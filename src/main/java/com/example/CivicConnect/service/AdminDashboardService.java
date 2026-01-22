package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

@Service
public class AdminDashboardService {

    private final ComplaintRepository complaintRepository;

    public AdminDashboardService(
            ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public List<Complaint> readyToClose() {
        return complaintRepository.findByStatus(
                ComplaintStatus.APPROVED
        );
    }
}
