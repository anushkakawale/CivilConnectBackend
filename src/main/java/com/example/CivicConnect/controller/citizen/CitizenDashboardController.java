package com.example.CivicConnect.controller.citizen;

import java.util.List;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.citizen.CitizenComplaintDTO;
import com.example.CivicConnect.dto.citizen.CitizenDashboardSummaryDTO;
import com.example.CivicConnect.dto.citizen.NotificationDTO;
import com.example.CivicConnect.dto.citizen.WardComplaintDTO;
//import com.example.CivicConnect.service.citizen.CitizenDashboardService;

import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/citizen/dashboard")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('CITIZEN')")
//public class CitizenDashboardController {
//
//    private final CitizenDashboardService service;
//
//    @GetMapping("/summary")
//    public CitizenDashboardSummaryDTO summary(Authentication auth) {
//        return service.getSummary(auth.getName());
//    }
//
//    @GetMapping("/complaints")
//    public List<CitizenComplaintDTO> myComplaints(Authentication auth) {
//        return service.getMyComplaints(auth.getName());
//    }
//
//    @GetMapping("/ward-complaints")
//    public List<WardComplaintDTO> wardComplaints(Authentication auth) {
//        return service.getWardComplaints(auth.getName());
//    }
//
//    @GetMapping("/notifications")
//    public List<NotificationDTO> notifications(Authentication auth) {
//        return service.getNotifications(auth.getName());
//    }
//}
