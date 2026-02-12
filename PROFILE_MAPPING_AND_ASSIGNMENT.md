# 🗺️ CivicConnect: Profile Mapping & Assignment Guide

This guide explains how to integrate the newly added profile coordinates and the assignment logic into your frontend map and dashboard.

---

## 1. 📍 User Profile Mapping
Officers and Citizens now have `latitude` and `longitude` fields in their profiles. This allows you to visualize the city's resources and demographic heatmaps.

### **A. Officers on Map**
To show officers (Live positions or Base stations):
- **API**: `GET /api/map/officers`
- **Frontend Action**: Iterate through the list. If `latitude` and `longitude` are present, place a **Blue Shield** icon (Ward Officer) or a **Blue Briefcase** icon (Dept Officer).
- **Tooltips**: Include the officer's `name`, `department`, and `activeComplaintsCount`.

### **B. Citizens on Map (Admin Only)**
To see where your citizens are located (Reporting hotspots):
- **API**: `GET /api/map/admin/citizens`
- **Frontend Action**: Use a **Gray User** icon or a simple dot. 
- **Analytics**: Use this data to generate a heatmap showing which areas have the highest density of registered citizens vs. actual complaints.

---

## 2. 🎯 The "Assignment Flow" (Ward Officer)
The Ward Officer is the "Tactical Commander" who assigns field tasks.

### **Step 1: Identifying Needs**
Ward Officers should filter the map for `status=SUBMITTED` (Unassigned). These markers will appear **Red** 🔴.

### **Step 2: Selection & Popup**
When a Ward Officer clicks a Red Marker:
1. Show the `ComplaintDetail` popup.
2. If `assignedOfficerName` is null, show an **"🚀 ASSIGN TASK"** button.

### **Step 3: Finding Available Personnel**
Clicking "Assign Task" should:
1. Open a sidebar/drawer listing officers from `GET /api/map/officers`.
2. Sort them by `activeComplaintsCount` (lowest first).
3. (Optional) Highlight officers who are geographically closest to the marker using the `latitude`/`longitude` in the officer list.

### **Step 4: Executing the API**
Once an officer is selected, call the Assignment API:
- **URL**: `PUT /api/ward-officer/complaints/{complaintId}/assign`
- **Body**: `{ "officerId": 123 }`
- **Result**: The marker on the map will instantly change from **Red** 🔴 to **Yellow** 🟡 (Assigned).

---

## 📦 Enhanced DTO Structure for Map
The backend now returns:
```json
{
  "userId": 105,
  "name": "Arjun Sharma",
  "role": "DEPARTMENT_OFFICER",
  "latitude": 18.5204, 
  "longitude": 73.8567,
  "activeComplaintsCount": 4
}
```

---

## 💡 Pro Tip for Frontend
Use **Leaflet Marker Clustering** for the citizen map to avoid cluttering, and use a **Pulsing Animation** for the officer markers to signify they are "on duty".
