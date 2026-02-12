package com.example.CivicConnect.service.departmentcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.ImageStage;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintImageRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.service.FileStorageService;
import com.example.CivicConnect.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentComplaintService {

	private final ComplaintRepository complaintRepository;
	private final ComplaintStatusHistoryRepository historyRepository;
	private final ComplaintApprovalRepository approvalRepository;
	private final NotificationService notificationService;
	private final ComplaintSlaRepository slaRepository;
	private final FileStorageService fileStorageService;
	private final ComplaintImageRepository imageRepository;

	public DepartmentComplaintService(
	        ComplaintRepository complaintRepository,
	        ComplaintStatusHistoryRepository historyRepository,
	        ComplaintApprovalRepository approvalRepository,
	        NotificationService notificationService,
	        ComplaintSlaRepository slaRepository,
	        FileStorageService fileStorageService,
	        ComplaintImageRepository imageRepository) {

	    this.complaintRepository = complaintRepository;
	    this.historyRepository = historyRepository;
	    this.approvalRepository = approvalRepository;
	    this.notificationService = notificationService;
	    this.slaRepository = slaRepository;
	    this.fileStorageService = fileStorageService;
	    this.imageRepository = imageRepository;
	}

	// ▶ START WORK
	public void startWork(Long complaintId, User officer, String remarks) {

		Complaint complaint = getComplaint(complaintId);

		if (!officer.getUserId().equals(complaint.getAssignedOfficer().getUserId())) {
			throw new RuntimeException("You are not assigned");
		}

		if (complaint.getStatus() != ComplaintStatus.ASSIGNED) {
			throw new RuntimeException("Invalid state");
		}

		complaint.setStatus(ComplaintStatus.IN_PROGRESS);
		logStatus(complaint, ComplaintStatus.IN_PROGRESS, officer, remarks);

		notificationService.notifyCitizen(
		    complaint.getCitizen(),
		    "Work Started",
		    "Work has started on your complaint " + complaint.getComplaintId(),
		    complaint.getComplaintId(),
		    NotificationType.STATUS_UPDATE
		);

		// TODO: Re-enable when User has ward relationship
		notificationService.notifyWardOfficer(
			    complaint.getWard().getWardId(),
			    "Work Started",
			    "Complaint " + complaint.getComplaintId() + " work has started",
			    complaint.getComplaintId(),
			    NotificationType.STATUS_UPDATE
			);


	}

	// ▶ RESOLVE COMPLAINT
	// ▶ RESOLVE COMPLAINT
	public void resolve(Long complaintId, User officer, String remarks) {

	    Complaint complaint = getComplaint(complaintId);

	    // 🔒 SECURITY CHECK
	    if (!officer.getUserId().equals(complaint.getAssignedOfficer().getUserId())) {
			throw new RuntimeException("You are not assigned to this complaint");
		}

	    if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
	        throw new RuntimeException("Must be IN_PROGRESS");
	    }

	    // 1️⃣ Update complaint
	    complaint.setStatus(ComplaintStatus.RESOLVED);
	    logStatus(complaint, ComplaintStatus.RESOLVED, officer, remarks);

	    // 2️⃣ Stop SLA
	    slaRepository.findByComplaint(complaint).ifPresent(sla -> {
	        sla.setSlaEndTime(LocalDateTime.now());
	        if (sla.getStatus() != SLAStatus.BREACHED) {
	            sla.setStatus(SLAStatus.MET);
	        }
	        slaRepository.save(sla);
	    });

	    // 3️⃣ Create approval for Ward Officer
	    ComplaintApproval approval = new ComplaintApproval();
	    approval.setComplaint(complaint);
	    approval.setStatus(ApprovalStatus.PENDING);
	    approval.setRemarks(remarks); // Also store remarks in approval
	    approval.setRoleAtTime(RoleName.WARD_OFFICER);
	    approval.setDecidedAt(LocalDateTime.now());

	    approvalRepository.save(approval);

	    // 4️⃣ 🔔 Notify Ward Officer
	    // TODO: Re-enable when User has ward relationship
	    notificationService.notifyWardOfficer(
	    	    complaint.getWard().getWardId(),
	    	    "Approval Required",
	    	    "Complaint " + complaint.getComplaintId() + " resolved and awaiting approval",
	    	    complaint.getComplaintId(),
	    	    NotificationType.APPROVAL_REQUIRED
	    	);
	}


	private Complaint getComplaint(Long id) {
		return complaintRepository.findById(id).orElseThrow(() -> new RuntimeException("Complaint not found"));
	}

	private void logStatus(Complaint complaint, ComplaintStatus status, User user, String remarks) {

		ComplaintStatusHistory history = new ComplaintStatusHistory();
		history.setComplaint(complaint);
		history.setStatus(status);
		history.setChangedBy(user);
		history.setChangedAt(LocalDateTime.now());
		history.setRemarks(remarks);
		historyRepository.save(history);
	}

	// 📸 UPLOAD PROGRESS IMAGES (During IN_PROGRESS)
	public void uploadProgressImages(Long complaintId, User officer, MultipartFile[] images) {
		Complaint complaint = getComplaint(complaintId);

		// 🔒 SECURITY CHECK
		if (!officer.getUserId().equals(complaint.getAssignedOfficer().getUserId())) {
			throw new RuntimeException("You are not assigned to this complaint");
		}

		if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
			throw new RuntimeException("Complaint must be IN_PROGRESS to upload progress images");
		}

		if (images == null || images.length == 0) {
			throw new RuntimeException("No images provided");
		}

		// Store each image
		for (MultipartFile file : images) {
			if (!file.isEmpty()) {
				String fileName = fileStorageService.storeComplaintImage(file, complaint.getComplaintId());
				
				ComplaintImage img = new ComplaintImage();
				img.setComplaint(complaint);
				img.setImageUrl(fileName);
				img.setImageStage(ImageStage.IN_PROGRESS);
				img.setUploadedAt(LocalDateTime.now());
				img.setUploadedBy(officer);
				imageRepository.save(img);
			}
		}

		// Notify citizen
		notificationService.notifyCitizen(
			complaint.getCitizen(),
			"Work Progress Update",
			"Progress images have been uploaded for complaint #" + complaint.getComplaintId(),
			complaint.getComplaintId(),
			NotificationType.STATUS_UPDATE
		);
	}

	// 📸 UPLOAD RESOLUTION IMAGES (During resolve)
	public void uploadResolutionImages(Long complaintId, User officer, MultipartFile[] images) {
		Complaint complaint = getComplaint(complaintId);

		// 🔒 SECURITY CHECK
		if (!officer.getUserId().equals(complaint.getAssignedOfficer().getUserId())) {
			throw new RuntimeException("You are not assigned to this complaint");
		}

		if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS && complaint.getStatus() != ComplaintStatus.RESOLVED) {
			throw new RuntimeException("Complaint must be IN_PROGRESS or RESOLVED to upload resolution images");
		}

		if (images == null || images.length == 0) {
			throw new RuntimeException("No images provided");
		}

		// Store each image
		for (MultipartFile file : images) {
			if (!file.isEmpty()) {
				String fileName = fileStorageService.storeComplaintImage(file, complaint.getComplaintId());
				
				ComplaintImage img = new ComplaintImage();
				img.setComplaint(complaint);
				img.setImageUrl(fileName);
				img.setImageStage(ImageStage.AFTER_RESOLUTION);
				img.setUploadedAt(LocalDateTime.now());
				img.setUploadedBy(officer);
				imageRepository.save(img);
			}
		}

		// Notify citizen
		notificationService.notifyCitizen(
			complaint.getCitizen(),
			"Work Completed",
			"Resolution images have been uploaded for complaint #" + complaint.getComplaintId(),
			complaint.getComplaintId(),
			NotificationType.STATUS_UPDATE
		);
	}

	// 🔄 RESOLVE WITH IMAGES (Combined method)
	public void resolveWithImages(Long complaintId, User officer, MultipartFile[] images, String remarks) {
		// First upload the resolution images
		if (images != null && images.length > 0) {
			uploadResolutionImages(complaintId, officer, images);
		}
		
		// Then resolve the complaint
		resolve(complaintId, officer, remarks);
	}
}
