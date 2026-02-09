# CivicConnect Full API Reference

This document provides a comprehensive list of all API endpoints in the CivicConnect application, organized by Role and Module.

## 1. Authentication & Common
**Base URL**: `/api`

### Auth (`AuthController`)
*   `POST /api/auth/login` - Login with email/password. Returns JWT, Role, UserId.

### Common Profile (`ProfileController`)
*   `GET /api/profile` - View authenticated user's profile (All roles: data adapts to role).
*   `GET /api/profile/completion-score` - Get profile completion percentage.
*   `PUT /api/profile/name` - Update name.
*   `PUT /api/profile/password` - Change password.
*   `GET /api/profile/completion-score` - Get profile completion percentage.
*   `POST /api/profile/image` - Upload profile picture.

### Notifications (`NotificationController`)
*   `GET /api/notifications` - List all notifications.
*   `GET /api/notifications/unread` - List unread notifications.
*   `GET /api/notifications/unread/count` - Get unread count.
*   `GET /api/notifications/stats` - Get notification statistics.
*   `PUT /api/notifications/{id}/read` - Mark specific notification as read.
*   `PUT /api/notifications/mark-all-as-read` - Mark all as read.
*   `PUT /api/notifications/seen-all` - Mark all as seen (clears bell).
*   `DELETE /api/notifications/{id}` - Delete a notification.
*   `DELETE /api/notifications/clear-read` - Delete all read notifications.

### Global Search (`GlobalSearchController`)
*   `GET /api/search/complaints?query={q}` - Search complaints (Scope depends on Role).

### User Account (`UserAccountController`)
*   `PUT /api/users/deactivate` - Deactivate own account.
*   `PUT /api/users/deactivate/{userId}` - Admin: Deactivate specific user.

### Metadata (`DepartmentController`)
*   `GET /api/departments` - List all departments.

---

## 2. Admin
**Role**: `ADMIN`

### Dashboard (`AdminDashboardController`)
*   `GET /api/admin/dashboard/stats` - Overall system statistics.
*   `GET /api/admin/dashboard/ready-to-close` - List complaints approved by Ward Officer, ready for closure.

### Analytics (`AdminAnalyticsController`)
*   `GET /api/admin/analytics/dashboard` - Detailed analytics dashboard.
*   `GET /api/admin/analytics/officer-workload` - Officer workload stats.

### Map (`AdminMapController`)
*   `GET /api/admin/map/city` - City-wide heatmap data.
*   `GET /api/admin/map/data` - Map pins data.
*   `GET /api/admin/map/ward/{wardId}` - Specific ward map data.

### Officer Management (`AdminOfficerController`)
*   `POST /api/admin/register/ward-officer` - Register a new Ward Officer.
*   `POST /api/admin/register/department-officer` - Register a new Department Officer.
*   `GET /api/admin/officers` - List all officers.

### User Management (`AdminUserController`)
*   `GET /api/admin/users` - List all users (Paginated).

### Reports (`AdminReportController`)
*   `GET /api/admin/reports/summary` - Summary report.
*   `GET /api/admin/reports/complaints` - Complaints report.
*   `GET /api/admin/reports/sla` - SLA compliance report.
*   `GET /api/admin/reports/users` - User activity report.
*   `GET /api/admin/reports/complaints/pdf` - Download Compliants PDF.
*   `GET /api/admin/reports/complaints/excel` - Download Complaints Excel.

### Complaint Operations (`AdminComplaintController` - *Assumed path from structure*)
*   *(Note: Closure usually handled via specific endpoint, check implementation)*

---

## 3. Ward Officer
**Role**: `WARD_OFFICER`

### Dashboard (`WardOfficerDashboardController`)
*   `GET /api/ward-officer/dashboard/stats` - Statistics for the ward.
*   `GET /api/ward-officer/dashboard/pending-approvals` - List of RESOLVED complaints waiting for approval.
*   `GET /api/ward-officer/dashboard/complaints` - List of all ward complaints.

### Analytics (`WardOfficerAnalyticsController`)
*   `GET /api/ward-officer/analytics/dashboard` - Comprehensive ward analytics.
*   `GET /api/ward-officer/analytics/department-distribution` - Work distribution by dept.
*   `GET /api/ward-officer/analytics/monthly-trends` - Monthly registration vs closure trends.

### Complaint Management (`WardOfficerComplaintController`)
*   `GET /api/ward-officer/complaints/{id}` - View complaint details.
*   `GET /api/ward-officer/complaints/all` - Paginated list covering all ward complaints.
*   `PUT /api/ward-officer/complaints/{id}/approve` - Approve resolution (RESOLVED -> APPROVED).
*   `PUT /api/ward-officer/complaints/{id}/reject` - Reject resolution (RESOLVED -> IN_PROGRESS).
*   `PUT /api/ward-officer/complaints/{id}/assign` - Manually update officer assignment.

### Ward Management (`WardOfficerManagementController`)
*   `POST /api/ward-officer/management/register-officer` - Register Department Officer for *this* ward.
*   `GET /api/ward-officer/management/ward-changes` - View pending citizen ward change requests.
*   `PUT /api/ward-officer/management/ward-changes/{id}/approve` - Approve ward change.
*   `PUT /api/ward-officer/management/ward-changes/{id}/reject` - Reject ward change.
*   `GET /api/ward-officer/management/analytics` - Management specific analytics.
*   `GET /api/ward-officer/management/complaints` - Management complaint list.

