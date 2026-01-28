package com.example.CivicConnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;

public interface UserRepository extends JpaRepository<User, Long>{
    
	boolean existsByEmail(String email);

	boolean existsByMobile(String mobile);
	
	Optional<User> findFirstByRole(RoleName role); 

	Optional<User> findByEmail(String email);

    // 🔐 For Admin notifications
    List<User> findByRole(RoleName role);
    
    Optional<User> findByMobile(String mobile);
}
