# 🎯 CivicConnect: SLA & Complaint Status Tracking System

## 📊 Overview
This document explains how **Service Level Agreements (SLAs)** and **Complaint Statuses** are tracked across all user profiles in the CivicConnect system.

---

## 1. 🕒 SLA Tracking System

### **What is SLA?**
SLA (Service Level Agreement) defines the **maximum time** allowed to resolve a complaint based on its department. Each department has a predefined SLA in **hours**.

### **Department-Specific SLA Hours**
| Department | SLA Hours | Priority Level | Description |
|------------|-----------|----------------|-------------|
| **Public Safety** | 6 hours | CRITICAL | Open manholes, hazards |
| **Waste Management** | 12 hours | CRITICAL | Garbage collection |
| **Water Supply** | 24 hours | HIGH | No water, leakage, low pressure |
| **Electricity** | 24 hours | HIGH | Street lights, power issues |
| **Sanitation** | 36 hours | MEDIUM | Public toilets, cleanliness |
| **Health** | 48 hours | MEDIUM | Mosquitoes, hygiene |
| **Roads** | 72 hours | LOW | Potholes, damaged roads |
| **Education** | 96 hours | LOW | School infrastructure |

### **SLA Lifecycle States**
```
┌─────────────┐
│  ON_TRACK   │ ← Complaint just assigned, plenty of time left
└──────┬──────┘
       │
       ▼ (2 hours before deadline)
┌─────────────┐
│   WARNING   │ ← Officer gets notified: "⏳ 2 Hours Remaining"
└──────┬──────┘
       │
       ▼ (Deadline passed)
┌─────────────┐
│  BREACHED   │ ← Citizen & Admin notified: "🚨 SLA Breached"
└──────┬──────┘
       │
       ▼ (Complaint resolved within SLA)
┌─────────────┐
│     MET     │ ← SLA successfully met
└─────────────┘
```

### **How SLA is Calculated**
1. **Start Time**: When complaint is **ASSIGNED** to a Department Officer
2. **Deadline**: `Start Time + Department SLA Hours`
3. **Tracking**: Automated scheduler runs **every 5 minutes** to check all active SLAs

### **Automated SLA Monitoring**
- **Scheduler**: `SlaEscalationScheduler.java`
- **Frequency**: Every 5 minutes (`@Scheduled(fixedRate = 300000)`)
- **Actions**:
  - **2 hours before deadline**: Status → `WARNING`, Officer notified
  - **After deadline**: Status → `BREACHED`, Citizen + Admin notified, `escalated` flag set

### **SLA Visibility by Role**
| Role | What They See |
|------|---------------|
| **Citizen** | SLA deadline, time remaining, breach status on their complaints |
| **Department Officer** | SLA warnings for assigned tasks, breach alerts |
| **Ward Officer** | SLA breach statistics for their ward |
| **Admin** | System-wide SLA breach count, department-wise performance |

---

## 2. 📋 Complaint Status Flow

### **Complete Status Lifecycle**
```
SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED → APPROVED → CLOSED
    ↓                                              ↑
REJECTED                                      REOPENED
    ↓                                              ↓
 CLOSED                                       IN_PROGRESS
```

### **Status Definitions & Permissions**

| Status | Who Sets It | Meaning | Visible To |
|--------|-------------|---------|------------|
| **SUBMITTED** | Citizen | Complaint registered, awaiting assignment | All |
| **ASSIGNED** | Ward Officer | Task assigned to Department Officer | All |
| **IN_PROGRESS** | Dept Officer | Officer actively working on the issue | All |
| **RESOLVED** | Dept Officer | Officer claims work is complete | All |
| **APPROVED** | Ward Officer | Ward Officer verifies resolution | All |
| **CLOSED** | Admin | Final closure after verification | All |
| **REOPENED** | Citizen | Citizen disputes resolution (within 7 days) | All |
| **REJECTED** | Admin | Complaint deemed invalid/spam | All |
| **ON_HOLD** | Dept Officer | Temporarily paused (awaiting resources) | All |
| **ESCALATED** | System (Auto) | SLA breached, escalated to higher authority | All |

### **Status Transition Rules**

#### **Citizen Actions**
- Can create: `SUBMITTED`
- Can trigger: `REOPENED` (only if status is `RESOLVED` or `CLOSED`, within 7 days)

#### **Ward Officer Actions**
- Can set: `ASSIGNED` (from `SUBMITTED`)
- Can set: `APPROVED` (from `RESOLVED`)

#### **Department Officer Actions**
- Can set: `IN_PROGRESS` (from `ASSIGNED`)
- Can set: `RESOLVED` (from `IN_PROGRESS`)
- Can set: `ON_HOLD` (from `IN_PROGRESS`)