### Officer Directory (`WardOfficerController`)
*   `GET /api/ward-officer/department-officers` - List Dept Officers in validity.

---

## 4. Department Officer
**Role**: `DEPARTMENT_OFFICER`

### Dashboard (`DepartmentDashboardController`)
*   `GET /api/department/dashboard/summary` - Officer's personal summary (Assigned, In Progress, SLA).
*   `GET /api/department/dashboard/assigned` - Paginated list of assigned work.
*   `GET /api/department/dashboard/peer-complaints` - List of complaints in same ward/dept not assigned to self.

### Analytics (`DepartmentAnalyticsController`)
*   `GET /api/department/analytics/dashboard` - Personal performance dashboard.
*   `GET /api/department/analytics/pending-work` - Detailed pending work list.
*   `GET /api/department/analytics/trends` - Personal monthly trends.
*   `GET /api/department/analytics/assigned` - Full assigned list (for registry view).

### Complaint Operations (`DepartmentComplaintController`)
*   `GET /api/department/complaints/assigned` - (Alias to dashboard/assigned).
*   `GET /api/department/complaints/peers` - (Alias to dashboard/peers).
*   `GET /api/department/complaints/{id}` - View details.
*   `PUT /api/department/complaints/{id}/start` - Mark as IN_PROGRESS.
*   `PUT /api/department/complaints/{id}/resolve` - Mark as RESOLVED (completes task).

### Map (`DepartmentMapController`)
*   `GET /api/department/map` - View map of complaints in assigned ward/dept.

### Officers (`DepartmentOfficerController`)
*   `GET /api/department/officers/ward-officer` - View supervising Ward Officer.
*   `GET /api/department/officers/colleagues` - View other officers in same dept/ward.

---

## 5. Citizen
**Role**: `CITIZEN`

### Dashboard (`CitizenDashboardController`)
*   `GET /api/citizen/dashboard` - Citizen dashboard data.

### Complaints (`CitizenComplaintController`)
*   `POST /api/citizens/complaints` - Register a new complaint.
*   `GET /api/citizens/complaints` - List my complaints (Paginated, Filters).
*   `GET /api/citizens/complaints/ward` - List complaints in my ward.
*   `GET /api/citizens/complaints/{id}` - Track complaint.
*   `GET /api/citizens/complaints/{id}/timeline` - detailed timeline.
*   `GET /api/citizens/complaints/{id}/sla` - SLA details.
*   `GET /api/citizens/complaints/{id}/sla/countdown` - SLA countdown.
*   `PUT /api/citizens/complaints/{id}/reopen` - Reopen a closed/resolved complaint.
*   `PUT /api/citizens/complaints/{id}/feedback` - Submit rating (1-5) & feedback for Resolved/Closed complaints.

### Profile (`CitizenProfileController`)
*   `GET /api/profile/citizen` - View profile with address.
*   `PUT /api/profile/citizen/address` - Update address.
*   `PUT /api/profile/citizen/ward` - Request ward change.

### Officers (`CitizenOfficerController`)
*   `GET /api/citizen/officers/ward-officer` - View my Ward Officer.
*   `GET /api/citizen/officers/department-officers` - View Dept Officers in my ward.
*   `GET /api/citizen/officers/all-department-officers` - View all officers (Directory).

---

## 6. Proper Application Flow

### A. Complaint Lifecycle
1.  **Creation**: Citizen creates complaint (`POST /api/citizens/complaints`). Status: `ASSIGNED` (Auto-assigned) or `OPEN`.
2.  **Assignment**:
    *   System tries to auto-assign to a Department Officer in that Ward & Dept.
    *   If no officer, Ward Officer can manually assign (`PUT /api/ward-officer/complaints/{id}/assign`).
3.  **In Progress**: Department Officer picks up task (`PUT /api/department/complaints/{id}/start`). Status: `IN_PROGRESS`.
4.  **Resolution**: Department Officer resolves task (`PUT /api/department/complaints/{id}/resolve`). Status: `RESOLVED`.
5.  **Approval (Quality Check)**:
    *   Ward Officer sees it in Pending Approvals (`GET /api/ward-officer/dashboard/pending-approvals`).
    *   Ward Officer approves (`PUT .../approve`) -> Status: `APPROVED`.
    *   OR Ward Officer rejects (`PUT .../reject`) -> Status: `IN_PROGRESS` (Back to Dept Officer).
6.  **Closure**:
    *   Admin sees approved complaints (`GET /api/admin/dashboard/ready-to-close`).
    *   Admin performs final closure.
    *   OR System auto-closes after X days (if implemented).

### B. User Registration Flow
1.  **Citizen**: Self-registration (`POST /api/auth/register`).
2.  **Ward Officer**: Registered by Admin (`POST /api/admin/register/ward-officer`).
3.  **Department Officer**:
    *   Registered by Admin (`POST /api/admin/register/department-officer`).
    *   OR Registered by Ward Officer for their specific ward (`POST /api/ward-officer/management/register-officer`).

### C. Ward Change Flow
1.  Citizen requests change (`PUT /api/profile/citizen/ward`).
2.  Status becomes `PENDING`.
3.  Target Ward Officer sees request (`GET /api/ward-officer/management/ward-changes`).
4.  Ward Officer approves/rejects (`PUT .../approve` or `.../reject`).
5.  On approval, citizen's ward is updated.
