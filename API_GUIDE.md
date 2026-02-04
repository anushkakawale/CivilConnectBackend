# CivicConnect Backend API Guide [V2.0 - PREMIUM]

This is the definitive guide for frontend developers to integrate with the CivicConnect ecosystem. All endpoints respond with JSON unless specified.

## 🔌 Connection Details
- **Base URL:** `http://localhost:8083/api`
- **Auth Pattern:** Bearer Token (`Authorization: Bearer <token>`)

---

## 🏗️ SYSTEM ARCHITECTURE & FLOW (THE "BLUEPRINT")

1.  **CITIZEN**: 
    - Registers -> Sets Ward in Profile -> **Registers Complaint**.
    - If status is `RESOLVED` or `CLOSED`, they have a **7-Day window** to **REOPEN** if not satisfied.
2.  **SYSTEM**: 
    - Uses **Smart Assignment** to find the Department Officer with the lowest workload in the respective Ward/Department.
3.  **DEPARTMENT OFFICER**: 
    - Sees `ASSIGNED` tasks -> Changes to `IN_PROGRESS` -> Resolves as `RESOLVED`.
4.  **WARD OFFICER**: 
    - Reviews `RESOLVED` complaints.
    - **APPROVE**: Moves to `APPROVED` (Ready for closure).
    - **REJECT**: Moves back to `IN_PROGRESS` (Sends back to Dept Officer).
5.  **ADMIN**: 
    - Reviews `APPROVED` complaints -> **CLOSE**.
    - Manages users, departments, and views global analytics.
6.  **SLA ENGINE**: 
    - Automatically tracks deadlines. Breached complaints are flagged for priority attention.

---

## 🔐 AUTHENTICATION
| Method | Endpoint | Body | Description |
| :--- | :--- | :--- | :--- |
| **POST** | `/auth/login` | `{email, password}` | Returns `{token, role, userId, name}` |
| **POST** | `/auth/register` | `{name, email, password, role, wardId}` | Register as CITIZEN |

---

## 👤 CITIZEN APIs (`/api/citizens/complaints`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/` | Register Complaint (supports multipart images) |
| **GET** | `/` | View list of MY complaints (paginated) |
| **GET** | `/{id}` | **TRACKING**: Detailed view with status history & images |
| **PUT** | `/{id}/reopen` | **SATISFACTION**: Reopen within 7 days. Body: `{remarks}` |

---

## 🏘️ WARD OFFICER APIs (`/api/ward-officer`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/complaints/all` | View ALL complaints registered in their Ward |
| **GET** | `/complaints/{id}` | Detailed view of any complaint in their Ward |
| **PUT** | `/complaints/{id}/approve` | Approve a Resolved complaint |
| **PUT** | `/complaints/{id}/reject` | Reject a Resolved complaint (back to Officer) |
| **PUT** | `/complaints/{id}/assign` | Re-assign a complaint to another officer |

---

## 🏗️ DEPARTMENT OFFICER APIs (`/api/department/complaints`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/` | View list of complaints assigned to THEM |
| **GET** | `/{id}` | Detailed view of an assigned complaint |
| **PUT** | `/{id}/start` | Mark task as `IN_PROGRESS` |
| **PUT** | `/{id}/resolve` | Mark task as `RESOLVED` (Ready for review) |

---

## 🛡️ ADMIN APIs (`/api/admin`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/analytics/dashboard` | **PREMIUM**: Overall stats, SLA breaches, charts data |
| **GET** | `/complaints` | Global view of ALL complaints in the city |
| **GET** | `/complaints/{id}` | Audit any complaint in the system |
| **PUT** | `/complaints/{id}/close` | Permanently CLOSE an Approved complaint |
| **GET** | `/reports/complaints/pdf` | Export complaints report with dates (from/to) |

---

## 🔔 COMMON / PROFILE
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/profile` | Get account details |
| **GET** | `/notifications` | Fetch real-time status updates/assignments |
| **GET** | `/wards` | List all available Wards for registration |

---

## 📚 STATUS LIFECYCLE
`SUBMITTED` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `APPROVED` ➔ `CLOSED`
*(At any point after RESOLVED/CLOSED, Citizen can trigger `REOPENED`)*

## 💡 FRONTEND IMPLEMENTATION TIPS
1. **Details View**: Always use the specific `/{id}` endpoint to get images and status history timeline.
2. **Badge Colors**: 
   - `RESOLVED`: Green
   - `IN_PROGRESS`: Blue
   - `REOPENED/BREACHED`: Red
   - `SUBMITTED`: Gray
3. **Satisfaction Window**: On the frontend, only show the "Reopen" button if `status` is `RESOLVED` or `CLOSED` AND `updatedAt/closedAt` is less than 7 days old.
