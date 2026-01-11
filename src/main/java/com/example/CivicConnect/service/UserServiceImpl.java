//package com.example.CivicConnect.service;
//
//import java.util.EnumSet;
//
//import org.springframework.stereotype.Service;
//
//import com.example.CivicConnect.dto.UserRequestDTO;
//import com.example.CivicConnect.dto.UserResponseDTO;
//import com.example.CivicConnect.entity.core.User;
//import com.example.CivicConnect.entity.enums.RoleName;
//import com.example.CivicConnect.exception.DuplicateResourceException;
//import com.example.CivicConnect.exception.InvalidRoleException;
//import com.example.CivicConnect.repository.UserRepository;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class UserServiceImpl implements UserService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserResponseDTO register(UserRequestDTO dto) {
//
//        if (!EnumSet.allOf(RoleName.class).contains(dto.getRole())) {
//            throw new InvalidRoleException("Invalid role");
//        }
//
//        if (userRepository.existsByEmail(dto.getEmail())) {
//            throw new DuplicateResourceException("Email already exists");
//        }
//
//        if (userRepository.existsByMobile(dto.getMobile())) {
//            throw new DuplicateResourceException("Mobile already exists");
//        }
//
//        User user = User.builder()
//                .name(dto.getName())
//                .email(dto.getEmail())
//                .mobile(dto.getMobile())
//                .password(dto.getPassword()) // plain for now
//                .role(dto.getRole())
//                .active(true)
//                .build();
//
//        User saved = userRepository.save(user);
//
//        return UserResponseDTO.builder()
//                .userId(saved.getUserId())
//                .name(saved.getName())
//                .email(saved.getEmail())
//                .mobile(saved.getMobile())
//                .role(saved.getRole())
//                .active(saved.isActive())
//                .createdAt(saved.getCreatedAt())
//                .build();
//    }
//}
