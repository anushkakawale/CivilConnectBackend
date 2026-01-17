package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Duplicate check (already correct)
    Optional<Complaint>
    findByWard_WardIdAndDepartment_DepartmentIdAndTitleIgnoreCaseAndCreatedAtAfter(
            Long wardId,
            Long departmentId,
            String title,
            LocalDateTime createdAt
    );

    //  FIXED citizen tracking
    List<Complaint>
    findByCitizen_UserIdOrderByCreatedAtDesc(Long citizenUserId);
    
    List<Complaint> findByWard_WardIdAndDepartment_DepartmentIdAndStatus(
            Long wardId,
            Long departmentId,
            ComplaintStatus status
    );

}
