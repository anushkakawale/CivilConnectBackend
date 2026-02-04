# CivicConnect Ward Officer API Integration Guide

This guide details the API Endpoints and Data Structures for the Ward Officer Dashboard.

## 🔑 Authentication
- **Header:** `Authorization: Bearer <token>`
- **Role Required:** `WARD_OFFICER`

---

## 1. 📊 Ward Analytics
**Page:** `WardDashboard.jsx`

### Endpoint: Get Ward Analytics
- **URL:** `/api/ward-officer/analytics/dashboard`
- **Method:** `GET`
- **Response Structure:**
```json
{
    "ward": "Ward 1 - Downtown",
    "officer": "Ward Officer 1",
    "cards": {
        "totalComplaints": 45,
        "pendingApproval": 5,
        "approved": 10,
        "inProgress": 15,
        "closed": 15
    },
    "sla": {
        "breached": 3,
        "onTrack": 42
    },
    "departmentPerformance": [
        {
            "department": "Roads & Traffic",
            "total": 12,
            "pending": 4,
            "resolved": 8,
            "completionRate": "67%"
        }
    ],
    "officerPerformance": [
        {
            "officerName": "Road Officer 1",
            "department": "Roads & Traffic",
            "totalAssigned": 8,
            "resolved": 6,
            "pending": 2
        }
    ],
    "recentActivity": {
        "last7Days": 5,
        "closedLast7Days": 2
    }
}
```

---

## 2. 📝 Ward Complaints (All)
**Page:** `WardComplaints.jsx`

### Endpoint: Get All Ward Complaints (Paginated)
- **URL:** `/api/ward-officer/complaints/all?page=0&size=10`
- **Method:** `GET`
- **Response Structure:**
```json
{
    "content": [
        {
            "complaintId": 101,
            "title": "Pothole fix needed",
            "status": "RESOLVED",
            "ward": "Ward 1 - Downtown",
            "department": "Roads & Traffic",
            "createdAt": "2026-01-28 14:30:00"
        }
    ],
    "totalPages": 5,
    "totalElements": 45
}
```

---

## 3. ✅ Complaint Decisions
**Actions:** Approve or Reject resolution

### Endpoint: Approve Resolution
- **URL:** `/api/ward-officer/complaints/{id}/approve`
- **Method:** `PUT`
- **Body:**
```json
{
    "remarks": "Verified. Work is satisfactory."
}
```

### Endpoint: Reject Resolution
- **URL:** `/api/ward-officer/complaints/{id}/reject`
- **Method:** `PUT`
- **Body:**
```json
{
    "remarks": "Work incomplete. Re-assigning to department."
}
```

---

## 4. 🏢 Department Officers
**Page:** `WardOfficerDirectory.jsx`

### Endpoint: List Department Officers in Ward
- **URL:** `/api/ward-officer/department-officers`
- **Method:** `GET`
- **Response Structure:**
```json
[
    {
        "userId": 5,
        "name": "Road Officer 1",
        "departmentName": "Roads & Traffic",
        "email": "ro1@civic.com",
        "mobile": "7777777701"
    }
]
```

---

## 5. 🏗️ Department Distribution (New structure)
**Page:** `WardAnalytics.jsx`

### Endpoint: Get Distribution
- **URL:** `/api/ward-officer/analytics/department-distribution`
- **Method:** `GET`
- **Response Structure (Fixed to Array):**
```json
[
    {
        "department": "Roads & Traffic",
        "stats": {
            "ASSIGNED": 5,
            "IN_PROGRESS": 3,
            "RESOLVED": 2
        }
    },
    {
        "department": "Water Supply",
        "stats": {
            "ASSIGNED": 2,
            "RESOLVED": 4
        }
    }
]
```

---

## 🚀 Frontend Integration Tip
For the charts (Department/Officer Performance), you can use the data directly from the `/dashboard` response. Since `departmentPerformance` and `department-distribution` are now **Arrays**, you can use `.map()`, `.find()`, and `.filter()` directly in React.

