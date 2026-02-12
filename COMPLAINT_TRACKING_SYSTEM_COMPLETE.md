# 🎯 Complete Complaint Tracking System - API Reference

## 📊 Overview
This document provides the **complete code reference** for tracking closed complaints and pending closures, similar to the approval queue system.

---

## 🏗️ Architecture

### DTOs Created

#### 1. **ClosedComplaintTrackingDTO** 
Comprehensive tracking for CLOSED complaints (similar to ApprovalQueueDTO structure)

```java
@Data
@Builder
public class ClosedComplaintTrackingDTO {
    // Basic Info
    private Long id;
    private String title;
    private String description;
    
    // Department & Location
    private String departmentName;
    private String wardName;
    private String priority;
    
    // Actors in the lifecycle
    private String citizenName;
    private String assignedOfficerName;
    private String approvedByName;
    private String closedByAdminName;
    
    // Status & SLA
    private String status;
    private String slaStatus;
    private Boolean slaBreached;
    
    // Timeline
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime closedAt;
    
    // Remarks & Feedback
    private String approvalRemarks;
    private String closureRemarks;
    
    // Ratings
    private Double averageRating;
    private Integer totalRatings;
    
    // Image count for quick reference
    private Integer beforeImageCount;
    private Integer afterImageCount;
}
```

#### 2. **PendingClosureTrackingDTO**
Comprehensive tracking for complaints pending admin closure

```java
@Data
@Builder
public class PendingClosureTrackingDTO {
    // Basic Info
    private Long id;
    private String title;
    private String description;
    
    // Department & Location
    private String departmentName;
    private String wardName;
    private String priority;
    
    // Actors
    private String citizenName;
    private String citizenMobile;
    private String assignedOfficerName;
    private String assignedOfficerMobile;
    private String approvedByName;
    
    // Status & SLA
    private String status;
    private String slaStatus;
    private Boolean slaBreached;
    private LocalDateTime slaDeadline;
    
    // Timeline
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime approvedAt;
    
    // Days waiting for closure
    private Long daysWaitingForClosure;
    
    // Remarks
    private String resolutionRemarks;
    private String approvalRemarks;
    
    // Image verification
    private Integer beforeImageCount;
    private Integer afterImageCount;
    private Boolean hasResolutionImages;
}
```

---

## 🔌 API Endpoints

### For Admin

#### 1. **Pending Closure Tracking** (Detailed)
```http
GET /api/admin/complaints/pending-closure-tracking?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 105,
      "title": "Broken Street Light",
      "description": "Street light not working for 3 days",
      "departmentName": "Electricity",
      "wardName": "Sector 4",
      "priority": "HIGH",
      "citizenName": "John Doe",
      "citizenMobile": "9876543210",
      "assignedOfficerName": "Officer Smith",
      "assignedOfficerMobile": "9876543211",
      "approvedByName": "Ward Officer Sarah",
      "status": "APPROVED",
      "slaStatus": "MET",
      "slaBreached": false,
      "slaDeadline": "2026-02-10T18:00:00",
      "createdAt": "2026-02-08T10:00:00",
      "resolvedAt": "2026-02-10T14:30:00",
      "approvedAt": "2026-02-10T16:00:00",
      "daysWaitingForClosure": 2,
      "resolutionRemarks": "Light replaced and tested",
      "approvalRemarks": "Verified on site. Working perfectly.",
      "beforeImageCount": 2,
      "afterImageCount": 3,
      "hasResolutionImages": true
    }
  ],
  "totalPages": 5,
  "totalElements": 47,
  "currentPage": 0
}
```

#### 2. **Closed Complaints Tracking** (Detailed)
```http
GET /api/admin/complaints/closed-tracking?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 98,
      "title": "Pothole on Main Road",
      "description": "Large pothole causing accidents",
      "departmentName": "Roads",
      "wardName": "Sector 3",
      "priority": "CRITICAL",
      "citizenName": "Jane Smith",
      "assignedOfficerName": "Officer Brown",
      "approvedByName": "Ward Officer Mike",
      "closedByAdminName": "Admin John",
      "status": "CLOSED",
      "slaStatus": "MET",
      "slaBreached": false,
      "createdAt": "2026-02-05T09:00:00",
      "resolvedAt": "2026-02-07T15:00:00",
      "approvedAt": "2026-02-07T17:00:00",
      "closedAt": "2026-02-08T10:00:00",
      "approvalRemarks": "Road repaired properly",
      "closureRemarks": "Verified via resolution images. Case closed.",
      "averageRating": 4.5,
      "totalRatings": 12,
      "beforeImageCount": 3,
      "afterImageCount": 4
    }
  ],
  "totalPages": 15,
  "totalElements": 143,
  "currentPage": 0
}
```

