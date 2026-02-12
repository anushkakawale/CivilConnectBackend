package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.repository.WardRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardRepository wardRepository;

    @GetMapping
    public ResponseEntity<List<Ward>> getAllWards() {
        return ResponseEntity.ok(wardRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Ward> createWard(@RequestBody Ward ward) {
        return ResponseEntity.ok(wardRepository.save(ward));
    }
}
