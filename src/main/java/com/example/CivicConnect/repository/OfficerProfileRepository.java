package com.example.CivicConnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;

public interface OfficerProfileRepository
        extends JpaRepository<OfficerProfile, Long> {

    // 🔹 Complaint auto-assignment
    Optional<OfficerProfile>
    findFirstByWard_WardIdAndDepartment_DepartmentIdAndActiveTrueOrderByActiveComplaintCountAsc(
            Long wardId,
            Long departmentId
    );

    // 🔹 Find ward officer
    Optional<OfficerProfile>
    findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
            Long wardId,
            RoleName role
    );

    // 🔹 Profile lookup
    Optional<OfficerProfile> findByUser_UserId(Long userId);
    
    //view all ward officers
    List<OfficerProfile> findByUser_Role(RoleName role);
    
    //for citizen officer directory
    List<OfficerProfile> findByWard_WardId(Long wardId);
    
    // Count officers by role
    long countByUser_Role(RoleName role);
    
    // Find officers by ward and role
    List<OfficerProfile> findByWard_WardIdAndUser_Role(Long wardId, RoleName role);
    
    // ✅ NEW: Methods for Citizen Officers Service
    List<OfficerProfile> findByDepartment_DepartmentIdAndActiveTrue(Long departmentId);
    
    List<OfficerProfile> findByWard_WardIdAndUser_RoleAndActiveTrue(Long wardId, RoleName role);
    
    List<OfficerProfile> findByActiveTrue();
    
    
}
