package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
	boolean existsByEmail(String email);

	boolean existsByMobile(String mobile);

}
