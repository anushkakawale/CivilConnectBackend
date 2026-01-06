package com.example.CivicConnect.entity.hierarchy;

import java.time.LocalDateTime;

import com.example.CivicConnect.entity.core.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "officer_hierarchy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hierarchyId;

    @ManyToOne
    @JoinColumn(name = "parent_officer_id", nullable = false)
    private User wardOfficer;

    @ManyToOne
    @JoinColumn(name = "child_officer_id", nullable = false)
    private User departmentOfficer;

    private LocalDateTime createdAt = LocalDateTime.now();
}