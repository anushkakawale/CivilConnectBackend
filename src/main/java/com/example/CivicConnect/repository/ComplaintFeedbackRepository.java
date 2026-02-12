package com.example.CivicConnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.CivicConnect.entity.complaint.ComplaintFeedback;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, Long> {

    Optional<ComplaintFeedback> findByComplaint_ComplaintIdAndCitizen_UserId(Long complaintId, Long citizenId);

    List<ComplaintFeedback> findByComplaint_ComplaintIdOrderByCreatedAtDesc(Long complaintId);

    List<ComplaintFeedback> findByComplaint_Ward_WardIdOrderByCreatedAtDesc(Long wardId);

    boolean existsByComplaint_ComplaintIdAndCitizen_UserId(Long complaintId, Long userId);

    @Query("""
        SELECT AVG(f.rating)
        FROM ComplaintFeedback f
        WHERE f.complaint.ward.wardId = :wardId
    """)
    Double getAverageRatingByWard(@Param("wardId") Long wardId);

    @Query("""
        SELECT AVG(f.rating)
        FROM ComplaintFeedback f
        WHERE f.complaint.department.departmentId = :departmentId
    """)
    Double getAverageRatingByDepartment(@Param("departmentId") Long departmentId);

    long countByComplaint_Ward_WardId(Long wardId);

    long countByComplaint_Department_DepartmentId(Long departmentId);
}
