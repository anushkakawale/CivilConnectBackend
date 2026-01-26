package com.example.CivicConnect.service.citizencomplaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitizenWardComplaintService {

    private final ComplaintRepository complaintRepository;

    public Page<ComplaintSummaryDTO> getWardComplaints(
            Long wardId,
            Long departmentId,
            Pageable pageable) {

        Page<Complaint> page;

        if (departmentId == null) {
            page = complaintRepository.findByWard_WardId(
                    wardId,
                    pageable
            );
        } else {
            page = complaintRepository.findByWard_WardIdAndDepartment_DepartmentId(
                    wardId,
                    departmentId,
                    pageable
            );
        }

        // Convert Entity → DTO
        return page.map(c ->
                new ComplaintSummaryDTO(
                        c.getComplaintId(),
                        c.getTitle(),
                        c.getStatus(),
                        c.getCreatedAt()
                )
        );
    }
}