package com.example.CivicConnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    // ✅ NEW: Paginated version for DepartmentDashboardService
    Page<Complaint> findByAssignedOfficer_UserIdAndStatusIn(
            Long userId,
            List<ComplaintStatus> statuses,
            Pageable pageable
    );
    
    // ✅ NEW: Paginated search method for GlobalSearchService
    @Query("""
            SELECT c FROM Complaint c
            WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.citizen.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.department.name) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Complaint> search(@Param("query") String query, Pageable pageable);
    
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
	
	// ✅ NEW: countByWard_WardId for analytics
	//long countByWard_WardId(Long wardId);

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
    		
    		List<Complaint> findByWard_WardIdAndDepartment_DepartmentIdOrderByCreatedAtDesc(
    		        Long wardId,
    		        Long departmentId
    		);
    		Page<Complaint> findByWard_WardId(
    		        Long wardId,
    		        Pageable pageable
    		);

    		Page<Complaint> findByWard_WardIdAndDepartment_DepartmentId(
    		        Long wardId,
    		        Long departmentId,
    		        Pageable pageable
    		);
    		@Query("""
    				SELECT c FROM Complaint c
    				WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%'))
    				   OR LOWER(c.citizen.name) LIKE LOWER(CONCAT('%', :q, '%'))
    				   OR LOWER(c.department.name) LIKE LOWER(CONCAT('%', :q, '%'))
    				""")
    				List<Complaint> search(@Param("q") String q);
    		Page<Complaint> findByCitizen_UserIdOrderByCreatedAtDesc(
    		        Long userId,
    		        Pageable pageable
    		);
    		Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);
    // Search methods for GlobalSearchService
    List<Complaint> findByWard_WardIdAndTitleContainingIgnoreCase(Long wardId, String query);
    
    List<Complaint> findByDepartment_DepartmentIdAndTitleContainingIgnoreCase(Long departmentId, String query);
    
    List<Complaint> findByWard_WardIdAndDepartment_DepartmentIdAndTitleContainingIgnoreCase(
            Long wardId, Long departmentId, String query);
    
    List<Complaint> findByAssignedOfficer_UserIdAndTitleContainingIgnoreCase(Long officerId, String query);
    
    List<Complaint> findByCitizen_UserIdAndTitleContainingIgnoreCase(Long citizenId, String query);
    
    // Paginated versions
    Page<Complaint> findByWard_WardIdAndTitleContainingIgnoreCase(Long wardId, String query, Pageable pageable);
    
    Page<Complaint> findByDepartment_DepartmentIdAndTitleContainingIgnoreCase(Long departmentId, String query, Pageable pageable);
    
    Page<Complaint> findByWard_WardIdAndDepartment_DepartmentIdAndTitleContainingIgnoreCase(
            Long wardId, Long departmentId, String query, Pageable pageable);
    
    Page<Complaint> findByAssignedOfficer_UserIdAndTitleContainingIgnoreCase(Long officerId, String query, Pageable pageable);
    
    Page<Complaint> findByCitizen_UserIdAndTitleContainingIgnoreCase(Long citizenId, String query, Pageable pageable);
    
    // Analytics queries for WardOfficerAnalyticsService
    @Query("""
            SELECT c.department.name, COUNT(c)
            FROM Complaint c
            WHERE c.ward.wardId = :wardId
            GROUP BY c.department.name
            """)
    List<Object[]> countByWard_WardIdGroupByDepartment(@Param("wardId") Long wardId);
    
    long countByWard_WardId(Long wardId);
    
    long countByWard_WardIdAndSlaBreachedTrue(Long wardId);
    
    long countByWard_WardIdAndStatus(Long wardId, ComplaintStatus status);
    
    long countByAssignedOfficer_UserIdAndStatusIn(Long officerId, List<ComplaintStatus> statuses);

}
