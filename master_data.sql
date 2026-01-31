-- CivicConnect Master Data SQL Script
-- Use this script to reset and populate Wards and Departments

USE civic_connect;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- CLEAR EXISTING DATA
-- ============================================
TRUNCATE TABLE wards;
TRUNCATE TABLE departments;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- WARDS DATA
-- ============================================
-- Inserting data as requested: ward_number, area_name
INSERT INTO wards (ward_number, area_name) VALUES 
('1', 'Shivaji Nagar'),
('2', 'Kothrud'),
('3', 'Hadapsar'),
('4', 'Baner'),
('5', 'Kasba Peth');

-- ============================================
-- DEPARTMENTS DATA
-- ============================================
-- Inserting data as requested: name, sla_hours, priority_level, description
INSERT INTO departments (name, sla_hours, priority_level, description) VALUES
('Water Supply', 24, 'HIGH', 'No water, leakage, low pressure'),
('Sanitation', 36, 'MEDIUM', 'Public toilets, cleanliness'),
('Roads', 72, 'LOW', 'Potholes, damaged roads'),
('Electricity', 24, 'HIGH', 'Street lights, power issues'),
('Waste Management', 12, 'CRITICAL', 'Garbage collection'),
('Public Safety', 6, 'CRITICAL', 'Open manholes, hazards'),
('Health', 48, 'MEDIUM', 'Mosquitoes, hygiene'),
('Education', 96, 'LOW', 'School infrastructure');

-- ============================================
-- VERIFICATION
-- ============================================
SELECT * FROM wards;
SELECT * FROM departments;
