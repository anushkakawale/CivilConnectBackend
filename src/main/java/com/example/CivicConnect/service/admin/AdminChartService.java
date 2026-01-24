package com.example.CivicConnect.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ChartDTO;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

@Service
public class AdminChartService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintSlaRepository slaRepository;

    public AdminChartService(
            ComplaintRepository complaintRepository,
            ComplaintSlaRepository slaRepository) {
        this.complaintRepository = complaintRepository;
        this.slaRepository = slaRepository;
    }

    public List<ChartDTO> complaintsByWard() {
        return complaintRepository.countByWard()
                .stream()
                .map(r -> new ChartDTO(
                        (String) r[0],
                        (Long) r[1]
                ))
                .toList();
    }

    public List<ChartDTO> complaintsByDepartment() {
        return complaintRepository.countByDepartment()
                .stream()
                .map(r -> new ChartDTO(
                        (String) r[0],
                        (Long) r[1]
                ))
                .toList();
    }

    public List<ChartDTO> slaStats() {
        return slaRepository.slaStats()
                .stream()
                .map(r -> new ChartDTO(
                        r[0].toString(),
                        (Long) r[1]
                ))
                .toList();
    }
}
