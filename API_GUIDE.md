# CivicConnect Backend API Guide for Frontend

This guide provides the essential API details required to build the CivicConnect frontend.

## 🔌 Connection Details
- **Base URL:** `http://localhost:8083`
- **Context Path:** `/api` (e.g., `http://localhost:8083/api/auth/login`)
- **Authentication:** JWT (JSON Web Token) based.
  - Send the token in the header: `Authorization: Bearer <your_token>`

## 🔐 Authentication & Profile

### Login (All Users)
*   **POST** `/api/auth/login`
*   **Body:** `{ "email": "user@example.com", "password": "password" }`
*   **Response:** `{ "token": "...", "role": "CITIZEN", "userId": 1, "name": "John" }`
*   **Note:** Store the `token` and `role` to manage access control.

### Register (Citizen)
*   **POST** `/api/auth/register`
*   **Body:**
    ```json
    {
      "name": "Jane Doe",
      "email": "jane@example.com",
      "password": "Password123",
      "mobile": "9876543210",
      "wardId": 1,
      "role": "CITIZEN"
    }
    ```

### User Profile
*   **GET** `/api/profile` (Requires Token) - Get logged-in user details.
*   **PUT** `/api/profile/name` - Update name. Body: `{ "name": "New Name" }`
*   **PUT** `/api/profile/password` - Update password. Body: `{ "currentPassword": "...", "newPassword": "..." }`
*   **Mobile Update:**
    1.  **POST** `/api/profile/mobile/request-otp` - `{ "mobileNumber": "..." }`
    2.  **POST** `/api/profile/mobile/verify-otp` - `{ "otp": "...", "newMobileNumber": "..." }`

---

## 👤 Citizen Module (`/api/citizen`)

### Dashboard
*   **GET** `/api/citizen/dashboard`
*   **Response:** Stats (total, pending, resolved), recent complaints list.

### Complaints
*   **GET** `/api/citizen/complaints` - List my complaints.
*   **POST** `/api/citizen/complaints` - Create new complaint.
    *   **Content-Type:** `multipart/form-data`
    *   **Fields:** `title` (text), `description` (text), `category` (enum), `wardId` (int), `latitude` (double), `longitude` (double), `address` (text).
    *   **Fields:** `images` (List of Files).
*   **GET** `/api/citizen/complaints/{id}` - Get details.
*   **PUT** `/api/citizen/complaints/{id}/reopen` - Reopen a resolved complaint.
*   **POST** `/api/citizen/complaints/{id}/feedback` - Give feedback ratings.

### Map & Officers
*   **GET** `/api/citizen/area-complaints` - Get complaints nearby logged-in user.
*   **GET** `/api/citizen/officers/ward-officers` - List officers in my ward.

---

## 🏘️ Ward Officer Module (`/api/ward-officer`)

### Dashboard
*   **GET** `/api/ward-officer/dashboard` - Stats, pending approvals count.

### Management
*   **GET** `/api/ward-officer/dashboard/pending-approvals` - List complaints waiting for approval.
*   **PUT** `/api/ward-officer/complaints/{id}/approve`
    *   **Body:** `{ "priority": "HIGH", "departmentId": 2, "assignedOfficerId": 5 }`
*   **PUT** `/api/ward-officer/complaints/{id}/reject`
    *   **Body:** `{ "reason": "Not valid..." }`
*   **GET** `/api/ward-officer/complaints` - All complaints in the ward.

### Staffing
*   **POST** `/api/ward-officer/register/department-officer` - Create a new Dept Officer under this ward.
*   **GET** `/api/ward-officer/department-officers` - List all Dept Officers in this ward.

---

## 🏗️ Department Officer Module (`/api/department`)

### Dashboard
*   **GET** `/api/department/dashboard` - Workload stats.

### Complaints Workflow
*   **GET** `/api/department/complaints` - List assigned complaints.
*   **PUT** `/api/department/complaints/{id}/status`
    *   **Body:** `{ "status": "IN_PROGRESS" }`
*   **POST** `/api/department/complaints/{id}/update` - Add a progress text log/comment.
*   **PUT** `/api/department/complaints/{id}/resolve`
    *   **Content-Type:** `multipart/form-data`
    *   **Fields:** `resolutionNotes` (text), `completionImages` (files).

---

## 🛡️ Admin Module (`/api/admin`)

*   **GET** `/api/admin/dashboard` - System-wide stats.
*   **GET** `/api/admin/users` - Manage all users.
*   **GET** `/api/admin/complaints` - View all complaints (filters available).
*   **PUT** `/api/admin/complaints/{id}/escalate` - Manually escalate priority.

---

## 🔔 Notifications & Common (`/api/notifications`)

*   **GET** `/api/notifications/stats` (New!) - `{"unreadCount": 5, "unseenCount": 2}`. Poll this often.
*   **GET** `/api/notifications/unread` - List unread items.
*   **PUT** `/api/notifications/{id}/read` - Mark specific item as read.
*   **PUT** `/api/notifications/read-all` - Mark all as read.

---

## 📚 Enums & Constants

### ComplaintStatus
`PENDING`, `APPROVED`, `REJECTED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REOPENED`

### Priority
`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

### ComplaintCategory
`ROAD_MAINTENANCE`, `STREET_LIGHTING`, `GARBAGE_COLLECTION`, `WATER_SUPPLY`, `DRAINAGE`, `OTHER`

---

## 💡 Frontend Tips
1.  **Dates:** Backend sends ISO 8601 strings (e.g., `2023-10-25T14:30:00`). Parse them in JS.
2.  **Images:** Images are served via `/api/images/{complaintId}/{filename}`. The complaint object contains full URLs.
3.  **Errors:** Backend returns standard 4xx/5xx codes. Check `response.data.message` for user-friendly error text.
