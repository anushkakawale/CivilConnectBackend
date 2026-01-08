package com.example.CivicConnect.service;

import com.example.CivicConnect.dto.UserRequestDTO;
import com.example.CivicConnect.dto.UserResponseDTO;

public interface UserService {
	UserResponseDTO register(UserRequestDTO requestDTO);
}
