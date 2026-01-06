package com.example.CivicConnect.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Ward {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wardId;

    private String wardNumber;
    
    private String areaName;
}
