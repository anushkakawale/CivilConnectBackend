package com.example.CivicConnect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.OfficerDirectoryDTO;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfficerDirectoryService {

    private final UserRepository userRepository;

    // ===============================
    // GET ALL WARD OFFICERS
    // ===============================
    public List<OfficerDirectoryDTO> getAllWardOfficers() {
        return userRepository.findByRole(RoleName.WARD_OFFICER)
                .stream()
                .map(user -> new OfficerDirectoryDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getMobile(), // Swapped with email based on DTO order? Wait.
                        user.getEmail(),  
                        user.getRole().name(),
                        null, // Department
                        null  // Ward Number
                ))
                .collect(Collectors.toList());
    }

    // ===============================
    // GET ALL DEPARTMENT OFFICERS
    // ===============================
    public List<OfficerDirectoryDTO> getAllDepartmentOfficers() {
        return userRepository.findByRole(RoleName.DEPARTMENT_OFFICER)
                .stream()
                .map(user -> new OfficerDirectoryDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getMobile(),
                        user.getEmail(),
                        user.getRole().name(),
                        null,
                        null
                ))
                .collect(Collectors.toList());
    }

    // ===============================
    // GET OFFICERS BY WARD (for Ward Officer/Citizen)
    // ===============================
    public List<OfficerDirectoryDTO> getOfficersByWard(Long wardId) {
        // This requires OfficerProfile with ward relationship
        // For now, return all officers
        // TODO: Filter by ward when OfficerProfile is available
        return getAllDepartmentOfficers();
    }

    // ===============================
    // GET ALL OFFICERS (Admin view)
    // ===============================
    public List<OfficerDirectoryDTO> getAllOfficers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == RoleName.WARD_OFFICER || 
                               user.getRole() == RoleName.DEPARTMENT_OFFICER)
                .map(user -> new OfficerDirectoryDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getMobile(),
                        user.getEmail(),
                        user.getRole().name(),
                        null,
                        null
                ))
                .collect(Collectors.toList());
    }
}
