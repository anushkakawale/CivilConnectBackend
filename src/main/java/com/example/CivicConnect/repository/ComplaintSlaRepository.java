package com.example.CivicConnect.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.sla.ComplaintSla;

public interface ComplaintSlaRepository
        extends JpaRepository<ComplaintSla, Long> {

    Optional<ComplaintSla> findByComplaint_ComplaintId(Long complaintId);

    List<ComplaintSla> findBySlaDeadlineBeforeAndStatus(
            LocalDateTime time,
            SLAStatus status
    );
    long countByStatus(SLAStatus status);
    
    long countByComplaint_Department_DepartmentId(Long departmentId);

    long countByComplaint_Department_DepartmentIdAndStatus(
            Long departmentId,
            SLAStatus status
    );
    Optional<ComplaintSla>
    findByComplaint_ComplaintIdAndComplaint_Citizen_UserId(
        Long complaintId,
        Long citizenUserId
    );
    Optional<ComplaintSla> findByComplaint(Complaint complaint);
    List<ComplaintSla> findByStatus(SLAStatus status);
    @Query("""
    		SELECT s.status, COUNT(s)
    		FROM ComplaintSla s
    		GROUP BY s.status
    		""")
    		List<Object[]> slaStats();

}
