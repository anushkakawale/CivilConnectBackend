//package com.example.CivicConnect.controller;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.CivicConnect.dto.UserRequestDTO;
//import com.example.CivicConnect.dto.UserResponseDTO;
//import com.example.CivicConnect.service.UserService;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    @PostMapping("/register")
//    public ResponseEntity<UserResponseDTO> register(
//            @Valid @RequestBody UserRequestDTO dto) {
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(userService.register(dto));
//    }
//}
