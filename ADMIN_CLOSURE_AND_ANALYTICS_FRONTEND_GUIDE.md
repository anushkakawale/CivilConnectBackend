# 🚀 Complete Closure System & Analytics - Frontend Guide

## 📊 Overview
This guide provides everything needed to build the frontend for:
1. **Admin Closure Approval Queue** (similar to Ward Officer's approval queue)
2. **Resolution Velocity Analytics** for Ward Officers
3. **Complete Closure Workflow** with automatic list management

---

## 🎯 Part 1: Admin Closure Approval Queue

### API Endpoint
```http
GET /api/admin/complaints/closure-approval-queue?page=0&size=10
```

### Response Structure
```json
{
  "content": [
    {
      "id": 105,
      "title": "Broken Street Light on Main Road",
      "description": "Street light not working for 3 days",
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
      "approvalRemarks": "Verified on site. Light is working perfectly.",
      "resolutionRemarks": "Light replaced and tested successfully",
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

### React Component Example

```jsx
import React, { useState, useEffect } from 'react';
import { CheckCircle, XCircle, Clock, Image, Star } from 'lucide-react';

const AdminClosureApprovalQueue = () => {
  const [queue, setQueue] = useState([]);
  const [pagination, setPagination] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchQueue();
  }, []);

  const fetchQueue = async (page = 0) => {
    setLoading(true);
    try {
      const response = await fetch(
        `/api/admin/complaints/closure-approval-queue?page=${page}&size=10`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      const data = await response.json();
      setQueue(data.content);
      setPagination({
        totalPages: data.totalPages,
        totalElements: data.totalElements,
        currentPage: data.currentPage
      });
    } catch (error) {
      console.error('Error fetching closure queue:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = async (complaintId) => {
    const remarks = prompt('Enter closure remarks:');
    if (!remarks) return;

    try {
      await fetch(`/api/admin/complaints/${complaintId}/close`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ remarks })
      });
      
      // Refresh queue - complaint will be automatically removed
      fetchQueue(pagination.currentPage);
      alert('Complaint closed successfully!');
    } catch (error) {
      console.error('Error closing complaint:', error);
      alert('Failed to close complaint');
    }
  };

  const getPriorityColor = (priority) => {
    const colors = {
      CRITICAL: 'bg-red-100 text-red-800 border-red-300',
      HIGH: 'bg-orange-100 text-orange-800 border-orange-300',
      MEDIUM: 'bg-yellow-100 text-yellow-800 border-yellow-300',
      LOW: 'bg-green-100 text-green-800 border-green-300'
    };
    return colors[priority] || colors.MEDIUM;
  };

  const getWaitingTimeColor = (days) => {
    if (days > 3) return 'text-red-600 font-bold';
    if (days > 1) return 'text-orange-600';
    return 'text-green-600';
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="closure-approval-queue">
      <div className="header">
        <h1>Closure Approval Queue</h1>
        <div className="stats">
          <span className="badge badge-primary">
            {pagination.totalElements} Pending Closure
          </span>
        </div>
      </div>

      <div className="queue-list">
        {queue.map(complaint => (
          <div key={complaint.id} className="complaint-card">
            {/* Header */}
            <div className="card-header">
              <div className="title-section">
                <h3>#{complaint.id} - {complaint.title}</h3>
                <span className={`priority-badge ${getPriorityColor(complaint.priority)}`}>
                  {complaint.priority}
                </span>
              </div>
              <div className="waiting-time">
                <Clock size={16} />
                <span className={getWaitingTimeColor(complaint.daysWaitingForClosure)}>
                  Waiting {complaint.daysWaitingForClosure} days 
                  ({complaint.hoursWaitingForClosure}h)
                </span>
              </div>
            </div>

            {/* Body */}
            <div className="card-body">
              <div className="info-grid">
                <div className="info-item">
                  <label>Ward:</label>
                  <span>{complaint.wardName}</span>
                </div>
                <div className="info-item">
                  <label>Department:</label>
                  <span>{complaint.departmentName}</span>
                </div>
                <div className="info-item">
                  <label>Citizen:</label>
                  <span>{complaint.citizenName}</span>
                </div>
                <div className="info-item">
                  <label>Assigned Officer:</label>
                  <span>{complaint.assignedOfficerName}</span>
                </div>
                <div className="info-item">
                  <label>Approved By:</label>
                  <span>{complaint.approvedBy}</span>
                </div>
                <div className="info-item">
                  <label>SLA Status:</label>
                  <span className={complaint.slaBreached ? 'text-red-600' : 'text-green-600'}>
                    {complaint.slaStatus}
                  </span>
                </div>
              </div>

              {/* Timeline */}
              <div className="timeline">
                <div className="timeline-item">
                  <span className="label">Created:</span>
                  <span>{new Date(complaint.createdAt).toLocaleString()}</span>
                </div>
                <div className="timeline-item">
                  <span className="label">Resolved:</span>
                  <span>{new Date(complaint.resolvedAt).toLocaleString()}</span>
                </div>
                <div className="timeline-item">
                  <span className="label">Approved:</span>
                  <span>{new Date(complaint.approvedAt).toLocaleString()}</span>
                </div>
              </div>

              {/* Remarks */}
              <div className="remarks-section">
                <div className="remark-box">
                  <strong>Resolution Remarks:</strong>
                  <p>{complaint.resolutionRemarks}</p>
                </div>
                <div className="remark-box">
                  <strong>Approval Remarks:</strong>
                  <p>{complaint.approvalRemarks}</p>
                </div>
              </div>

              {/* Verification */}
              <div className="verification-section">
                <div className="image-verification">
                  <Image size={16} />
                  <span>Before: {complaint.beforeImageCount}</span>
                  <span>After: {complaint.afterImageCount}</span>
                  {complaint.hasResolutionImages ? (
                    <span className="badge badge-success">✓ Verified</span>
                  ) : (
                    <span className="badge badge-warning">⚠ Missing Images</span>
                  )}
                </div>
                
                {complaint.totalRatings > 0 && (
                  <div className="rating-info">
                    <Star size={16} fill="gold" />
                    <span>{complaint.averageRating} ({complaint.totalRatings} ratings)</span>
                  </div>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="card-footer">
              <button 
                className="btn btn-primary"
                onClick={() => window.location.href = `/complaints/${complaint.id}`}
              >
                View Details
              </button>
              <button 
                className="btn btn-success"
                onClick={() => handleClose(complaint.id)}
                disabled={!complaint.hasResolutionImages}
              >
                <CheckCircle size={16} />
                Close Complaint
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="pagination">
        <button 
          disabled={pagination.currentPage === 0}
          onClick={() => fetchQueue(pagination.currentPage - 1)}
        >
          Previous
        </button>
        <span>Page {pagination.currentPage + 1} of {pagination.totalPages}</span>
        <button 
          disabled={pagination.currentPage >= pagination.totalPages - 1}
          onClick={() => fetchQueue(pagination.currentPage + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default AdminClosureApprovalQueue;
```

---

## 📈 Part 2: Resolution Velocity Analytics

### API Endpoint
```http
GET /api/ward-officer/analytics/resolution-velocity
```

### Response Structure
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

### React Component Example

```jsx
import React, { useState, useEffect } from 'react';
import { TrendingUp, Clock, Zap, AlertCircle } from 'lucide-react';

const ResolutionVelocityCard = () => {
  const [velocity, setVelocity] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchVelocity();
  }, []);

  const fetchVelocity = async () => {
    try {
      const response = await fetch('/api/ward-officer/analytics/resolution-velocity', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      setVelocity(data);
    } catch (error) {
      console.error('Error fetching velocity:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (!velocity) return null;

  return (
    <div className="resolution-velocity-card">
      <div className="card-header">
        <h2>
          <TrendingUp size={20} />
          Resolution Velocity
        </h2>
      </div>

      <div className="metrics-grid">
        {/* Average Resolution Time */}
        <div className="metric-card primary">
          <div className="metric-icon">
            <Clock size={24} />
          </div>
          <div className="metric-content">
            <div className="metric-value">
              {velocity.averageResolutionTimeDays} days
            </div>
            <div className="metric-label">Average Resolution Time</div>
            <div className="metric-subtitle">
              ({velocity.averageResolutionTimeHours} hours)
            </div>
          </div>
        </div>

        {/* Fastest Resolution */}
        <div className="metric-card success">
          <div className="metric-icon">
            <Zap size={24} />
          </div>
          <div className="metric-content">
            <div className="metric-value">
              {velocity.fastestResolutionHours}h
            </div>
            <div className="metric-label">Fastest Resolution</div>
            <div className="metric-subtitle">Best Performance</div>
          </div>
        </div>

        {/* Slowest Resolution */}
        <div className="metric-card warning">
          <div className="metric-icon">
            <AlertCircle size={24} />
          </div>
          <div className="metric-content">
            <div className="metric-value">
              {velocity.slowestResolutionHours}h
            </div>
            <div className="metric-label">Slowest Resolution</div>
            <div className="metric-subtitle">Needs Improvement</div>
          </div>
        </div>

        {/* Resolution Rate */}
        <div className="metric-card info">
          <div className="metric-icon">
            <TrendingUp size={24} />
          </div>
          <div className="metric-content">
            <div className="metric-value">
              {velocity.resolutionRate}%
            </div>
            <div className="metric-label">Resolution Rate</div>
            <div className="metric-subtitle">
              {velocity.totalResolved} resolved
            </div>
          </div>
        </div>
      </div>

      {/* Performance Indicator */}
      <div className="performance-indicator">
        <div className="progress-bar">
          <div 
            className="progress-fill"
            style={{ width: `${velocity.resolutionRate}%` }}
          />
        </div>
        <p className="performance-text">
          {velocity.resolutionRate >= 80 ? '🎯 Excellent Performance!' :
           velocity.resolutionRate >= 60 ? '👍 Good Performance' :
           '⚠️ Needs Improvement'}
        </p>
      </div>
    </div>
  );
};

export default ResolutionVelocityCard;
```

---

## 🎨 CSS Styling

```css
/* Closure Approval Queue */
.closure-approval-queue {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.closure-approval-queue .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.complaint-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.complaint-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.card-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.priority-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid;
}

.waiting-time {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.2);
  padding: 8px 16px;
  border-radius: 20px;
}

.card-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.info-item label {
  font-weight: 600;
  color: #666;
  display: block;
  margin-bottom: 4px;
}

.timeline {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.timeline-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #dee2e6;
}

.timeline-item:last-child {
  border-bottom: none;
}

.remarks-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
}

.remark-box {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.remark-box strong {
  display: block;
  margin-bottom: 8px;
  color: #667eea;
}

.verification-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.image-verification {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-footer {
  padding: 20px;
  background: #f8f9fa;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-success {
  background: #48bb78;
  color: white;
}

.btn-success:hover {
  background: #38a169;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Resolution Velocity */
.resolution-velocity-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 24px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.metric-card {
  padding: 20px;
  border-radius: 12px;
  display: flex;
  gap: 15px;
  transition: transform 0.2s;
}

.metric-card:hover {
  transform: translateY(-4px);
}

.metric-card.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.metric-card.success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.metric-card.warning {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  color: white;
}

.metric-card.info {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  color: white;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.metric-label {
  font-size: 14px;
  font-weight: 600;
  opacity: 0.9;
}

.metric-subtitle {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

.performance-indicator {
  margin-top: 30px;
}

.progress-bar {
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #48bb78 0%, #38a169 100%);
  transition: width 0.3s ease;
}

.performance-text {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.pagination button {
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid #667eea;
  background: white;
  color: #667eea;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  background: #667eea;
  color: white;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

---

## 🔄 Workflow Logic

### Automatic List Management

When Admin closes a complaint:

```javascript
const handleClose = async (complaintId) => {
  const remarks = prompt('Enter closure remarks:');
  if (!remarks) return;

  try {
    // 1. Close the complaint
    await fetch(`/api/admin/complaints/${complaintId}/close`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ remarks })
    });
    
    // 2. Refresh the queue
    // The complaint is automatically removed because:
    // - Status changes from APPROVED to CLOSED
    // - The API only returns APPROVED complaints
    fetchQueue(pagination.currentPage);
    
    // 3. Show success message
    showToast('Complaint closed successfully!', 'success');
    
  } catch (error) {
    showToast('Failed to close complaint', 'error');
  }
};
```

---

## 📊 Summary of New Endpoints

### Admin Endpoints
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/admin/complaints/closure-approval-queue` | GET | Get approval-style queue for closure |
| `/api/admin/complaints/pending-closure-tracking` | GET | Detailed tracking view |
| `/api/admin/complaints/closed-tracking` | GET | History of closed complaints |
| `/api/admin/complaints/{id}/close` | PUT | Close a complaint |

### Ward Officer Endpoints
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/ward-officer/analytics/resolution-velocity` | GET | Get resolution time analytics |
| `/api/ward-officer/complaints/closed-tracking` | GET | Ward's closed complaints |

---

## ✅ Key Features Implemented

1. **✅ Closure Approval Queue** - Similar to Ward Officer's approval queue
2. **✅ Automatic List Management** - Complaints removed when closed
3. **✅ Resolution Velocity** - Track average resolution times
4. **✅ Waiting Time Tracking** - Days and hours waiting for closure
5. **✅ Image Verification** - Before/after image counts
6. **✅ Performance Metrics** - Fastest, slowest, average times
7. **✅ Complete Audit Trail** - All remarks and timestamps

This system provides the **best backend and frontend** for efficient complaint closure management! 🚀
