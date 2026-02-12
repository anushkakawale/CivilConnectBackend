package com.example.CivicConnect.job;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.WardRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MasterDataSeeder implements CommandLineRunner {

    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        seedWards();
        seedDepartments();
    }

    private void seedWards() {
        // No longer returning if count > 0.
        // We will check individual records to ensure everything is present.

        saveWardIfMissing("1", "Shivaji Nagar", "411005", "Central Pune Area");
        saveWardIfMissing("2", "Kothrud", "411038", "Residential Zone");
        saveWardIfMissing("3", "Hinjewadi", "411057", "IT Hub");
        saveWardIfMissing("4", "Baner", "411045", "Commercial Area");
        saveWardIfMissing("5", "Hadapsar", "411028", "Industrial Area");
        saveWardIfMissing("6", "Kasba Peth", "411011", "Old City Area");
        
        System.out.println("✅ Wards Seeded (checked/updated)!");
    }



    private void saveWardIfMissing(String wardNumber, String areaName, String pincode, String description) {
        java.util.Optional<Ward> existing = wardRepository.findByWardNumber(wardNumber);
        
        if (existing.isPresent()) {
            // Update existing record
            Ward ward = existing.get();
            boolean changed = false;
            
            if (!areaName.equals(ward.getAreaName())) { ward.setAreaName(areaName); changed = true; }
            if (pincode != null && !pincode.equals(ward.getPincode())) { ward.setPincode(pincode); changed = true; }
            if (description != null && !description.equals(ward.getDescription())) { ward.setDescription(description); changed = true; }
            
            if (changed) {
                wardRepository.save(ward);
                System.out.println("🔄 Ward " + wardNumber + " updated.");
            }
        } else {
            // Create new record
            wardRepository.save(new Ward(null, wardNumber, areaName, pincode, null, description));
            System.out.println("✅ Ward " + wardNumber + " created.");
        }
    }

    private void seedDepartments() {
        saveDepartmentIfMissing("Water Supply", 24, "HIGH", "No water, leakage, low pressure");
        saveDepartmentIfMissing("Sanitation", 36, "MEDIUM", "Public toilets, cleanliness");
        saveDepartmentIfMissing("Roads", 72, "LOW", "Potholes, damaged roads");
        saveDepartmentIfMissing("Electricity", 24, "HIGH", "Street lights, power issues");
        saveDepartmentIfMissing("Waste Management", 12, "CRITICAL", "Garbage collection");
        saveDepartmentIfMissing("Public Safety", 6, "CRITICAL", "Open manholes, hazards");
        saveDepartmentIfMissing("Health", 48, "MEDIUM", "Mosquitoes, hygiene");
        saveDepartmentIfMissing("Education", 96, "LOW", "School infrastructure");
        
        System.out.println("✅ Departments Seeded (checked/updated)!");
    }

    private void saveDepartmentIfMissing(String name, int sla, String priority, String desc) {
        java.util.Optional<Department> existing = departmentRepository.findByName(name);
        
        if (existing.isPresent()) {
            // Update existing record
            Department dept = existing.get();
            boolean changed = false;
            
            if (dept.getSlaHours() != sla) { dept.setSlaHours(sla); changed = true; }
            if (priority != null && !priority.equals(dept.getPriorityLevel())) { dept.setPriorityLevel(priority); changed = true; }
            if (desc != null && !desc.equals(dept.getDescription())) { dept.setDescription(desc); changed = true; }
            
            if (changed) {
                departmentRepository.save(dept);
                System.out.println("🔄 Department " + name + " updated.");
            }
        } else {
            // Create new record
            departmentRepository.save(new Department(null, name, sla, priority, desc));
            System.out.println("✅ Department " + name + " created.");
        }
    }
}
