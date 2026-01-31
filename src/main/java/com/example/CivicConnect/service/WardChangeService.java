package com.example.CivicConnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.WardChangeRequestRepository;
import com.example.CivicConnect.repository.WardRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WardChangeService {

    private final WardChangeRequestRepository requestRepo;
    private final WardRepository wardRepo;
    private final CitizenProfileRepository profileRepo;
    private final NotificationService notificationService;

    // ===============================
    // CITIZEN → CREATE REQUEST
    // ===============================
    public void createWardChangeRequest(User citizen, Long wardId) {

        if (citizen.getRole() != RoleName.CITIZEN) {
            throw new RuntimeException("Only citizens can request ward change");
        }

        CitizenProfile profile = profileRepo
                .findByUser_UserId(citizen.getUserId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Ward newWard = wardRepo.findById(wardId)
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        // ✅ CASE 1: FIRST TIME WARD → DIRECT SET
        if (profile.getWard() == null) {

            profile.setWard(newWard);
            profileRepo.save(profile); // ⭐ THIS WAS MISSING

            notificationService.notifyUser(
                    citizen,
                    "Ward Added",
                    "Your ward has been set successfully"
            );
            return;
        }

        // ❌ Prevent duplicate pending requests
        if (requestRepo.existsByCitizenAndStatus(citizen, ApprovalStatus.PENDING)) {
            throw new RuntimeException("Pending ward change already exists");
        }

        // ✅ CASE 2: CHANGE WARD → APPROVAL
        WardChangeRequest request = WardChangeRequest.builder()
                .citizen(citizen)
                .oldWard(profile.getWard())
                .requestedWard(newWard)
                .status(ApprovalStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        requestRepo.save(request);

        notificationService.notifyUser(
                citizen,
                "Ward Change Requested",
                "Your ward change request is sent for approval"
        );

        notificationService.notifyWardOfficer(
                newWard.getWardId(),
                "Ward Change Request",
                "New ward change request from " + citizen.getName(),
                request.getRequestId(),
                NotificationType.WARD_CHANGE
        );
    }


    // ===============================
    // OFFICER → APPROVE
    // ===============================
    public void approveWardChange(Long requestId, User officer, String remarks) {

        WardChangeRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        CitizenProfile profile = profileRepo
                .findByUser_UserId(request.getCitizen().getUserId())
                .orElseThrow(() -> new RuntimeException("Citizen profile not found"));

        // ✅ THIS WAS MISSING EARLIER
        profile.setWard(request.getRequestedWard());
        profileRepo.save(profile);

        request.setStatus(ApprovalStatus.APPROVED);
        request.setDecidedBy(officer);
        request.setDecidedAt(LocalDateTime.now());
        request.setRemarks(remarks);

        requestRepo.save(request);

        notificationService.notifyUser(
                request.getCitizen(),
                "Ward Change Approved",
                "Your ward has been updated successfully"
        );
    }

    // ===============================
    // OFFICER → REJECT
    // ===============================
    public void rejectWardChange(Long requestId, User officer, String remarks) {

        WardChangeRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(ApprovalStatus.REJECTED);
        request.setDecidedBy(officer);
        request.setDecidedAt(LocalDateTime.now());
        request.setRemarks(remarks);

        requestRepo.save(request);

        notificationService.notifyUser(
                request.getCitizen(),
                "Ward Change Rejected",
                remarks
        );
    }

    public List<WardChangeRequest> getCitizenRequests(User citizen) {
        return requestRepo.findByCitizenOrderByRequestedAtDesc(citizen);
    }

    public List<WardChangeRequest> getPendingForWard(Ward ward) {
        return requestRepo
                .findByRequestedWardAndStatusOrderByRequestedAtDesc(
                        ward, ApprovalStatus.PENDING);
    }
}
