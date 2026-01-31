package com.example.CivicConnect.entity.profiles;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "officer_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long officerProfileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = true)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @ManyToOne(optional = true)
    @JoinColumn(name = "ward_id", nullable = true)
    private Ward ward;

    private String designation;
    
    private String employeeId;

    private boolean active = true;

    private int activeComplaintCount = 0;
}