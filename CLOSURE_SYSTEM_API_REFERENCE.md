# 🎯 Complete API Reference - Closure System & Analytics

## 📊 Quick Reference Table

| Endpoint | Method | Role | Purpose | Auto-Remove on Action |
|----------|--------|------|---------|----------------------|
| `/api/admin/complaints/closure-approval-queue` | GET | Admin | Approval-style queue for closures | ✅ Yes (on close) |
| `/api/admin/complaints/pending-closure-tracking` | GET | Admin | Detailed tracking view | ✅ Yes (on close) |
| `/api/admin/complaints/closed-tracking` | GET | Admin | History of closed complaints | ❌ No (archive) |
| `/api/admin/complaints/{id}/close` | PUT | Admin | Close a complaint | - |
| `/api/ward-officer/analytics/resolution-velocity` | GET | Ward Officer | Resolution time analytics | - |
| `/api/ward-officer/complaints/closed-tracking` | GET | Ward Officer | Ward's closed history | ❌ No (archive) |
| `/api/ward-officer/complaints/pending-approval` | GET | Ward Officer | Complaints awaiting approval | ✅ Yes (on approve/reject) |

---

## 🔄 Automatic List Management Flow

### Ward Officer Approval Flow
```
1. Complaint is RESOLVED by Department Officer
   ↓
2. Appears in: GET /api/ward-officer/complaints/pending-approval
   ↓
3. Ward Officer clicks "Approve"
   ↓
4. PUT /api/ward-officer/complaints/{id}/approve
   ↓
5. Status changes: RESOLVED → APPROVED
   ↓
6. ✅ Automatically REMOVED from pending-approval list
   ↓
7. ✅ Automatically ADDED to Admin's closure-approval-queue
```

### Admin Closure Flow
```
1. Complaint is APPROVED by Ward Officer
   ↓
2. Appears in: GET /api/admin/complaints/closure-approval-queue
   ↓
3. Admin clicks "Close"
   ↓
4. PUT /api/admin/complaints/{id}/close
   ↓
5. Status changes: APPROVED → CLOSED
   ↓
6. ✅ Automatically REMOVED from closure-approval-queue
   ↓
7. ✅ Automatically ADDED to closed-tracking (archive)
```

---

## 📋 Detailed API Specifications

### 1. Admin Closure Approval Queue

**Endpoint:** `GET /api/admin/complaints/closure-approval-queue`

**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 10)

**Response:**
```json
{
  "content": [
    {
      "id": 105,
      "title": "Broken Street Light",
      "description": "Street light not working",
      "wardName": "Sector 4",
      "departmentName": "Electricity",
      "priority": "HIGH",
      "citizenName": "John Doe",
      "assignedOfficerName": "Officer Smith",
      "approvedBy": "Ward Officer Sarah",
      "status": "APPROVED",
      "slaStatus": "MET",
      "slaBreached": false,
      "slaDeadline": "2026-02-10T18:00:00",
      "createdAt": "2026-02-08T10:00:00",
      "resolvedAt": "2026-02-10T14:30:00",
      "approvedAt": "2026-02-10T16:00:00",
      "daysWaitingForClosure": 2,
      "hoursWaitingForClosure": 48,
      "approvalRemarks": "Verified on site",
      "resolutionRemarks": "Light replaced",
      "beforeImageCount": 2,
      "afterImageCount": 3,
      "hasResolutionImages": true,
      "averageRating": 4.5,
      "totalRatings": 12
    }
  ],
  "totalPages": 5,
  "totalElements": 47,
  "currentPage": 0
}
```

**Use Case:**
- Display in a table/card format similar to Ward Officer's approval queue
- Show waiting time with color coding (red if > 3 days)
- Verify image presence before allowing closure
- One-click closure with remarks

---

### 2. Resolution Velocity Analytics

**Endpoint:** `GET /api/ward-officer/analytics/resolution-velocity`

**Response:**
```json
{
  "averageResolutionTimeHours": 36.5,
  "averageResolutionTimeDays": 1.5,
  "fastestResolutionHours": 12.0,
  "slowestResolutionHours": 96.0,
  "totalResolved": 145,
  "resolutionRate": 78.5
}
```

**Metrics Explained:**
- `averageResolutionTimeHours`: Mean time from creation to resolution (in hours)
- `averageResolutionTimeDays`: Same as above but in days (rounded to 1 decimal)
- `fastestResolutionHours`: Quickest complaint resolution time
- `slowestResolutionHours`: Longest complaint resolution time
- `totalResolved`: Count of resolved/approved/closed complaints
- `resolutionRate`: Percentage of total complaints that are resolved

**Use Case:**
- Display in dashboard as KPI cards
- Show trend indicators (improving/declining)
- Compare against department benchmarks
- Identify bottlenecks

---

### 3. Close Complaint (Admin)

**Endpoint:** `PUT /api/admin/complaints/{id}/close`

**Request Body:**
```json
{
  "remarks": "Verified via resolution images. Case closed."
}
```

**Response:**
```json
{
  "message": "Complaint closed successfully"
}
```

**Side Effects:**
1. Status changes: `APPROVED` → `CLOSED`
2. Sets `closedAt` timestamp
3. Sets `closedByAdmin` to current admin
4. Finalizes SLA status (MET/BREACHED)
5. Logs status change in history
6. Sends notification to citizen
7. **Automatically removes from closure-approval-queue**
8. **Automatically appears in closed-tracking**

