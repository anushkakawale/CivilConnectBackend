package com.example.CivicConnect.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.NotificationRepository;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public void notifyCitizen(User citizen, String title, String message, Long complaintId) {

		Notification n = new Notification();
		n.setUser(citizen);
		n.setMessage("[" + title + "] " + message);
		n.setSeen(false);
		n.setCreatedAt(LocalDateTime.now());
		n.setReferenceId(complaintId); // ADD THIS FIELD

		notificationRepository.save(n);
	}

	public void notifyOfficer(User officer, String message, Long complaintId) {

		Notification n = new Notification();
		n.setUser(officer);
		n.setMessage(message);
		n.setSeen(false);
		n.setCreatedAt(LocalDateTime.now());
		n.setReferenceId(complaintId);

		notificationRepository.save(n);
	}

	public void notifyUser(User user, String message) {

		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(message);
		notification.setSeen(false);
		notification.setCreatedAt(LocalDateTime.now());

		notificationRepository.save(notification);
	}

	// OPTIONAL: SYSTEM / ADMIN MESSAGE
	// ==============================
	public void notifySystem(String message) {

		Notification notification = new Notification();
		notification.setUser(null); // system-level
		notification.setMessage(message);
		notification.setSeen(false);
		notification.setCreatedAt(LocalDateTime.now());

		notificationRepository.save(notification);
	}

	public void notifyWardOfficer(Long wardId, String message, Long referenceId) {

		// You can improve this later to fetch all ward officers
		Notification notification = new Notification();
		notification.setMessage(message);
		notification.setSeen(false);
		notification.setCreatedAt(LocalDateTime.now());
		notification.setReferenceId(referenceId);

		notificationRepository.save(notification);
	}

}
