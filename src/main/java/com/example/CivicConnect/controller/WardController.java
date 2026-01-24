package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.repository.WardRepository;

@RestController
@RequestMapping("/api/wards")
public class WardController {

    private final WardRepository wardRepository;

    public WardController(WardRepository wardRepository) {
        this.wardRepository = wardRepository;
    }

    @GetMapping
    public List<Ward> getAllWards() {
        return wardRepository.findAll();
    }
}
