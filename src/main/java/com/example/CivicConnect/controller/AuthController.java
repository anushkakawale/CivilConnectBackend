package com.example.CivicConnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.LoginRequestDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.service.JWTService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
            Map.of(
                "token", token,
                "role", user.getRole().name(),
                "userId", user.getUserId(),
                "name", user.getName()
            )
        );
    }
}


//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final AuthenticationManager authenticationManager;
//    private final JWTService jwtService;
//
//    public AuthController(
//            AuthenticationManager authenticationManager,
//            JWTService jwtService) {
//        this.authenticationManager = authenticationManager;
//        this.jwtService = jwtService;
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
//
//        Authentication authentication =
//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                request.getEmail(),
//                                request.getPassword()
//                        )
//                );
//
//        User user = (User) authentication.getPrincipal();
//        String token = jwtService.generateToken(user);
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "token", token,
//                        "role", user.getRole().name()
//                )
//        );
//    }
//}


//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final AuthService authService;
//
//    public AuthController(AuthService authService) {
//        this.authService = authService;
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponseDTO> login(
//            @RequestBody @Valid LoginRequestDTO dto) {
//        return ResponseEntity.ok(authService.login(dto));
//    }
//}
