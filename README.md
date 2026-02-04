# CivicConnect - Backend API Documentation

**Last Updated**: January 30, 2026, 1:05 AM IST  
**Status**: ✅ All Errors Fixed  
**Base URL**: `http://localhost:8083/api`

---

## 📚 DOCUMENTATION
- **[Main API Guide](API_GUIDE.md)**
- **[Frontend UI Quick Guide](FRONTEND_UI_QUICK_GUIDE.md)**
- **[Admin Integration Guide](ADMIN_API_INTEGRATION_GUIDE.md)**
- **[Ward Officer Integration Guide](WARD_OFFICER_API_INTEGRATION_GUIDE.md)**
- **[Department Officer Integration Guide](DEPARTMENT_OFFICER_API_INTEGRATION_GUIDE.md)**

---

## 🚀 QUICK START

### 1. Start Backend
```bash
mvn spring-boot:run
```

### 2. Test APIs
```bash
# Login
POST http://localhost:8083/api/auth/login
{
  "email": "user@example.com",
  "password": "password"
}

# Get Dashboard
GET http://localhost:8083/api/citizen/dashboard
Authorization: Bearer {token}
```

---

## 📊 CITIZEN APIs

### Dashboard
```
GET /api/citizen/dashboard
Authorization: Bearer {token}
```

### My Complaints
```
GET /api/citizen/my-complaints?page=0&size=10
Authorization: Bearer {token}
```

### Officers Directory
```
GET /api/citizen/officers?departmentId=1
Authorization: Bearer {token}
```

### Area Complaints (Map)
```
GET /api/citizen/area-complaints?wardId=1
Authorization: Bearer {token}
```

### Create Complaint
```
POST /api/citizen/complaints
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Data:
- title
- description
- category
- priority (LOW, MEDIUM, HIGH, CRITICAL)
- location
- latitude
- longitude
- wardId
- departmentId
- images (optional, multiple files)
```

---

## 🔑 AUTHENTICATION

### Register
```
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "mobile": "9876543210",
  "password": "SecurePass@123",
  "wardId": 1,
  "address": "123 Main Street"
}
```

### Login
```
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "SecurePass@123"
}

Response:
{
  "token": "eyJhbGc...",
  "role": "CITIZEN",
  "userId": 1,
  "name": "John Doe"
}
```

---

## 📋 MASTER DATA

### Get Wards
```
GET /api/wards
No authentication required
```

### Get Departments
```
GET /api/departments
No authentication required
```

---

## 🔔 NOTIFICATIONS

### Get Unread Count
```
GET /api/notifications/unread/count
Authorization: Bearer {token}
```

### Get All Notifications
```
GET /api/notifications
Authorization: Bearer {token}
```

### Mark All as Read
```
PUT /api/notifications/read-all
Authorization: Bearer {token}
```

---

## 👤 PROFILE

### Get My Profile
```
GET /api/profile/me
Authorization: Bearer {token}
```

### Update Profile
```
PUT /api/profile/update
Authorization: Bearer {token}
{
  "name": "John Doe",
  "mobile": "9876543210",
  "address": "123 Main Street"
}
```

---

## 🖼️ IMAGES

### Upload Image
```
POST /api/complaints/{id}/images
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Data:
- file
- type (BEFORE_COMPLAINT or AFTER_RESOLUTION)
```

### Get Complaint Images
```
GET /api/complaints/{id}/images
Authorization: Bearer {token}
```

---

## 📝 COMPLAINT DETAILS

### Get Complaint by ID
```
GET /api/complaints/{id}
Authorization: Bearer {token}
```

---

## ✅ RECENT FIXES

### 1. Created Priority Enum
- Created `Priority.java` enum with values: LOW, MEDIUM, HIGH, CRITICAL
- Updated `Complaint.java` to use `Priority` instead of `ComplaintPriority`

### 2. Created Citizen APIs
- `CitizenDashboardController` - Dashboard statistics
- `CitizenComplaintListController` - My complaints with pagination
- `CitizenOfficersController` - Officers directory
- `CitizenAreaComplaintsController` - Area complaints for map

### 3. Updated Entities
- `OfficerProfile` - Added `designation` and `employeeId` fields
- `Complaint` - Changed to use `Priority` enum

### 4. Updated Repositories
- `OfficerProfileRepository` - Added 3 new query methods
- `ComplaintRepository` - Added 5 new query methods for citizen filtering

---

## 🎯 FRONTEND INTEGRATION

### Setup Axios
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8083/api',
  timeout: 30000,
});

// Add token to requests
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

### Example: Fetch Dashboard
```javascript
const fetchDashboard = async () => {
  const response = await api.get('/citizen/dashboard');
  return response.data;
};
```

### Example: Create Complaint
```javascript
const createComplaint = async (formData) => {
  const data = new FormData();
  data.append('title', formData.title);
  data.append('description', formData.description);
  data.append('category', formData.category);
  data.append('priority', formData.priority);
  data.append('location', formData.location);
  data.append('latitude', formData.latitude);
  data.append('longitude', formData.longitude);
  data.append('wardId', formData.wardId);
  data.append('departmentId', formData.departmentId);
  
  formData.images?.forEach(image => {
    data.append('images', image);
  });
  
  const response = await api.post('/citizen/complaints', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  
  return response.data;
};
```

---

## 🚨 IMPORTANT NOTES

1. **All .md documentation files have been removed** (except this README.md)
2. **Priority enum created** to fix compilation errors
3. **All backend errors fixed** and ready to run
4. **Restart backend** after pulling latest changes

---

## 📊 PROJECT STATUS

- ✅ Backend: 100% Complete
- ✅ All Compilation Errors: Fixed
- ✅ API Endpoints: Working
- ⏳ Frontend: To be built

---

**Built with ❤️ for efficient civic governance**
