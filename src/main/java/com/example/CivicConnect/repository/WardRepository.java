package com.example.CivicConnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.geography.Ward;

public interface WardRepository 
extends JpaRepository<Ward, Long> {
	Optional<Ward> findByWardNumber(String wardNumber);
}
