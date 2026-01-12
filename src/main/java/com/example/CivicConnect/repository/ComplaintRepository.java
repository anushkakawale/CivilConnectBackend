package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Duplicate check (already correct)
    Optional<Complaint>
    findByWard_WardIdAndDepartment_DepartmentIdAndTitleIgnoreCaseAndCreatedAtAfter(
            Long wardId,
            Long departmentId,
            String title,
            LocalDateTime createdAt
    );

    // ✅ FIXED citizen tracking
    List<Complaint>
    findByCitizen_UserIdOrderByCreatedAtDesc(Long citizenUserId);
}
