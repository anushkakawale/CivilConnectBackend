# CivicConnect Department Officer API Integration Guide

This guide details the API Endpoints and Data Structures for the Department Officer Dashboard.

## 🔑 Authentication
- **Header:** `Authorization: Bearer <token>`
- **Role Required:** `DEPARTMENT_OFFICER`

---

## 1. 📊 Enhanced Dashboard Analytics
**Page:** `ProfessionalDepartmentOfficerDashboard.jsx`

### Endpoint: Get Dashboard Data
- **URL:** `/api/department/analytics/dashboard`
- **Method:** `GET`
- **Response Structure (Updated):**
```json
{
    "officerName": "River",
    "department": "Electricity",
    "ward": "Kasba Peth",
    "wardOfficer": {
        "name": "Ward Admin John",
        "email": "john@civicconnect.gov",
        "mobile": "9999999999"
    },
    "statistics": {
        "totalAssigned": 15,
        "pending": 5,
        "inProgress": 3,
        "resolved": 4,
        "approved": 2,
        "closed": 1,
        "completionRate": "20.0%",
        "avgResolutionTimeHours": "12.5"
    },
    "sla": {
        "breached": 2,
        "warning": 3,
        "onTrack": 10
    },
    "recentActivity": {
        "last7Days": 4,
        "resolvedLast7Days": 1
    }
}
```

---

## 2. 📋 Assigned Work
**Page:** `DepartmentComplaints.jsx`

### Endpoint: Get Assigned Complaints (Paginated)
- **URL:** `/api/department/dashboard/assigned?page=0&size=20`
- **Method:** `GET`
- **Response Structure:**
```json
{
    "content": [
        {
            "complaintId": 105,
            "title": "Short circuit in area 4",
            "status": "ASSIGNED",
            "createdAt": "2026-02-01T01:00:00"
        }
    ],
    "totalPages": 2,
    "totalElements": 25
}
```

---

## 3. 🗺️ Map Integration
**Page:** `DepartmentMap.jsx`

### Endpoint: Get Pending Work for Map
- **URL:** `/api/department/analytics/pending-work`
- **Method:** `GET`
- **Response Structure:**
```json
{
    "count": 8,
    "complaints": [
        {
            "complaintId": 105,
            "title": "Short circuit",
            "status": "ASSIGNED",
            "priority": "HIGH",
            "slaDeadline": "2026-02-02T10:00:00",
            "SLAStatus": "ON_TRACK",
            "daysOpen": 0
        }
    ]
}
```

---

## 🛠️ Frontend Troubleshooting (Common Errors)

### 1. "apiService.departmentOfficer.getMyComplaints is not a function"
**Cause:** Your `apiService.js` is missing this method or the object is undefined in `DepartmentMap.jsx`.
**Fix:** 
In `apiService.js`, add:
```javascript
export const departmentOfficer = {
    getMyComplaints: () => api.get('/department/analytics/pending-work'),
    getDashboard: () => api.get('/department/analytics/dashboard'),
    // ... other methods
};
```
In `DepartmentMap.jsx`, ensure you use:
```javascript
const response = await apiService.departmentOfficer.getMyComplaints();
// Access data as response.data.complaints
```

### 2. "The width(-1) and height(-1) of chart should be greater than 0"
**Cause:** Recharts is trying to render before the parent container has a height.
**Fix:**
Wrap your `ResponsiveContainer` in a fixed-height div:
```jsx
<div style={{ width: '100%', height: 300 }}>
    <ResponsiveContainer>
        <BarChart ... />
    </ResponsiveContainer>
</div>
```

---

## 🚀 Pro Tips for "The Best" Dashboard
1.  **Ward Officer Contact**: Display the `wardOfficer` card proudly in the sidebar so the Dept Officer can easily call/email them for coordination.
2.  **SLA Badges**: Use the `SLAStatus` from `pending-work` to highlight "BREACHED" complaints in Red and "WARNING" in Yellow on the map.
3.  **Real-time Feed**: The `recentActivity` object can be used to show a "Last 7 Days" summary card.
