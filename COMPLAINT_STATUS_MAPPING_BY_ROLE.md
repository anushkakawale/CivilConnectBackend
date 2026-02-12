# 🗺️ Complete Complaint Status Mapping by User Role

## 📊 Overview
This document maps all complaint statuses to each user role, showing what complaints they can see and what actions they can take.

---

## 🎭 User Roles & Their Complaint Views

### 1. **CITIZEN** 👤

**What They See:** All complaints they have registered (in their ward or any ward)

**Complaint Statuses Visible:**
- ✅ `SUBMITTED` - Just registered, awaiting assignment
- ✅ `ASSIGNED` - Assigned to a department officer
- ✅ `IN_PROGRESS` - Officer is working on it
- ✅ `RESOLVED` - Officer marked as resolved, awaiting approval
- ✅ `APPROVED` - Ward Officer approved, awaiting admin closure
- ✅ `CLOSED` - Admin closed the complaint
- ✅ `REJECTED` - Ward Officer rejected the resolution
- ✅ `REOPENED` - Citizen reopened after closure
- ✅ `ON_HOLD` - Temporarily paused
- ✅ `ESCALATED` - Escalated due to SLA breach

**API Endpoint:**
```http
GET /api/citizen/complaints/my-complaints
```

**Filter Options:**
- By Status
- By Ward (if registered in multiple wards)
- By Date Range
- By Priority

**Response Structure:**
```json
{
  "content": [
    {
      "id": 101,
      "title": "Broken Street Light",
      "status": "IN_PROGRESS",
      "wardName": "Sector 4",
      "departmentName": "Electricity",
      "priority": "HIGH",
      "slaStatus": "ON_TRACK",
      "createdAt": "2026-02-10T10:00:00",
      "canReopen": false,
      "canRate": false
    }
  ]
}
```

---

### 2. **DEPARTMENT OFFICER** 👷

**What They See:** Only complaints assigned to them

**Complaint Statuses Visible:**
- ✅ `ASSIGNED` - Newly assigned, needs to start work
- ✅ `IN_PROGRESS` - Currently working on
- ✅ `RESOLVED` - Marked as resolved, awaiting approval
- ✅ `REJECTED` - Ward Officer rejected, needs rework
- ✅ `ON_HOLD` - Temporarily paused by them
- ✅ `ESCALATED` - Escalated due to SLA breach

**NOT Visible:**
- ❌ `SUBMITTED` - Not assigned yet
- ❌ `APPROVED` - Already approved, out of their hands
- ❌ `CLOSED` - Already closed
- ❌ `REOPENED` - Goes back to ASSIGNED status

**API Endpoint:**
```http
GET /api/department-officer/complaints/my-assignments
```

**Filter Options:**
- By Status
- By Priority
- By SLA Status
- By Date Assigned

**Response Structure:**
```json
{
  "content": [
    {
      "id": 102,
      "title": "Pothole on Main Road",
      "status": "ASSIGNED",
      "wardName": "Sector 3",
      "departmentName": "Roads",
      "priority": "CRITICAL",
      "slaStatus": "WARNING",
      "slaDeadline": "2026-02-12T18:00:00",
      "assignedAt": "2026-02-11T09:00:00",
      "citizenName": "John Doe",
      "citizenMobile": "9876543210",
      "beforeImageCount": 2,
      "afterImageCount": 0
    }
  ]
}
```

---

### 3. **WARD OFFICER** 🏛️

**What They See:** All complaints in their assigned ward

**Complaint Statuses Visible:**
- ✅ `SUBMITTED` - Newly registered, needs assignment
- ✅ `ASSIGNED` - Assigned to department officer
- ✅ `IN_PROGRESS` - Officer working on it
- ✅ `RESOLVED` - Awaiting their approval
- ✅ `APPROVED` - They approved, awaiting admin closure
- ✅ `CLOSED` - Admin closed
- ✅ `REJECTED` - They rejected the resolution
- ✅ `REOPENED` - Citizen reopened
- ✅ `ON_HOLD` - Temporarily paused
- ✅ `ESCALATED` - Escalated due to SLA breach

