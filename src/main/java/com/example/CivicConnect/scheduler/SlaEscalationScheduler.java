package com.example.CivicConnect.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlaEscalationScheduler {

    private final ComplaintSlaRepository slaRepository;
    private final NotificationService notificationService;

    // ⏱ Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void checkSlaStatus() {

        // 1️⃣ Fetch all ACTIVE SLAs
    	List<ComplaintSla> activeSlas =
    	        slaRepository.findByStatus(SLAStatus.ACTIVE);

        for (ComplaintSla sla : activeSlas) {

            LocalDateTime now = LocalDateTime.now();

            // =====================================================
            // ⚠ SLA WARNING (2 HOURS LEFT)
            // =====================================================
            if (!sla.isEscalated()
                && sla.getSlaDeadline().minusHours(2).isBefore(now)
                && sla.getSlaDeadline().isAfter(now)) {

                if (sla.getComplaint().getAssignedOfficer() != null) {
                    notificationService.notifyOfficer(
                            sla.getComplaint().getAssignedOfficer(),
                            "⏳ SLA expiring soon for Complaint ID "
                                    + sla.getComplaint().getComplaintId(),
                            sla.getComplaint().getComplaintId()
                    );
                }
            }

            // =====================================================
            // 🚨 SLA BREACHED
            // =====================================================
            if (sla.getSlaDeadline().isBefore(now) && !sla.isEscalated()) {

                sla.setStatus(SLAStatus.BREACHED);
                sla.setEscalated(true);

                // Notify citizen
                notificationService.notifyCitizen(
                        sla.getComplaint().getCitizen(),
                        "SLA Breached",
                        "SLA breached for Complaint ID "
                                + sla.getComplaint().getComplaintId(),
                        sla.getComplaint().getComplaintId()
                );

                // Notify assigned officer
                if (sla.getComplaint().getAssignedOfficer() != null) {
                    notificationService.notifyOfficer(
                            sla.getComplaint().getAssignedOfficer(),
                            "🚨 SLA breached for Complaint ID "
                                    + sla.getComplaint().getComplaintId(),
                            sla.getComplaint().getComplaintId()
                    );
                }

                slaRepository.save(sla);
            }
        }
    }
}
