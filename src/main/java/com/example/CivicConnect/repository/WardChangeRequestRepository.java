package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;

public interface WardChangeRequestRepository
        extends JpaRepository<WardChangeRequest, Long> {
	/*
    // Find all pending requests for a ward
    List<WardChangeRequest> findByRequestedWardAndStatusOrderByRequestedAtDesc(Ward ward, ApprovalStatus status);

    // Find all requests by citizen
    List<WardChangeRequest> findByCitizenOrderByRequestedAtDesc(User citizen);

    // Find pending request for a citizen
    @Query("SELECT w FROM WardChangeRequest w WHERE w.citizen = :citizen AND w.status = 'PENDING'")
    List<WardChangeRequest> findPendingRequestsByCitizen(@Param("citizen") User citizen);

    // Count pending requests for a ward
    long countByRequestedWardAndStatus(Ward ward, ApprovalStatus status);
    
    // Find all pending requests (for admin)
    List<WardChangeRequest> findByStatusOrderByRequestedAtDesc(ApprovalStatus status);
   
 // Citizen: check pending request
    boolean existsByCitizenAndStatus(User citizen, ApprovalStatus status);

    // ✅ FIXED METHOD (if you had this)
    List<WardChangeRequest>
        findByStatusAndRequestedWard_WardId(
            ApprovalStatus status,
            Long wardId
        );
    */
	boolean existsByCitizenAndStatus(User citizen, ApprovalStatus status);

    List<WardChangeRequest> findByCitizenOrderByRequestedAtDesc(User citizen);

    List<WardChangeRequest>
        findByRequestedWardAndStatusOrderByRequestedAtDesc(
            Ward ward,
            ApprovalStatus status
        );
}