---

### For Ward Officers

#### 1. **Closed Complaints Tracking** (Detailed)
```http
GET /api/ward-officer/complaints/closed-tracking?page=0&size=10
```

**Response:** Same structure as Admin's closed tracking, but filtered to the Ward Officer's ward only.

**Use Case:**
- View complete history of all closed complaints in their ward
- Track performance metrics (ratings, SLA compliance)
- Review approval and closure remarks
- Verify image documentation

---

## 🎨 Frontend Integration Guide

### 1. **Pending Closure Queue (Admin Dashboard)**

```jsx
// Example React Component
import React, { useState, useEffect } from 'react';

const PendingClosureQueue = () => {
  const [complaints, setComplaints] = useState([]);
  const [pagination, setPagination] = useState({});

  useEffect(() => {
    fetch('/api/admin/complaints/pending-closure-tracking?page=0&size=10')
      .then(res => res.json())
      .then(data => {
        setComplaints(data.content);
        setPagination({
          totalPages: data.totalPages,
          totalElements: data.totalElements,
          currentPage: data.currentPage
        });
      });
  }, []);

  return (
    <div className="pending-closure-queue">
      <h2>Pending Closure ({pagination.totalElements})</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Ward</th>
            <th>Department</th>
            <th>Approved By</th>
            <th>Days Waiting</th>
            <th>SLA Status</th>
            <th>Images</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {complaints.map(c => (
            <tr key={c.id} className={c.daysWaitingForClosure > 3 ? 'urgent' : ''}>
              <td>#{c.id}</td>
              <td>{c.title}</td>
              <td>{c.wardName}</td>
              <td>{c.departmentName}</td>
              <td>{c.approvedByName}</td>
              <td>
                <span className={c.daysWaitingForClosure > 3 ? 'text-red' : ''}>
                  {c.daysWaitingForClosure} days
                </span>
              </td>
              <td>
                <span className={`badge ${c.slaBreached ? 'badge-danger' : 'badge-success'}`}>
                  {c.slaStatus}
                </span>
              </td>
              <td>
                <div className="image-verification">
                  <span>Before: {c.beforeImageCount}</span>
                  <span>After: {c.afterImageCount}</span>
                  {c.hasResolutionImages ? 
                    <span className="badge badge-success">✓ Verified</span> : 
                    <span className="badge badge-warning">⚠ Missing</span>
                  }
                </div>
              </td>
              <td>
                <button onClick={() => viewDetails(c.id)}>View</button>
                <button onClick={() => closeComplaint(c.id)}>Close</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```

### 2. **Closed Complaints History (Ward Officer Dashboard)**

