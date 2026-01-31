# CivicConnect Frontend UI & Profile Guide

This guide ensures a consistent, professional, and functional UI for all dashboards (Citizen, Ward Officer, Department Officer, Admin).

## 🎨 Unified Design Philosophy
- **Color Palette:** Use a single primary color (Blue/Purple ecosystem) with varying shades.
  - **Primary:** `#2563EB` (Royal Blue)
  - **Secondary/Accent:** `#3B82F6` (Lighter Blue)
  - **Background:** `#F3F4F6` (Light Gray/White smoke)
  - **Card Bg:** `#FFFFFF` (Pure White)
  - **Text:** `#1F2937` (Dark Gray) for readability.
- **Inputs:**
  - **Editable:** White background (`#FFFFFF`) with border.
  - **Read-only:** Light gray/blue-tinted background (`#EFF6FF`) to clearly indicate "non-editable".

## 👤 Profile Page UI Requirement
**Goal:** A simplified "Single Edit Mode" interface.

### Behavior
1.  **Default View:** All fields are **disabled** (read-only). Backgrounds are light gray.
2.  **Edit Action:** One "Edit Profile" button at the top right (or bottom).
3.  **Edit Mode:**
    - "Name" field turns **White** and becomes editable.
    - "Email", "Role", "Ward", "Department" remain **Gray** (non-editable).
    - "Edit Profile" button changes to "Save Changes".
    - A "Cancel" button appears.

### 💻 React Implementation Example

```jsx
import React, { useState, useEffect } from 'react';
import axios from './api'; // method that adds token

const ProfilePage = () => {
    const [user, setUser] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [editName, setEditName] = useState("");

    useEffect(() => {
        // Fetch User Profile
        axios.get('/api/profile').then(res => {
            setUser(res.data);
            setEditName(res.data.name);
        });
    }, []);

    const handleSave = () => {
        axios.put('/api/profile/name', { name: editName })
            .then(() => {
                alert("Updated!");
                setIsEditing(false);
                setUser({ ...user, name: editName });
            });
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="p-8 bg-gray-100 min-h-screen flex justify-center">
            <div className="bg-white p-8 rounded-xl shadow-lg w-full max-w-2xl">
                
                {/* HEADER */}
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-blue-700">My Profile</h2>
                    {!isEditing ? (
                        <button 
                            onClick={() => setIsEditing(true)}
                            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                        >
                            ✏️ Edit Profile
                        </button>
                    ) : (
                        <div className="flex gap-2">
                            <button 
                                onClick={handleSave}
                                className="bg-green-600 text-white px-4 py-2 rounded"
                            >
                                ✅ Save
                            </button>
                            <button 
                                onClick={() => setIsEditing(false)}
                                className="bg-gray-400 text-white px-4 py-2 rounded"
                            >
                                ❌ Cancel
                            </button>
                        </div>
                    )}
                </div>

                {/* FIELDS GRID */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    
                    {/* NAME (Editable) */}
                    <div className="col-span-2">
                        <label className="block text-sm font-semibold text-gray-600">Full Name</label>
                        <input 
                            type="text" 
                            disabled={!isEditing}
                            value={editName}
                            onChange={(e) => setEditName(e.target.value)}
                            className={`w-full p-2 mt-1 rounded border 
                                ${isEditing ? 'bg-white border-blue-500' : 'bg-blue-50 border-gray-200 text-gray-500'}
                            `}
                        />
                    </div>

                    {/* EMAIL (Read-Only) */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-600">Email</label>
                        <input type="text" value={user.email} disabled className="w-full p-2 mt-1 rounded border bg-gray-100 text-gray-500 cursor-not-allowed" />
                    </div>

                    {/* ROLE (Read-Only) */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-600">Role</label>
                        <input type="text" value={user.role} disabled className="w-full p-2 mt-1 rounded border bg-gray-100 text-gray-500 cursor-not-allowed" />
                    </div>

                    {/* DYNAMIC FIELDS BASED ON ROLE */}
                    {user.wardNumber && (
                        <div>
                            <label className="block text-sm font-semibold text-gray-600">Ward</label>
                            <input type="text" value={`${user.wardNumber} - ${user.areaName}`} disabled className="w-full p-2 mt-1 rounded border bg-gray-100 text-gray-500 cursor-not-allowed" />
                        </div>
                    )}

                    {user.departmentName && (
                        <div>
                            <label className="block text-sm font-semibold text-gray-600">Department</label>
                            <input type="text" value={user.departmentName} disabled className="w-full p-2 mt-1 rounded border bg-gray-100 text-gray-500 cursor-not-allowed" />
                        </div>
                    )}

                     {user.designation && (
                        <div>
                            <label className="block text-sm font-semibold text-gray-600">Designation</label>
                            <input type="text" value={user.designation} disabled className="w-full p-2 mt-1 rounded border bg-gray-100 text-gray-500 cursor-not-allowed" />
                        </div>
                    )}

                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
```

## 🔄 Backend Controller Check

We have verified the backend controllers:

1.  **`ProfileController`** (`/api/profile`) is the main entry point for:
    *   **Ward Officers**
    *   **Department Officers**
    *   **Admins**
    *   **Citizens** (Basic info)
    *   It now correctly returns `departmentName`, `designation`, etc., dynamically based on the role.

2.  **`CitizenProfileController`** (`/api/profile/citizen`) is for **Citizen-specific** actions:
    *   `PUT /ward` (Request ward change)
    *   `PUT /address` (Update address)

3.  **`MobileOtpController`** (`/api/profile/mobile`) handles mobile updates for **EVERYONE**:
    *   `POST /request-otp`
    *   `POST /verify-otp`

✅ **Conclusion:** The profile system is fully operational for all 4 roles. Use the `/api/profile` endpoint for the unified profile page.
