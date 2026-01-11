package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.geography.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