```jsx
const ClosedComplaintsHistory = () => {
  const [complaints, setComplaints] = useState([]);

  useEffect(() => {
    fetch('/api/ward-officer/complaints/closed-tracking?page=0&size=10')
      .then(res => res.json())
      .then(data => setComplaints(data.content));
  }, []);

  return (
    <div className="closed-history">
      <h2>Closed Complaints History</h2>
      <div className="complaint-cards">
        {complaints.map(c => (
          <div key={c.id} className="complaint-card">
            <div className="card-header">
              <h3>{c.title}</h3>
              <span className={`priority-badge ${c.priority.toLowerCase()}`}>
                {c.priority}
              </span>
            </div>
            
            <div className="card-body">
              <div className="info-row">
                <span>Department:</span>
                <strong>{c.departmentName}</strong>
              </div>
              
              <div className="timeline">
                <div className="timeline-item">
                  <span>Created:</span>
                  <span>{new Date(c.createdAt).toLocaleDateString()}</span>
                </div>
                <div className="timeline-item">
                  <span>Resolved:</span>
                  <span>{new Date(c.resolvedAt).toLocaleDateString()}</span>
                </div>
                <div className="timeline-item">
                  <span>Approved:</span>
                  <span>{new Date(c.approvedAt).toLocaleDateString()}</span>
                </div>
                <div className="timeline-item">
                  <span>Closed:</span>
                  <span>{new Date(c.closedAt).toLocaleDateString()}</span>
                </div>
              </div>
              
              <div className="actors">
                <div>Assigned: {c.assignedOfficerName}</div>
                <div>Approved By: {c.approvedByName}</div>
                <div>Closed By: {c.closedByAdminName}</div>
              </div>
              
              <div className="remarks">
                <div className="remark-box">
                  <strong>Approval Remarks:</strong>
                  <p>{c.approvalRemarks}</p>
                </div>
                <div className="remark-box">
                  <strong>Closure Remarks:</strong>
                  <p>{c.closureRemarks}</p>
                </div>
              </div>
              
              <div className="metrics">
                <div className="metric">
                  <span>SLA:</span>
                  <span className={c.slaBreached ? 'text-red' : 'text-green'}>
                    {c.slaStatus}
                  </span>
                </div>
                <div className="metric">
                  <span>Rating:</span>
                  <span>⭐ {c.averageRating} ({c.totalRatings} reviews)</span>
                </div>
                <div className="metric">
                  <span>Images:</span>
                  <span>Before: {c.beforeImageCount} | After: {c.afterImageCount}</span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## 📝 Key Features

### ✅ Pending Closure Tracking
1. **Days Waiting Calculation**: Automatically calculates how long a complaint has been waiting for closure
2. **Image Verification**: Shows before/after image counts and verification status
3. **Contact Information**: Includes citizen and officer mobile numbers for quick communication
4. **Remarks History**: Shows both resolution and approval remarks

### ✅ Closed Complaints Tracking
1. **Complete Timeline**: Shows all timestamps (created, resolved, approved, closed)
2. **Actor Traceability**: Tracks who assigned, approved, and closed the complaint
3. **Performance Metrics**: Includes ratings and SLA status
4. **Full Remarks**: Shows approval and closure remarks for audit trail

---

## 🎯 Best Practices

### For Frontend Developers

1. **Use the Detailed Tracking Endpoints** (`-tracking` suffix) for dashboard views
2. **Implement Pagination** - All endpoints support `page` and `size` parameters
3. **Color Code by Priority**:
   - CRITICAL: Red
   - HIGH: Orange
   - MEDIUM: Yellow
   - LOW: Green

4. **Highlight Urgent Items**:
   - `daysWaitingForClosure > 3`: Show in red
   - `slaBreached === true`: Show warning badge
   - `hasResolutionImages === false`: Show alert

5. **Enable Quick Actions**:
   - View Details button → Navigate to full complaint view
   - Close button (Admin only) → Trigger closure modal
   - Export button → Download CSV/PDF report

---

## 🔄 Workflow Integration

### Admin Workflow
```
1. View Pending Closure Queue
   ↓
2. Review complaint details (images, remarks, SLA)
   ↓
3. Verify resolution quality
   ↓
4. Click "Close" button
   ↓
5. Complaint moves to Closed History
   ↓
6. Removed from Pending Queue automatically
```

### Ward Officer Workflow
```
1. Approve resolved complaint
   ↓
2. Complaint appears in Admin's Pending Closure Queue
   ↓
3. Ward Officer can track in their Closed History after Admin closes
   ↓
4. Review performance metrics and citizen feedback
```

---

## ✅ Summary

**New Endpoints:**
- `GET /api/admin/complaints/pending-closure-tracking` - Detailed pending closures
- `GET /api/admin/complaints/closed-tracking` - Detailed closed history (Admin)
- `GET /api/ward-officer/complaints/closed-tracking` - Detailed closed history (Ward)

**New DTOs:**
- `PendingClosureTrackingDTO` - Comprehensive pending closure data
- `ClosedComplaintTrackingDTO` - Comprehensive closed complaint data

**Key Benefits:**
- ✅ Complete lifecycle tracking
- ✅ Image verification status
- ✅ Performance metrics (ratings, SLA)
- ✅ Full audit trail with remarks
- ✅ Waiting time calculations
- ✅ Contact information for quick communication

This system provides the **best backend** for tracking complaints from approval to closure, with complete transparency and accountability! 🚀
