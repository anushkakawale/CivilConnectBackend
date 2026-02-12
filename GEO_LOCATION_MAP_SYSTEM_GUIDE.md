# 🗺️ Geo-Location Map System - Complete Guide

## 📊 Overview
This system provides role-based geo-location mapping of complaints and officer directories for the CivicConnect platform.

---

## 🎯 Role-Based Complaint Visibility on Map

### **CITIZEN** 👤
**Sees:** All complaints in their ward (not just their own)

**Use Case:** Citizens can see what issues are being reported in their neighborhood

**Endpoint:**
```http
GET /api/map/complaints
Authorization: Bearer {citizen_token}
```

**Response:**
```json
[
  {
    "complaintId": 101,
    "latitude": 18.5204,
    "longitude": 73.8567,
    "status": "IN_PROGRESS",
    "slaStatus": "ON_TRACK",
    "title": "Broken Street Light",
    "description": "Street light not working for 3 days",
    "imageUrl": "https://...",
    "departmentName": "Electricity",
    "wardName": "Sector 4",
    "priority": "HIGH",
    "createdAt": "2026-02-10T10:00:00"
  }
]
```

---

### **DEPARTMENT OFFICER** 👷
**Sees:** Only complaints assigned to them

**Use Case:** Officers can see their workload geographically distributed

**Endpoint:**
```http
GET /api/map/complaints
Authorization: Bearer {dept_officer_token}
```

**Response:** Same structure, but filtered to assigned complaints only

---

### **WARD OFFICER** 🏛️
**Sees:** All complaints in their ward

**Use Case:** Ward Officers can monitor all issues in their jurisdiction

**Endpoint:**
```http
GET /api/map/complaints
Authorization: Bearer {ward_officer_token}
```

**Response:** Same structure, all complaints in the ward

---

### **ADMIN** 👨‍💼
**Sees:** All complaints system-wide

**Use Case:** Admin can see city-wide complaint distribution

**Endpoint:**
```http
GET /api/map/complaints
Authorization: Bearer {admin_token}
```

**Response:** Same structure, all complaints in the system

---

## 🎨 Grouped View (All Statuses)

Get complaints grouped by status for better visualization:

**Endpoint:**
```http
GET /api/map/complaints/grouped
Authorization: Bearer {token}
```

**Response:**
```json
{
  "complaintsByStatus": {
    "SUBMITTED": [
      { "complaintId": 101, "latitude": 18.5204, ... },
      { "complaintId": 102, "latitude": 18.5210, ... }
    ],
    "ASSIGNED": [
      { "complaintId": 103, "latitude": 18.5220, ... }
    ],
    "IN_PROGRESS": [
      { "complaintId": 104, "latitude": 18.5230, ... }
    ],
    "RESOLVED": [
      { "complaintId": 105, "latitude": 18.5240, ... }
    ],
    "APPROVED": [
      { "complaintId": 106, "latitude": 18.5250, ... }
    ],
    "CLOSED": [
      { "complaintId": 107, "latitude": 18.5260, ... }
    ]
  },
  "totalComplaints": 150,
  "statusCounts": {
    "SUBMITTED": 10,
    "ASSIGNED": 25,
    "IN_PROGRESS": 40,
    "RESOLVED": 30,
    "APPROVED": 20,
    "CLOSED": 25
  }
}
```

---

## 👥 Officer Directory System

### **CITIZEN** 👤
**Sees:** All department officers in their ward

**Use Case:** Citizens can see which officers are responsible for their area

**Endpoint:**
```http
GET /api/map/officers
Authorization: Bearer {citizen_token}
```

**Response:**
```json
[
  {
    "officerId": 201,
    "name": "Officer Smith",
    "email": "smith@civic.com",
    "mobile": "9876543210",
    "role": "DEPARTMENT_OFFICER",
    "departmentName": "Electricity",
    "wardName": "Sector 4",
    "specialization": "Power Distribution",
    "activeComplaintsCount": 5
  },
  {
    "officerId": 202,
    "name": "Officer Brown",
    "email": "brown@civic.com",
    "mobile": "9876543211",
    "role": "DEPARTMENT_OFFICER",
    "departmentName": "Roads",
    "wardName": "Sector 4",
    "specialization": "Road Maintenance",
    "activeComplaintsCount": 8
  }
]
```

---

### **DEPARTMENT OFFICER** 👷
**Sees:** Peer officers in the same department (excluding self)

**Use Case:** Officers can see their colleagues for collaboration

**Endpoint:**
```http
GET /api/map/officers
Authorization: Bearer {dept_officer_token}
```

**Response:** Same structure, filtered to same department

---

