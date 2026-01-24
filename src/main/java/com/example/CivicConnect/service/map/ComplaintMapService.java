package com.example.CivicConnect.service.map;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ComplaintMapDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.repository.ComplaintRepository;

@Service
public class ComplaintMapService {

    private final ComplaintRepository complaintRepository;

    public ComplaintMapService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    // =========================
    // 🏘️ WARD MAP (Citizen / Ward Officer)
    // =========================
    public List<ComplaintMapDTO> wardMap(Long wardId) {

        return complaintRepository
                .findByWard_WardId(wardId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================
    // 🏙️ CITY MAP (Admin)
    // =========================
    public List<ComplaintMapDTO> cityMap() {

        return complaintRepository
                .findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================
    // 🏢 DEPARTMENT MAP (Dept Officer)
    // =========================
    public List<ComplaintMapDTO> departmentMap(
            Long wardId,
            Long departmentId) {

        return complaintRepository
                .findByWard_WardIdAndDepartment_DepartmentId(
                        wardId,
                        departmentId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================
    // 🔄 COMMON MAPPER
    // =========================
    private ComplaintMapDTO toDto(Complaint c) {

        return new ComplaintMapDTO(
                c.getComplaintId(),
                c.getLatitude(),
                c.getLongitude(),
                c.getStatus()
        );
    }
}
	