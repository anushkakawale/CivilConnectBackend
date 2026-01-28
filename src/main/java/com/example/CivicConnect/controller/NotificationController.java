package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.NotificationDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
            notificationService.getNotificationsForUser(user)
        );
    }
}