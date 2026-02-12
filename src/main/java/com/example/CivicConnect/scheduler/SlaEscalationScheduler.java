package com.example.CivicConnect.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.service.NotificationService;

@Component
public class SlaEscalationScheduler {

    private final ComplaintSlaRepository slaRepository;
    private final NotificationService notificationService;
    private final com.example.CivicConnect.repository.ComplaintRepository complaintRepository;

    public SlaEscalationScheduler(
            ComplaintSlaRepository slaRepository, 
            NotificationService notificationService,
            com.example.CivicConnect.repository.ComplaintRepository complaintRepository) {
        this.slaRepository = slaRepository;
        this.notificationService = notificationService;
        this.complaintRepository = complaintRepository;
    }

    //  Runs every 5 minutes to update SLA status
    @Scheduled(fixedRate = 300000)
    public void checkSlaStatus() {

        // 1️⃣ Fetch all active or warning SLAs (not MET or BREACHED)
    	List<ComplaintSla> activeSlas =
    	        slaRepository.findByStatusIn(List.of(SLAStatus.ON_TRACK, SLAStatus.WARNING, SLAStatus.ACTIVE));

        for (ComplaintSla sla : activeSlas) {

            LocalDateTime now = LocalDateTime.now();
            boolean changed = false;

            // =====================================================
            // 🚨 SLA BREACHED (Deadline Passed)
            // =====================================================
            if (sla.getSlaDeadline().isBefore(now)) {
                
                sla.setStatus(SLAStatus.BREACHED);
                sla.setSlaBreached(true);
                sla.setEscalated(true);
                changed = true;
                
                // Sync with Complaint entity
                sla.getComplaint().setSlaBreached(true);
                sla.getComplaint().setEscalated(true);

                // Notify citizen
                notificationService.notifyCitizen(
                	    sla.getComplaint().getCitizen(),
                	    "SLA Breached",
                	    "Resolution deadline missed for complaint #" + sla.getComplaint().getComplaintId() + ". Administrators have been notified.",
                	    sla.getComplaint().getComplaintId(),
                	    NotificationType.SLA_BREACHED
                	);

                // Notify assigned officer
                if (sla.getComplaint().getAssignedOfficer() != null) {
                    notificationService.notifyOfficer(
                    	    sla.getComplaint().getAssignedOfficer(),
                    	    "SLA Breached",
                    	    "🚨 DEADLINE MISSED: Complaint #"
                    	        + sla.getComplaint().getComplaintId() + " is now in breach.",
                    	    sla.getComplaint().getComplaintId(),
                    	    NotificationType.SLA_BREACHED
                    	);
                }
            }
            // =====================================================
            // ⚠️ SLA WARNING (2 HOURS LEFT)
            // =====================================================
            else if (sla.getStatus() != SLAStatus.WARNING 
                    && sla.getSlaDeadline().minusHours(2).isBefore(now)) {

            	sla.setStatus(SLAStatus.WARNING);
            	changed = true;

            	// ⏳ SLA WARNING (2 hours before deadline)
            	if (sla.getComplaint().getAssignedOfficer() != null) {
                	notificationService.notifyOfficer(
                	    sla.getComplaint().getAssignedOfficer(),
                	    "SLA Warning",
                	    "⏳ 2 Hours Remaining: Complaint #"
                	        + sla.getComplaint().getComplaintId() + " is due soon.",
                	    sla.getComplaint().getComplaintId(),
                	    NotificationType.SLA_WARNING
                	);
            	}
            }

            if (changed) {
                slaRepository.save(sla);
                complaintRepository.save(sla.getComplaint()); // 💾 Save the complaint entity too
            }
        }
    }
}
