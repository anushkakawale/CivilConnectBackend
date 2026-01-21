//package com.example.CivicConnect.controller;
//
//import java.util.List;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import com.example.CivicConnect.entity.core.User;
//import com.example.CivicConnect.entity.profiles.CitizenProfile;
//import com.example.CivicConnect.repository.CitizenProfileRepository;

//@RestController
//@RequestMapping("/api/admin")
//public class AdminController {
//
//    private final CitizenProfileRepository citizenProfileRepository;
//
//    public AdminController(CitizenProfileRepository citizenProfileRepository) {
//        this.citizenProfileRepository = citizenProfileRepository;
//    }
//
//    // ✅ Get all citizens (admin only)
//    @GetMapping("/citizens")
//    public ResponseEntity<List<CitizenProfile>> getAllCitizens(@AuthenticationPrincipal User admin) {
//        return ResponseEntity.ok(citizenProfileRepository.findAll());
//    }
//
//    // ✅ Example: Get single citizen by ID
//    @GetMapping("/citizens/{id}")
//    public ResponseEntity<CitizenProfile> getCitizenById(
//            @PathVariable Long id,
//            @AuthenticationPrincipal User admin
//    ) {
//        return citizenProfileRepository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // You can add more admin-specific actions here (approve profiles, manage wards, etc.)
//}
