package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> getAll(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return notificationRepository
            .findByUserOrderByCreatedAtDesc(user);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return Map.of(
            "unreadCount",
            notificationRepository.countByUserAndIsReadFalse(user)
        );
    }

    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Notification n = notificationRepository.findById(id)
            .orElseThrow();

        if (!n.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Unauthorized");

        n.setRead(true);
        notificationRepository.save(n);
    }

    @PutMapping("/read-all")
    public void readAll(Authentication auth) {
        User user = (User) auth.getPrincipal();
        notificationRepository
            .findByUserAndIsReadFalse(user)
            .forEach(n -> n.setRead(true));
    }
}