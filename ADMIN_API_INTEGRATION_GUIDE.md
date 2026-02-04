# CivicConnect Admin API Integration Guide

This guide details the exact API Endpoints and Data Structures required to build the Admin Dashboard.

## 🔑 Authentication
All Admin endpoints require the `Authorization` header.
- **Header:** `Authorization: Bearer <your_jwt_token>`
- **Role Required:** `ADMIN`

---

## 1. 📊 Admin Dashboard (Analytics)
**Page:** `AdminDashboard.jsx`

### Endpoint: Get Analytics
- **URL:** `/api/admin/analytics/dashboard`
- **Method:** `GET`
- **Response Example:**
```json
{
    "overallStatistics": {
        "totalComplaints": 120,
        "assigned": 10,
        "inProgress": 35,
        "resolved": 40,
        "firstLevel": 0,
        "approved": 5,
        "closed": 30,
        "rejected": 5,
        "avgComplaintsPerWard": "40.00"
    },
    "slaStatistics": {
        "breached": 15,
        "warning": 5,
        "onTrack": 100,
        "total": 120
    },
    "resources": {
        "totalWards": 3,
        "totalDepartments": 4,
        "wardOfficers": 2,
        "departmentOfficers": 3,
        "totalUsers": 15
    }
}
```

---

## 2. 👥 User Management
**Page:** `AdminUserManagement.jsx`

### Endpoint: Get All Users
- **URL:** `/api/admin/users?page=0&size=20`
- **Method:** `GET`
- **Response Example:**
```json
{
    "content": [
        {
            "userId": 1,
            "name": "John Citizen",
            "email": "john@example.com",
            "mobile": "9876543210",
            "role": "CITIZEN",
            "active": true
        },
        {
            "userId": 2,
            "name": "Ward Officer 1",
            "email": "wo1@example.com",
            "mobile": "9870000001",
            "role": "WARD_OFFICER",
            "active": true
        }
    ],
    "totalPages": 5,
    "totalElements": 95,
    "last": false
}
```

### Endpoint: Deactivate User (Self/Target)
- **URL:** `/api/users/deactivate`
- **Method:** `PUT`

---

## 3. 📝 Complaint Management
**Page:** `AdminComplaints.jsx`

### Endpoint: Get All Complaints
- **URL:** `/api/admin/complaints?page=0&size=20`
- **Method:** `GET`
- **Response Example:**
```json
{
    "data": [
        {
            "complaintId": 101,
            "title": "Broken Streetlight",
            "status": "IN_PROGRESS",
            "ward": "Ward 1 - Downtown",
            "department": "Electricity",
            "createdAt": "2026-01-20T10:00:00"
        }
    ],
    "total": 50
}
```

### Endpoint: Get Pending Closure Queue
- **URL:** `/api/admin/complaints/pending-closure?page=0&size=20`
- **Method:** `GET`
- **Description:** Returns complaints with status `APPROVED` waiting for final closure.
- **Response:** Same structure as "Get All Complaints".

### Endpoint: Final Close Complaint
- **URL:** `/api/admin/complaints/{id}/close`
- **Method:** `PUT`
- **Description:** Marks the complaint as `CLOSED`.

---

## 4. 👮‍♂️ Officers Directory
**Page:** `AdminOfficers.jsx`

### Endpoint: Get All Officers
- **URL:** `/api/admin/officers`
- **Method:** `GET`
- **Response Example:**
```json
[
    {
        "officerId": 5,
        "name": "Jane Officer",
        "email": "jane@civic.com",
        "mobile": "9988776655",
        "role": "WARD_OFFICER",
        "wardName": "Ward 1 - Downtown",
        "departmentName": null,
        "active": true
    },
    {
        "officerId": 6,
        "name": "Bob Tech",
        "email": "bob@civic.com",
        "mobile": "9988776644",
        "role": "DEPARTMENT_OFFICER",
        "wardName": "Ward 1 - Downtown",
        "departmentName": "Roads",
        "active": true
    }
]
```

### Endpoint: Register Ward Officer
- **URL:** `/api/admin/register/ward-officer`
- **Method:** `POST`
- **Body:**
```json
{
  "name": "New Officer",
  "email": "new.officer@civic.com",
  "mobile": "9876543210",
  "password": "TempPassword123!",
  "wardId": 1
}
```

---

## ✅ Frontend Integration Snippet (Axios)
Use this utility pattern in your Frontend `apiService.js`:

```javascript
// Generic fetcher
export const fetchAdminData = async (endpoint) => {
    try {
        const response = await axios.get(`/admin/${endpoint}`);
        return response.data;
    } catch (error) {
        console.error("API Error:", error);
        throw error;
    }
};

// Usage Examples
const loadDashboard = async () => {
    const data = await fetchAdminData('analytics/dashboard');
    // setStats(data);
};

const loadUsers = async () => {
    const data = await fetchAdminData('users?page=0&size=20');
    // setUsers(data.content);
};
```
