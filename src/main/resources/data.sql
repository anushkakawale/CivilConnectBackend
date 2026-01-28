-- ========================================
-- CIVIC CONNECT - MASTER DATA INITIALIZATION
-- ========================================
-- This file is automatically executed on application startup
-- INSERT IGNORE ensures no duplicates are created on restart

-- 🏘️ WARDS (Pune City Areas)
INSERT IGNORE INTO wards (ward_number, area_name)
VALUES
('1', 'Shivaji Nagar'),
('2', 'Kothrud'),
('3', 'Hadapsar'),
('4', 'Baner'),
('5', 'Kasba Peth');

-- 🏢 DEPARTMENTS (Municipal Services)
INSERT IGNORE INTO departments (name, sla_hours, priority_level, description)
VALUES
('Water Supply', 24, 'HIGH', 'No water, leakage, low pressure'),
('Sanitation', 36, 'MEDIUM', 'Public toilets, cleanliness'),
('Roads', 72, 'LOW', 'Potholes, damaged roads'),
('Electricity', 24, 'HIGH', 'Street lights, power issues'),
('Waste Management', 12, 'CRITICAL', 'Garbage collection'),
('Public Safety', 6, 'CRITICAL', 'Open manholes, hazards'),
('Health', 48, 'MEDIUM', 'Mosquitoes, hygiene'),
('Education', 96, 'LOW', 'School infrastructure');