**API Endpoints:**

**All Ward Complaints:**
```http
GET /api/ward-officer/complaints/all
```

**By Status:**
```http
GET /api/ward-officer/complaints/status/{status}
# Example: /api/ward-officer/complaints/status/SUBMITTED
```

**Pending Approval (Most Important):**
```http
GET /api/ward-officer/complaints/pending-approval
# Returns only RESOLVED complaints
```

**Closed History:**
```http
GET /api/ward-officer/complaints/closed-tracking
```

**Response Structure:**
```json
{
  "content": [
    {
      "id": 103,
      "title": "Garbage Not Collected",
      "status": "RESOLVED",
      "wardName": "Sector 4",
      "departmentName": "Sanitation",
      "priority": "MEDIUM",
      "citizenName": "Jane Smith",
      "assignedOfficerName": "Officer Brown",
      "slaStatus": "MET",
      "createdAt": "2026-02-09T08:00:00",
      "resolvedAt": "2026-02-11T14:00:00",
      "resolutionRemarks": "Garbage collected and area cleaned",
      "beforeImageCount": 3,
      "afterImageCount": 4,
      "daysToResolve": 2
    }
  ]
}
```

---

### 4. **ADMIN** 👨‍💼

**What They See:** ALL complaints across ALL wards

**Complaint Statuses Visible:**
- ✅ `SUBMITTED` - All new complaints
- ✅ `ASSIGNED` - All assigned complaints
- ✅ `IN_PROGRESS` - All in-progress complaints
- ✅ `RESOLVED` - All resolved complaints
- ✅ `APPROVED` - All approved, awaiting closure
- ✅ `CLOSED` - All closed complaints
- ✅ `REJECTED` - All rejected complaints
- ✅ `REOPENED` - All reopened complaints
- ✅ `ON_HOLD` - All on-hold complaints
- ✅ `ESCALATED` - All escalated complaints

**API Endpoints:**

**All Complaints:**
```http
GET /api/admin/complaints/all
```

**By Status:**
```http
GET /api/admin/complaints/status/{status}
# Example: /api/admin/complaints/status/APPROVED
```

**Closure Approval Queue (Most Important):**
```http
GET /api/admin/complaints/closure-approval-queue
# Returns only APPROVED complaints
```

**Closed History:**
```http
GET /api/admin/complaints/closed-tracking
```

**By Ward:**
```http
GET /api/admin/complaints/ward/{wardId}
```

**By Department:**
```http
GET /api/admin/complaints/department/{departmentId}
```

**Response Structure:**
```json
{
  "content": [
    {
      "id": 104,
      "title": "Water Leakage",
      "status": "APPROVED",
      "wardName": "Sector 2",
      "departmentName": "Water Supply",
      "priority": "HIGH",
      "citizenName": "Mike Johnson",
      "assignedOfficerName": "Officer Davis",
      "approvedByName": "Ward Officer Sarah",
      "slaStatus": "MET",
      "createdAt": "2026-02-08T11:00:00",
      "resolvedAt": "2026-02-10T15:00:00",
      "approvedAt": "2026-02-11T10:00:00",
      "daysWaitingForClosure": 1,
      "beforeImageCount": 2,
      "afterImageCount": 3
    }
  ]
}
```

---

## 📋 Complete Status Breakdown

### Status Definitions

