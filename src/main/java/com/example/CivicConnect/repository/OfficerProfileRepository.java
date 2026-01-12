package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.profiles.OfficerProfile;

public interface OfficerProfileRepository
        extends JpaRepository<OfficerProfile, Long> {

    Optional<OfficerProfile>
    findFirstByWard_WardIdAndDepartment_DepartmentIdAndActiveTrueOrderByActiveComplaintCountAsc(
            Long wardId,
            Long departmentId
    );
    //Ward officer = role = WARD_OFFICER
    //Ward Officer lookup (if department officer of that ward is not present)
    Optional<OfficerProfile>
    findFirstByWard_WardIdAndUser_RoleAndActiveTrue(
            Long wardId,
            RoleName role
    );

}
