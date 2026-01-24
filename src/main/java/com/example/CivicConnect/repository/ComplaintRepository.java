package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    // SLA ESCALATION QUERY
    List<Complaint> findBySlaDeadlineBeforeAndStatusNotAndEscalatedFalse(
            LocalDateTime now,
            ComplaintStatus status
    );
	List<Complaint> findByStatus(ComplaintStatus approved);
	long countBySlaBreachedTrue();

	long countByDepartment_DepartmentId(Long departmentId);

	long countByDepartment_DepartmentIdAndSlaBreachedTrue(Long departmentId);
	
	List<Complaint> findByWard_WardIdOrderByCreatedAtDesc(Long wardId);

    List<Complaint> findByWard_WardIdAndStatus(
            Long wardId,
            ComplaintStatus status
    );

    List<Complaint> findByWard_WardIdAndDepartment_DepartmentId(
            Long wardId,
            Long departmentId
    );
    //for map
    List<Complaint> findByWard_WardId(Long wardId);

    List<Complaint> findByDepartment_DepartmentId(Long deptId);

    List<Complaint> findAll(); // for admin city view

    //for admin - AdminComplaintQueryController.java
    List<Complaint> findAllByOrderByCreatedAtDesc();

//    List<Complaint> findByWard_WardId(Long wardId);
//
//    List<Complaint> findByDepartment_DepartmentId(Long departmentId);
//
//    List<Complaint> findByStatus(ComplaintStatus status);
    @Query("""
    		SELECT c.ward.areaName, COUNT(c)
    		FROM Complaint c
    		GROUP BY c.ward.areaName
    		""")
    		List<Object[]> countByWard();

    		@Query("""
    		SELECT c.department.name, COUNT(c)
    		FROM Complaint c
    		GROUP BY c.department.name
    		""")
    		List<Object[]> countByDepartment();

}