#### **Admin Actions**
- Can set: `CLOSED` (from `APPROVED`)
- Can set: `REJECTED` (from any status)

#### **System Auto-Actions**
- Sets: `ESCALATED` (when SLA is breached)

---

## 3. 🔍 How Users Track Complaints

### **Citizen View**
```javascript
GET /api/citizen/complaints
// Returns:
{
  "complaintId": 123,
  "status": "IN_PROGRESS",
  "slaStatus": "WARNING",
  "slaDeadline": "2026-02-12T18:00:00",
  "slaBreached": false,
  "assignedOfficerName": "Arjun Sharma",
  "department": "Water Supply"
}
```
**What they see:**
- Current status
- Assigned officer details
- SLA countdown timer
- Breach alerts

### **Department Officer View**
```javascript
GET /api/department/complaints/assigned
// Returns only their assigned complaints with:
{
  "activeCount": 12,
  "slaWarningCount": 3,
  "slaBreachedCount": 1,
  "complaints": [...]
}
```
**What they see:**
- Tasks assigned to them
- SLA warnings (red alerts for <2 hours)
- Workload metrics

### **Ward Officer View**
```javascript
GET /api/ward-officer/complaints/all
// Returns all complaints in their ward
{
  "totalComplaints": 245,
  "unassignedCount": 12,
  "slaBreachedCount": 8,
  "resolvedPercentage": 78.5
}
```
**What they see:**
- Ward-wide statistics
- Unassigned complaints
- SLA breach hotspots

### **Admin View**
```javascript
GET /api/admin/complaints/all
// System-wide view
{
  "totalComplaints": 1523,
  "slaBreachedCount": 45,
  "departmentPerformance": {
    "Water Supply": { "total": 320, "breached": 12 },
    "Roads": { "total": 450, "breached": 8 }
  }
}
```
**What they see:**
- Global metrics
- Department-wise SLA performance
- Closure authority

---

## 4. 🎨 Frontend Integration

### **SLA Display Components**
```jsx
// SLA Status Badge
{slaStatus === 'BREACHED' && <Badge color="red">🚨 SLA BREACHED</Badge>}
{slaStatus === 'WARNING' && <Badge color="yellow">⏳ 2 HRS LEFT</Badge>}
{slaStatus === 'ON_TRACK' && <Badge color="green">✅ ON TRACK</Badge>}

// Countdown Timer
<SlaTimer deadline={complaint.slaDeadline} />
```

### **Status Color Coding**
```css
SUBMITTED → Gray
ASSIGNED → Yellow
IN_PROGRESS → Blue
RESOLVED → Purple
APPROVED → Teal
CLOSED → Green
REOPENED → Orange
REJECTED → Red
ESCALATED → Dark Red
```

---

## 5. 🔔 Notification System

### **SLA-Based Notifications**
| Event | Recipient | Message |
|-------|-----------|---------|
| SLA Warning (2hrs) | Assigned Officer | "⏳ 2 Hours Remaining: Complaint #123 is due soon." |
| SLA Breached | Citizen + Officer + Admin | "🚨 DEADLINE MISSED: Complaint #123 is now in breach." |
| Status Change | Citizen | "Your complaint #123 is now IN_PROGRESS" |

---

## 6. 📈 Key Metrics Tracked

### **Per Department**
- Total complaints
- SLA breach rate
- Average resolution time
- Active vs Closed ratio

### **Per Ward**
- Complaint density
- SLA compliance %
- Unassigned backlog

### **Per Officer**
- Active workload
- SLA breach count
- Resolution success rate

---

## 7. 🛠️ Technical Implementation

### **Database Schema**
```sql
-- Complaint table has:
status ENUM('SUBMITTED', 'ASSIGNED', ...) NOT NULL
slaBreached BOOLEAN DEFAULT FALSE
escalated BOOLEAN DEFAULT FALSE
slaDeadline DATETIME

-- ComplaintSla table has:
slaStartTime DATETIME NOT NULL
slaDeadline DATETIME NOT NULL
status ENUM('ON_TRACK', 'WARNING', 'BREACHED', 'MET')
```

### **Key Services**
- `SlaEscalationScheduler`: Automated SLA monitoring
- `ComplaintService`: Status transitions
- `NotificationService`: Real-time alerts

---

## 8. ✅ Best Practices

1. **Always check SLA before assignment**: Ward Officers should prioritize critical departments
2. **Monitor WARNING status**: Officers get 2-hour buffer to prevent breaches
3. **Reopen within 7 days**: Citizens have a grace period to dispute resolutions
4. **Admin final authority**: Only Admins can permanently close or reject complaints

---

This system ensures **accountability**, **transparency**, and **timely resolution** of civic issues across all stakeholders.
