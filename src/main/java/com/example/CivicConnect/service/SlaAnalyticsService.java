package com.example.CivicConnect.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

@Service
public class SlaAnalyticsService {

    private final ComplaintSlaRepository slaRepository;

    public SlaAnalyticsService(ComplaintSlaRepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    // 🌍 Overall SLA report
    public Map<String, Object> overallReport() {

        long total = slaRepository.count();
        long breached = slaRepository.countByStatus(SLAStatus.BREACHED);
        long completed = slaRepository.countByStatus(SLAStatus.MET);
        long active = slaRepository.countByStatus(SLAStatus.ACTIVE);

        return Map.of(
                "totalSla", total,
                "active", active,
                "completed", completed,
                "breached", breached,
                "slaCompliancePercent",
                total == 0 ? 100 : (completed * 100 / total)
        );
    }

    // 🏢 Department-wise SLA report
    public Map<String, Object> departmentReport(Long departmentId) {

        long total =
                slaRepository.countByComplaint_Department_DepartmentId(departmentId);
        long breached =
                slaRepository.countByComplaint_Department_DepartmentIdAndStatus(
                        departmentId, SLAStatus.BREACHED);

        return Map.of(
                "departmentId", departmentId,
                "totalSla", total,
                "breached", breached,
                "slaCompliancePercent",
                total == 0 ? 100 : (100 - (breached * 100 / total))
        );
    }
}