| Status | Description | Who Can See | Who Can Change |
|--------|-------------|-------------|----------------|
| `SUBMITTED` | Just registered | Citizen, Ward Officer, Admin | System (auto-assigns) |
| `ASSIGNED` | Assigned to officer | All roles | Dept Officer (to IN_PROGRESS) |
| `IN_PROGRESS` | Officer working | All roles | Dept Officer (to RESOLVED/ON_HOLD) |
| `RESOLVED` | Officer completed | All roles | Ward Officer (to APPROVED/REJECTED) |
| `APPROVED` | Ward Officer approved | All roles | Admin (to CLOSED) |
| `CLOSED` | Admin closed | All roles | Citizen (to REOPENED, within 7 days) |
| `REJECTED` | Ward Officer rejected | All roles | System (back to ASSIGNED) |
| `REOPENED` | Citizen reopened | All roles | System (back to ASSIGNED) |
| `ON_HOLD` | Temporarily paused | All roles | Dept Officer (back to IN_PROGRESS) |
| `ESCALATED` | SLA breached | All roles | System (automatic) |

---

## 🔄 Status Flow by Role

### Citizen's View
```
SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED → APPROVED → CLOSED
                                                              ↓
                                                          REOPENED
```

### Department Officer's View
```
ASSIGNED → IN_PROGRESS → RESOLVED
    ↑           ↓
    ←──── ON_HOLD
    ↑
REJECTED
```

### Ward Officer's View
```
SUBMITTED → (assigns) → ASSIGNED → IN_PROGRESS → RESOLVED
                                                      ↓
                                                  APPROVED / REJECTED
```

### Admin's View
```
Complete visibility of all statuses and transitions
APPROVED → CLOSED
```

---

## 🎯 Implementation Guide

### Backend Repository Methods Needed

```java
// For Citizen
Page<Complaint> findByCitizen_UserId(Long userId, Pageable pageable);
Page<Complaint> findByCitizen_UserIdAndStatus(Long userId, ComplaintStatus status, Pageable pageable);

// For Department Officer
Page<Complaint> findByAssignedOfficer_UserId(Long userId, Pageable pageable);
Page<Complaint> findByAssignedOfficer_UserIdAndStatus(Long userId, ComplaintStatus status, Pageable pageable);
Page<Complaint> findByAssignedOfficer_UserIdAndStatusIn(Long userId, List<ComplaintStatus> statuses, Pageable pageable);

// For Ward Officer
Page<Complaint> findByWard_WardId(Long wardId, Pageable pageable);
Page<Complaint> findByWard_WardIdAndStatus(Long wardId, ComplaintStatus status, Pageable pageable);
Page<Complaint> findByWard_WardIdAndStatusIn(Long wardId, List<ComplaintStatus> statuses, Pageable pageable);

// For Admin
Page<Complaint> findAll(Pageable pageable);
Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
Page<Complaint> findByStatusIn(List<ComplaintStatus> statuses, Pageable pageable);
Page<Complaint> findByWard_WardId(Long wardId, Pageable pageable);
Page<Complaint> findByDepartment_DepartmentId(Long deptId, Pageable pageable);
```

---

## 📊 Frontend Dashboard Views

### Citizen Dashboard
```jsx
const CitizenDashboard = () => {
  return (
    <div>
      <StatusCards>
        <Card title="Total Complaints" count={totalComplaints} />
        <Card title="In Progress" count={inProgress} status="IN_PROGRESS" />
        <Card title="Resolved" count={resolved} status="RESOLVED" />
        <Card title="Closed" count={closed} status="CLOSED" />
      </StatusCards>
      
      <ComplaintList>
        <Tab name="All" />
        <Tab name="Submitted" status="SUBMITTED" />
        <Tab name="In Progress" status="IN_PROGRESS" />
        <Tab name="Resolved" status="RESOLVED" />
        <Tab name="Closed" status="CLOSED" />
      </ComplaintList>
    </div>
  );
};
```

