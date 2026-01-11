package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.geography.Ward;

public interface WardRepository extends JpaRepository<Ward, Long> {
}
