package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.geography.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
	


}