### Department Officer Dashboard
```jsx
const DeptOfficerDashboard = () => {
  return (
    <div>
      <StatusCards>
        <Card title="Assigned" count={assigned} status="ASSIGNED" urgent />
        <Card title="In Progress" count={inProgress} status="IN_PROGRESS" />
        <Card title="Resolved" count={resolved} status="RESOLVED" />
        <Card title="Rejected" count={rejected} status="REJECTED" warning />
      </StatusCards>
      
      <ComplaintList>
        <Tab name="Pending Work" statuses={["ASSIGNED", "REJECTED"]} />
        <Tab name="In Progress" status="IN_PROGRESS" />
        <Tab name="Resolved" status="RESOLVED" />
        <Tab name="On Hold" status="ON_HOLD" />
      </ComplaintList>
    </div>
  );
};
```

### Ward Officer Dashboard
```jsx
const WardOfficerDashboard = () => {
  return (
    <div>
      <StatusCards>
        <Card title="Pending Assignment" count={submitted} status="SUBMITTED" urgent />
        <Card title="Pending Approval" count={resolved} status="RESOLVED" urgent />
        <Card title="In Progress" count={inProgress} status="IN_PROGRESS" />
        <Card title="Closed" count={closed} status="CLOSED" />
      </StatusCards>
      
      <ComplaintList>
        <Tab name="Pending Assignment" status="SUBMITTED" />
        <Tab name="Pending Approval" status="RESOLVED" />
        <Tab name="In Progress" status="IN_PROGRESS" />
        <Tab name="Approved" status="APPROVED" />
        <Tab name="Closed" status="CLOSED" />
      </ComplaintList>
    </div>
  );
};
```

### Admin Dashboard
```jsx
const AdminDashboard = () => {
  return (
    <div>
      <StatusCards>
        <Card title="Total Complaints" count={total} />
        <Card title="Pending Closure" count={approved} status="APPROVED" urgent />
        <Card title="In Progress" count={inProgress} status="IN_PROGRESS" />
        <Card title="Closed" count={closed} status="CLOSED" />
      </StatusCards>
      
      <ComplaintList>
        <Tab name="All" />
        <Tab name="Submitted" status="SUBMITTED" />
        <Tab name="Assigned" status="ASSIGNED" />
        <Tab name="In Progress" status="IN_PROGRESS" />
        <Tab name="Resolved" status="RESOLVED" />
        <Tab name="Approved" status="APPROVED" />
        <Tab name="Closed" status="CLOSED" />
        <Tab name="Escalated" status="ESCALATED" />
      </ComplaintList>
      
      <Filters>
        <FilterByWard />
        <FilterByDepartment />
        <FilterByPriority />
        <FilterBySLA />
      </Filters>
    </div>
  );
};
```

---

## 🎨 Status Color Coding

```css
.status-SUBMITTED { background: #3b82f6; color: white; } /* Blue */
.status-ASSIGNED { background: #8b5cf6; color: white; } /* Purple */
.status-IN_PROGRESS { background: #f59e0b; color: white; } /* Orange */
.status-RESOLVED { background: #10b981; color: white; } /* Green */
.status-APPROVED { background: #06b6d4; color: white; } /* Cyan */
.status-CLOSED { background: #6b7280; color: white; } /* Gray */
.status-REJECTED { background: #ef4444; color: white; } /* Red */
.status-REOPENED { background: #f97316; color: white; } /* Dark Orange */
.status-ON_HOLD { background: #eab308; color: black; } /* Yellow */
.status-ESCALATED { background: #dc2626; color: white; } /* Dark Red */
```

---

## ✅ Summary Table

| Role | Sees | Primary Actions | Key Statuses |
|------|------|----------------|--------------|
| **Citizen** | Own complaints | Register, Reopen, Rate | ALL (read-only) |
| **Dept Officer** | Assigned to them | Start, Resolve, Upload Images | ASSIGNED, IN_PROGRESS, RESOLVED |
| **Ward Officer** | Ward complaints | Assign, Approve, Reject | SUBMITTED, RESOLVED |
| **Admin** | All complaints | Close, View Reports | APPROVED (for closure) |

---

**This mapping ensures each role sees exactly what they need to see and can act on!** 🚀
