package com.example.CivicConnect.service.citizen;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.WardChangeRequestRepository;
import com.example.CivicConnect.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WardChangeApprovalService {

    private final WardChangeRequestRepository requestRepo;
    private final CitizenProfileRepository citizenProfileRepo;
    private final NotificationService notificationService;

    public WardChangeApprovalService(
            WardChangeRequestRepository requestRepo,
            CitizenProfileRepository citizenProfileRepo,
            NotificationService notificationService) {

        this.requestRepo = requestRepo;
        this.citizenProfileRepo = citizenProfileRepo;
        this.notificationService = notificationService;
    }

    public void approveWardChange(Long requestId, User officer) {

        WardChangeRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Ward change request not found"));

        CitizenProfile profile = citizenProfileRepo
                .findByUser_UserId(request.getCitizen().getUserId())
                .orElseThrow();

        request.setStatus(ApprovalStatus.APPROVED);
        request.setDecidedAt(LocalDateTime.now());

        profile.setWard(request.getRequestedWard());

        requestRepo.save(request);
        citizenProfileRepo.save(profile);

        notificationService.notifyCitizen(
                request.getCitizen(),
                "Ward Change Approved",
                "Your ward has been updated successfully",
                null
        );
    }
}
