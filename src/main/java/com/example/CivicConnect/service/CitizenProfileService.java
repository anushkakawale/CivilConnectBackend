package com.example.CivicConnect.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.CitizenProfileResponseDTO;
import com.example.CivicConnect.dto.CitizenProfileUpdateDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.profiles.WardChangeRequest;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.WardChangeRequestRepository;
import com.example.CivicConnect.repository.WardRepository;
@Service
@Transactional
public class CitizenProfileService {

    private final CitizenProfileRepository citizenProfileRepository;
    private final WardRepository wardRepository;
    private final WardChangeRequestRepository wardChangeRequestRepository;
    private final NotificationService notificationService;

    public CitizenProfileService(
            CitizenProfileRepository citizenProfileRepository,
            WardRepository wardRepository,
            WardChangeRequestRepository wardChangeRequestRepository,
            NotificationService notificationService) {

        this.citizenProfileRepository = citizenProfileRepository;
        this.wardRepository = wardRepository;
        this.wardChangeRequestRepository = wardChangeRequestRepository;
        this.notificationService = notificationService;
    }

    // ==============================
    // VIEW PROFILE
    // ==============================
    public CitizenProfileResponseDTO getCitizenProfile(User user) {

        CitizenProfile profile =
                citizenProfileRepository.findByUser_UserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Profile not found"));

        return new CitizenProfileResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                profile.getWard().getWardId(),
                profile.getWard().getAreaName()
        );
    }

    // ==============================
    // UPDATE PROFILE (CITIZEN ONLY)
    // ==============================
    public void updateProfile(Long userId, CitizenProfileUpdateDTO dto) {

        CitizenProfile profile =
                citizenProfileRepository.findByUser_UserId(userId)
                        .orElseThrow(() -> new RuntimeException("Profile not found"));

        User user = profile.getUser();
        boolean updated = false;

        // ✅ NAME UPDATE (IMMEDIATE)
        if (dto.getName() != null &&
            !dto.getName().equals(user.getName())) {

            user.setName(dto.getName());
            updated = true;
        }

        // ❗ WARD CHANGE → REQUEST ONLY
        if (dto.getWardId() != null &&
            !dto.getWardId().equals(profile.getWard().getWardId())) {

            Ward requestedWard = wardRepository.findById(dto.getWardId())
                    .orElseThrow(() -> new RuntimeException("Ward not found"));

            WardChangeRequest request = new WardChangeRequest();
            request.setCitizen(user);
            request.setOldWard(profile.getWard());
            request.setRequestedWard(requestedWard);

            wardChangeRequestRepository.save(request);

            // 🔔 Notify ward officer of NEW ward
            notificationService.notifyWardOfficer(
                    requestedWard.getWardId(),
                    "New ward change request from citizen " + user.getName(),
                    null
            );

            updated = true;
        }

        if (updated) {
            notificationService.notifyUser(
                    user,
                    "Profile updated successfully. Ward change requires approval (if requested)."
            );
        }
    }
}
