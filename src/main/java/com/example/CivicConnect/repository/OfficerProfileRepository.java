package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;

public interface OfficerProfileRepository
        extends JpaRepository<OfficerProfile, Long> {

    // 🔹 Used during complaint auto-assignment
    Optional<OfficerProfile>
    findFirstByWard_WardIdAndDepartment_DepartmentIdAndActiveTrueOrderByActiveComplaintCountAsc(
            Long wardId,
            Long departmentId
    );

    // 🔹 Used when no department officer exists
    Optional<OfficerProfile>
    findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
            Long wardId,
            RoleName role
    );

    // 🔹 REQUIRED FOR AUTHORIZATION (JWT → OfficerProfile)
    Optional<OfficerProfile> findByUser_UserId(Long userId);
}