### **WARD OFFICER** 🏛️
**Sees:** All department officers in their ward

**Use Case:** Ward Officers can see all officers they supervise

**Endpoint:**
```http
GET /api/map/officers
Authorization: Bearer {ward_officer_token}
```

**Response:** Same structure, all officers in the ward

---

### **ADMIN** 👨‍💼
**Sees:** All officers system-wide

**Use Case:** Admin can see all officers for management

**Endpoint:**
```http
GET /api/map/officers
Authorization: Bearer {admin_token}
```

**Response:** Same structure, all officers in the system

---

## 🗺️ Frontend Implementation

### Map Component with Markers

```jsx
import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';

const ComplaintMap = () => {
  const [complaints, setComplaints] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState(null);

  useEffect(() => {
    fetchComplaints();
  }, [selectedStatus]);

  const fetchComplaints = async () => {
    const url = selectedStatus 
      ? `/api/map/complaints?status=${selectedStatus}`
      : '/api/map/complaints';
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const data = await response.json();
    setComplaints(data);
  };

  // Custom marker icons by status
  const getMarkerIcon = (status) => {
    const colors = {
      SUBMITTED: '#3b82f6',
      ASSIGNED: '#8b5cf6',
      IN_PROGRESS: '#f59e0b',
      RESOLVED: '#10b981',
      APPROVED: '#06b6d4',
      CLOSED: '#6b7280',
      REJECTED: '#ef4444',
      ESCALATED: '#dc2626'
    };

    return L.divIcon({
      className: 'custom-marker',
      html: `<div style="background-color: ${colors[status]}; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    });
  };

  return (
    <div className="map-container">
      {/* Status Filter */}
      <div className="status-filter">
        <button onClick={() => setSelectedStatus(null)}>All</button>
        <button onClick={() => setSelectedStatus('SUBMITTED')}>Submitted</button>
        <button onClick={() => setSelectedStatus('IN_PROGRESS')}>In Progress</button>
        <button onClick={() => setSelectedStatus('RESOLVED')}>Resolved</button>
        <button onClick={() => setSelectedStatus('CLOSED')}>Closed</button>
      </div>

      {/* Map */}
      <MapContainer 
        center={[18.5204, 73.8567]} 
        zoom={13} 
        style={{ height: '600px', width: '100%' }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />

        {complaints.map(complaint => (
          <Marker
            key={complaint.complaintId}
            position={[complaint.latitude, complaint.longitude]}
            icon={getMarkerIcon(complaint.status)}
          >
            <Popup>
              <div className="complaint-popup">
                <h3>#{complaint.complaintId} - {complaint.title}</h3>
                <p>{complaint.description}</p>
                <div className="popup-details">
                  <span className={`status-badge ${complaint.status}`}>
                    {complaint.status}
                  </span>
                  <span className="department">{complaint.departmentName}</span>
                  <span className="priority">{complaint.priority}</span>
                </div>
                {complaint.imageUrl && (
                  <img src={complaint.imageUrl} alt="Complaint" style={{ width: '100%', marginTop: '10px' }} />
                )}
                <button 
                  onClick={() => window.location.href = `/complaints/${complaint.complaintId}`}
                  className="view-details-btn"
                >
                  View Details
                </button>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default ComplaintMap;
```

---

### Grouped Status Map (Clustered View)

```jsx
import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';

const GroupedComplaintMap = () => {
  const [groupedData, setGroupedData] = useState(null);
  const [visibleStatuses, setVisibleStatuses] = useState({
    SUBMITTED: true,
    ASSIGNED: true,
    IN_PROGRESS: true,
    RESOLVED: true,
    APPROVED: true,
    CLOSED: false
  });

  useEffect(() => {
    fetchGroupedComplaints();
  }, []);

  const fetchGroupedComplaints = async () => {
    const response = await fetch('/api/map/complaints/grouped', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const data = await response.json();
    setGroupedData(data);
  };

  const statusColors = {
    SUBMITTED: '#3b82f6',
    ASSIGNED: '#8b5cf6',
    IN_PROGRESS: '#f59e0b',
    RESOLVED: '#10b981',
    APPROVED: '#06b6d4',
    CLOSED: '#6b7280'
  };

  const toggleStatus = (status) => {
    setVisibleStatuses(prev => ({
      ...prev,
      [status]: !prev[status]
    }));
  };

  if (!groupedData) return <div>Loading...</div>;

  return (
    <div className="grouped-map-container">
      {/* Legend with toggles */}
      <div className="map-legend">
        <h3>Status Legend ({groupedData.totalComplaints} total)</h3>
        {Object.entries(groupedData.statusCounts).map(([status, count]) => (
          <div 
            key={status}
            className="legend-item"
            onClick={() => toggleStatus(status)}
          >
            <input 
              type="checkbox" 
              checked={visibleStatuses[status]}
              onChange={() => {}}
            />
            <span 
              className="legend-color" 
              style={{ backgroundColor: statusColors[status] }}
            />
            <span>{status} ({count})</span>
          </div>
        ))}
      </div>

      {/* Map */}
      <MapContainer 
        center={[18.5204, 73.8567]} 
        zoom={13} 
        style={{ height: '600px', width: '100%' }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {Object.entries(groupedData.complaintsByStatus).map(([status, complaints]) => 
          visibleStatuses[status] && complaints.map(complaint => (
            <CircleMarker
              key={complaint.complaintId}
              center={[complaint.latitude, complaint.longitude]}
              radius={8}
              fillColor={statusColors[status]}
              color="white"
              weight={2}
              fillOpacity={0.8}
            >
              <Popup>
                <div className="complaint-popup">
                  <h4>#{complaint.complaintId}</h4>
                  <p>{complaint.title}</p>
                  <span className={`status ${status}`}>{status}</span>
                </div>
              </Popup>
            </CircleMarker>
          ))
        )}
      </MapContainer>
    </div>
  );
};

export default GroupedComplaintMap;
```

---

### Officer Directory Component

```jsx
import React, { useState, useEffect } from 'react';

const OfficerDirectory = () => {
  const [officers, setOfficers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchOfficers();
  }, []);

  const fetchOfficers = async () => {
    const response = await fetch('/api/map/officers', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const data = await response.json();
    setOfficers(data);
  };

  const filteredOfficers = officers.filter(officer =>
    officer.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    officer.departmentName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="officer-directory">
      <h2>Officer Directory</h2>
      
      <input
        type="text"
        placeholder="Search by name or department..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="search-input"
      />

      <div className="officer-grid">
        {filteredOfficers.map(officer => (
          <div key={officer.officerId} className="officer-card">
            <div className="officer-header">
              <h3>{officer.name}</h3>
              <span className="role-badge">{officer.role}</span>
            </div>
            
            <div className="officer-details">
              <div className="detail-row">
                <span className="label">Department:</span>
                <span>{officer.departmentName}</span>
              </div>
              <div className="detail-row">
                <span className="label">Ward:</span>
                <span>{officer.wardName}</span>
              </div>
              <div className="detail-row">
                <span className="label">Specialization:</span>
                <span>{officer.specialization}</span>
              </div>
              <div className="detail-row">
                <span className="label">Active Complaints:</span>
                <span className="badge">{officer.activeComplaintsCount}</span>
              </div>
            </div>

            <div className="officer-contact">
              <a href={`mailto:${officer.email}`}>📧 {officer.email}</a>
              <a href={`tel:${officer.mobile}`}>📱 {officer.mobile}</a>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OfficerDirectory;
```

---

## 🎨 CSS Styling

```css
.map-container {
  position: relative;
  width: 100%;
  height: 100vh;
}

.status-filter {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1000;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.status-filter button {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  background: #f3f4f6;
  cursor: pointer;
  transition: all 0.2s;
}

.status-filter button:hover {
  background: #667eea;
  color: white;
}

.complaint-popup {
  min-width: 250px;
}

.complaint-popup h3 {
  margin: 0 0 10px 0;
  color: #1f2937;
}

.popup-details {
  display: flex;
  gap: 8px;
  margin: 10px 0;
  flex-wrap: wrap;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.view-details-btn {
  width: 100%;
  padding: 8px;
  margin-top: 10px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* Officer Directory */
.officer-directory {
  padding: 20px;
}

.search-input {
  width: 100%;
  padding: 12px;
  margin-bottom: 20px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
}

.officer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.officer-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.officer-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.officer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.role-badge {
  padding: 4px 12px;
  background: #667eea;
  color: white;
  border-radius: 12px;
  font-size: 12px;
}

.officer-details {
  margin-bottom: 15px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.officer-contact {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.officer-contact a {
  color: #667eea;
  text-decoration: none;
}
```

---

## ✅ Summary

**Endpoints Created:**
- `GET /api/map/complaints` - Role-based complaint map
- `GET /api/map/complaints/grouped` - Grouped by status
- `GET /api/map/officers` - Role-based officer directory

**Role-Based Visibility:**
- ✅ Citizen: Ward complaints + Ward officers
- ✅ Dept Officer: Assigned complaints + Peer officers
- ✅ Ward Officer: Ward complaints + Ward officers
- ✅ Admin: All complaints + All officers

**This provides the BEST geo-location mapping system!** 🚀
