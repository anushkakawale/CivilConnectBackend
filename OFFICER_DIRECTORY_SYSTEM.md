# 📋 CivicConnect Personnel & Officer Directory API Reference

This document provides a unified reference for fetching user and officer rosters across all roles. These APIs power the **Personnel Matrix** and **Officer Directory** features.

---

## 1. 🛡️ Role-Based Officer Directory
Used by Citizens and Officers to find colleagues or responsible authorities.
**Base Path:** `/api/officers`

| Endpoint | Method | Role | Response Type | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/citizen/ward` | `GET` | `CITIZEN` | `List<OfficerDirectoryDTO>` | All officers (Ward + Dept) assigned to the citizen's ward. |
| `/ward-officer/dept-officers` | `GET` | `WARD_OFFICER` | `List<OfficerDirectoryDTO>` | All Department Officers working under the Ward Officer. |
| `/department-officer/peers` | `GET` | `DEPT_OFFICER` | `List<OfficerDirectoryDTO>` | Colleagues in the **same Department** and **same Ward**. |
| `/admin/all` | `GET` | `ADMIN` | `List<OfficerDirectoryDTO>` | System-wide roster of every officer. |

---

## 2. 🏛️ Administrative User Management
Used by Admins for high-level system oversight.
**Base Path:** `/api/admin`

| Endpoint | Method | Role | Response Type | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/users` | `GET` | `ADMIN` | `Page<UserDTO>` | Paginated list of every user (Citizens + Officers) in the system. |
| `/officers` | `GET` | `ADMIN` | `List<AdminOfficerDTO>` | Detailed officer list with mobile, email, and activity status. |

---

## 3. 🗺️ Map Integration (Live Directory)
Used to overlay officer locations and workloads on the city map.
**Base Path:** `/api/map`

| Endpoint | Method | Role | Description |
| :--- | :--- | :--- | :--- |
| `/officers` | `GET` | All roles | Returns names, contact, and `activeComplaintsCount` for the map sidebar. |

---

## 📦 Data Structures (DTOs)

### `OfficerDirectoryDTO`
```json
{
  "userId": 105,
  "name": "Arjun Sharma",
  "mobile": "9876543210",
  "email": "arjun.s@corp.pune.gov.in",
  "role": "DEPARTMENT_OFFICER",
  "department": "Water Supply",
  "wardNumber": "Ward 4 (Baner)",
  "activeComplaintsCount": 12,
  "lastLoginAt": "2026-02-11T14:30:00"
}
```

### `UserDTO`
```json
{
  "userId": 1001,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "CITIZEN",
  "active": true,
  "wardName": "Kothrud",
  "departmentName": "N/A"
}
```

---

## 🚀 Frontend Integration Strategy
1. **Dynamic Tab Filtering**: Use the `role` field from the response to group personnel into "Management" (Ward Officers) and "Field Units" (Dept Officers).
2. **Real-time Search**: Implement client-side filtering on the `name` or `department` fields.
3. **Personnel Matrix UI**: Use the `activeComplaintsCount` to show a "Workload Bar" (High/Medium/Low) next to each officer.
