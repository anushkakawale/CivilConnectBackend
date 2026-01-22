package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // -------------------------------------------------
    // DUPLICATE COMPLAINT CHECK
    // -------------------------------------------------
    Optional<Complaint>
    findByWard_WardIdAndDepartment_DepartmentIdAndTitleIgnoreCaseAndCreatedAtAfter(
            Long wardId,
            Long departmentId,
            String title,
            LocalDateTime createdAt
    );

    // -------------------------------------------------
    // CITIZEN COMPLAINT TRACKING
    // -------------------------------------------------
    List<Complaint>
    findByCitizen_UserIdOrderByCreatedAtDesc(Long citizenUserId);

    // -------------------------------------------------
    // AUTO-ASSIGNMENT & PENDING COMPLAINTS
    // -------------------------------------------------
    List<Complaint>
    findByWard_WardIdAndDepartment_DepartmentIdAndStatus(
            Long wardId,
            Long departmentId,
            ComplaintStatus status
    );
    
    List<Complaint> findByAssignedOfficer_UserIdAndStatusIn(
            Long userId,
            List<ComplaintStatus> statuses
    );

	List<Complaint> findByStatus(ComplaintStatus approved);
}
