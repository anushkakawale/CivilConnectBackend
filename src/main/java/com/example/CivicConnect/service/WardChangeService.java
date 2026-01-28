package com.example.CivicConnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.repository.WardChangeRequestRepository;
import com.example.CivicConnect.repository.WardRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WardChangeService {

    private final WardChangeRequestRepository wardChangeRequestRepository;
    private final WardRepository wardRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ===============================
    // CREATE WARD CHANGE REQUEST
    // ===============================
    public WardChangeRequest createWardChangeRequest(User citizen, Long newWardId) {
        // Check for pending requests
        List<WardChangeRequest> pendingRequests = wardChangeRequestRepository
                .findPendingRequestsByCitizen(citizen);

        if (!pendingRequests.isEmpty()) {
            throw new RuntimeException("You already have a pending ward change request");
        }

        // Get new ward
        Ward newWard = wardRepository.findById(newWardId)
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        // Get citizen's current ward (from CitizenProfile)
        Ward oldWard = null; // You'll need to fetch from CitizenProfile

        // Create request
        WardChangeRequest request = WardChangeRequest.builder()
                .citizen(citizen)
                .oldWard(oldWard)
                .requestedWard(newWard)
                .status(ApprovalStatus.PENDING)
                .build();

        wardChangeRequestRepository.save(request);

        // Notify citizen
        notificationService.notifyUser(
                citizen,
                "Ward Change Request Submitted",
                "Your ward change request to Ward #" + newWard.getWardNumber() + " has been submitted for approval."
        );

        // Notify ward officer of new ward
        notificationService.notifyWardOfficer(
            newWard.getWardId(),
            "Ward Change Request", 
            "New ward change request from citizen " + citizen.getName(),
            request.getRequestId(),
            com.example.CivicConnect.entity.enums.NotificationType.WARD_CHANGE
        );

        return request;
    }

    // ===============================
    // APPROVE WARD CHANGE
    // ===============================
    public void approveWardChange(Long requestId, User wardOfficer, String remarks) {
        WardChangeRequest request = wardChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        // Update request
        request.setStatus(ApprovalStatus.APPROVED);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(wardOfficer);
        request.setRemarks(remarks);
        wardChangeRequestRepository.save(request);

        // Update citizen's ward in CitizenProfile
        // TODO: Update CitizenProfile with new ward

        // Notify citizen
        notificationService.notifyUser(
                request.getCitizen(),
                "Ward Change Approved",
                "Your ward change request has been approved. You are now in Ward #" + 
                request.getRequestedWard().getWardNumber()
        );
    }

    // ===============================
    // REJECT WARD CHANGE
    // ===============================
    public void rejectWardChange(Long requestId, User wardOfficer, String remarks) {
        WardChangeRequest request = wardChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        // Update request
        request.setStatus(ApprovalStatus.REJECTED);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(wardOfficer);
        request.setRemarks(remarks);
        wardChangeRequestRepository.save(request);

        // Notify citizen
        notificationService.notifyUser(
                request.getCitizen(),
                "Ward Change Rejected",
                "Your ward change request has been rejected. Reason: " + remarks
        );
    }

    // ===============================
    // GET PENDING REQUESTS FOR WARD
    // ===============================
    public List<WardChangeRequest> getPendingRequestsForWard(Ward ward) {
        return wardChangeRequestRepository.findByRequestedWardAndStatusOrderByRequestedAtDesc(
                ward, ApprovalStatus.PENDING);
    }

    // ===============================
    // GET CITIZEN'S REQUESTS
    // ===============================
    public List<WardChangeRequest> getCitizenRequests(User citizen) {
        return wardChangeRequestRepository.findByCitizenOrderByRequestedAtDesc(citizen);
    }
}
