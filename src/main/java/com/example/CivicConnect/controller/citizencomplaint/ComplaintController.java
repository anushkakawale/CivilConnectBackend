package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintRequestDTO;
import com.example.CivicConnect.dto.ComplaintResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

@RestController
@RequestMapping("/api/citizens/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserRepository userRepository;

    public ComplaintController(
            ComplaintService complaintService,
            UserRepository userRepository) {
        this.complaintService = complaintService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ComplaintResponseDTO> registerComplaint(
            @RequestBody ComplaintRequestDTO request,
            Authentication auth) {

        String email = auth.getName(); // JWT subject

        User citizen = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        return ResponseEntity.ok(
                complaintService.registerComplaint(request, citizen)
        );
    }

}
