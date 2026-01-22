package com.example.CivicConnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.RoleName;

public interface ComplaintApprovalRepository
extends JpaRepository<ComplaintApproval, Long> {

List<ComplaintApproval> findByStatusAndRoleAtTime(
    ApprovalStatus status,
    RoleName roleAtTime
);

Optional<ComplaintApproval> findByComplaintAndStatus(
    com.example.CivicConnect.entity.complaint.Complaint complaint,
    ApprovalStatus status
);
}