---

## 🎨 Frontend Implementation Checklist

### Admin Closure Queue Page

```jsx
✅ Fetch queue on page load
✅ Display in card/table format
✅ Show waiting time with color coding
✅ Display before/after image counts
✅ Show approval and resolution remarks
✅ Enable "Close" button only if hasResolutionImages === true
✅ Prompt for closure remarks on close
✅ Refresh queue after successful closure
✅ Implement pagination
✅ Add filters (by ward, department, priority)
✅ Add search functionality
```

### Ward Officer Analytics Page

```jsx
✅ Fetch velocity data on page load
✅ Display as KPI cards
✅ Show average resolution time prominently
✅ Highlight fastest resolution (green)
✅ Highlight slowest resolution (red/orange)
✅ Show resolution rate as percentage
✅ Add progress bar for resolution rate
✅ Display performance indicator (Excellent/Good/Needs Improvement)
✅ Add trend chart (optional)
✅ Enable export to PDF/Excel
```

---

## 🔍 Filtering & Sorting

### Recommended Filters for Closure Queue

```javascript
const filters = {
  ward: 'all',           // Filter by specific ward
  department: 'all',     // Filter by department
  priority: 'all',       // Filter by priority
  waitingDays: 'all',    // Filter by waiting time (0-1, 1-3, 3+)
  slaStatus: 'all',      // Filter by SLA status
  hasImages: 'all'       // Filter by image verification
};
```

### Recommended Sorting Options

```javascript
const sortOptions = [
  { value: 'waitingTime', label: 'Waiting Time (Longest First)' },
  { value: 'priority', label: 'Priority (Highest First)' },
  { value: 'slaDeadline', label: 'SLA Deadline (Urgent First)' },
  { value: 'approvedAt', label: 'Approval Date (Newest First)' }
];
```

---

## 📱 Mobile Responsive Design

### Card Layout for Mobile

```jsx
<div className="complaint-card-mobile">
  <div className="card-header-mobile">
    <span className="id">#{id}</span>
    <span className="priority-badge">{priority}</span>
  </div>
  
  <div className="card-title">{title}</div>
  
  <div className="card-meta">
    <span>{wardName}</span>
    <span>•</span>
    <span>{departmentName}</span>
  </div>
  
  <div className="waiting-time-mobile">
    ⏱️ Waiting {daysWaitingForClosure} days
  </div>
  
  <div className="card-actions-mobile">
    <button onClick={viewDetails}>View</button>
    <button onClick={close}>Close</button>
  </div>
</div>
```

---

## 🚀 Performance Optimization

### Pagination Best Practices

```javascript
// Use server-side pagination
const fetchQueue = async (page = 0, size = 10) => {
  const response = await fetch(
    `/api/admin/complaints/closure-approval-queue?page=${page}&size=${size}`
  );
  return response.json();
};

// Cache results for quick navigation
const cache = new Map();
const getCachedQueue = async (page) => {
  if (cache.has(page)) {
    return cache.get(page);
  }
  const data = await fetchQueue(page);
  cache.set(page, data);
  return data;
};
```

### Debounced Search

```javascript
import { debounce } from 'lodash';

const debouncedSearch = debounce((searchTerm) => {
  fetchQueue(0, 10, { search: searchTerm });
}, 300);
```

---

## 🎯 Key Differences from Previous Endpoints

| Feature | Old Endpoints | New Endpoints |
|---------|--------------|---------------|
| **Format** | Simple list | Approval queue format |
| **Waiting Time** | Not calculated | Days + Hours calculated |
| **Image Verification** | Not shown | Before/After counts + verification flag |
| **Remarks** | Not included | Both approval + resolution remarks |
| **Performance Metrics** | Not included | Ratings + SLA status |
| **Auto-Remove** | Manual refresh | Automatic on status change |

---

## ✅ Testing Checklist

### Backend Testing

```bash
# Test closure approval queue
curl -X GET "http://localhost:8080/api/admin/complaints/closure-approval-queue?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test resolution velocity
curl -X GET "http://localhost:8080/api/ward-officer/analytics/resolution-velocity" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test close complaint
curl -X PUT "http://localhost:8080/api/admin/complaints/105/close" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"remarks": "Test closure"}'
```

### Frontend Testing

```javascript
// Test automatic removal
test('Complaint removed from queue after closure', async () => {
  const initialQueue = await fetchQueue();
  const complaintId = initialQueue.content[0].id;
  
  await closeComplaint(complaintId, 'Test remarks');
  
  const updatedQueue = await fetchQueue();
  const stillExists = updatedQueue.content.find(c => c.id === complaintId);
  
  expect(stillExists).toBeUndefined();
});
```

---

## 📊 Summary

**New Endpoints Created:** 2
- `GET /api/admin/complaints/closure-approval-queue`
- `GET /api/ward-officer/analytics/resolution-velocity`

**New DTOs Created:** 2
- `ClosureApprovalQueueDTO`
- Resolution velocity returns Map<String, Object>

**Key Features:**
✅ Approval-style queue for Admin closures
✅ Automatic list management (add/remove on status change)
✅ Resolution velocity analytics
✅ Waiting time tracking (days + hours)
✅ Image verification status
✅ Complete audit trail with remarks
✅ Performance metrics (ratings, SLA)

**This provides the BEST backend for efficient complaint closure management!** 🚀
