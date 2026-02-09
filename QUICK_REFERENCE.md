# CivicConnect Quick Reference

## Application Flow

### 1. User Roles
- **Citizen**: Registers complaints, tracks status, updates profile.
- **Department Officer**: Receives complaints, works on them (Start -> Resolve), uploads proof.
- **Ward Officer**: Oversees ward, approves/rejects resolved complaints from Department Officers.
- **Admin**: Oversees entire city, manages users, closes approved complaints, views analytics.

### 2. Complaint Lifecycle
1.  **Submitted**: Citizen creates a complaint.
    *   *Auto-Assignment*: System assigns to Department Officer based on Ward & Department.
    *   *Status*: `SUBMITTED` -> `ASSIGNED`.
2.  **In Progress**: Department Officer clicks "Start Work".
    *   *Status*: `ASSIGNED` -> `IN_PROGRESS`.
3.  **Resolved**: Department Officer uploads image & clicks "Resolve".
    *   *Status*: `IN_PROGRESS` -> `RESOLVED`.
    *   *Approval Created*: A pending approval request is sent to Ward Officer.
4.  **Approved/Rejected**: Ward Officer requires action.
    *   **Approve**: Validates work.
        *   *Status*: `RESOLVED` -> `APPROVED`.
        *   *Notification*: Admin is notified.
    *   **Reject**: Work unsatisfactory.
        *   *Status*: `RESOLVED` -> `IN_PROGRESS` (Sent back to Department Officer).
5.  **Closed**: Admin reviews approved complaints and performs final closure.
    *   *Status*: `APPROVED` -> `CLOSED`.

### 3. Key Backend Services
- **ComplaintService**: Core logic for creation and tracking.
- **ComplaintAssignmentService**: Handles auto-assignment logic.
- **DepartmentComplaintService**: Dept Officer actions (Start, Resolve).
- **WardOfficerComplaintService**: Ward Officer actions (Approve, Reject).
- **AdminDashboardService**: Admin stats and closure logic.
- **NotificationService**: Centralized notification handler.

## Frontend Overview (React)

### Key Pages
- **Login/Register**: `/login`, `/register`
- **Citizen**:
    - Dashboard: `/citizen/dashboard`
    - New Complaint: `/complaints/new`
    - My Complaints: `/citizen/my-complaints`
    - Profile: `/profile`
- **Department Officer**:
    - Dashboard: `/department/dashboard`
    - Assigned Work: `/department/assigned`
- **Ward Officer**:
    - Dashboard: `/ward-officer/dashboard`
    - Approvals: `/ward-officer/approvals`
- **Admin**:
    - Dashboard: `/admin/dashboard`
    - Complaints List: `/admin/complaints`
    - Officers: `/admin/officers`
    - Reports: `/admin/reports` (PDF/Excel)
    - Map: `/admin/map`

### Styling
- **CSS**: Pure CSS / Modules.
- **Theme**: "Premium Tactical" (Blues, White, Clean Gradients).

## Troubleshooting
- **403 Forbidden**: Check `SecurityConfig.java` to ensure the endpoint matches the user role.
- **No Class Def Found**: Usually a build issue. Try refreshing the project or "touching" the file to force recompile.
