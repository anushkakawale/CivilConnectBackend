// Profile Management Frontend Integration Guide
// Complete React/JavaScript implementation examples for CivicConnect Profile APIs

// ============================================================================
// 1. API SERVICE LAYER
// ============================================================================

// File: src/services/profileService.js

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8083/api';

/**
 * Get authentication token from localStorage
 */
const getAuthToken = () => {
    return localStorage.getItem('token');
};

/**
 * Common headers for API requests
 */
const getHeaders = (includeContentType = true) => {
    const headers = {
        'Authorization': `Bearer ${getAuthToken()}`
    };

    if (includeContentType) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
};

/**
 * Profile Service - All profile-related API calls
 */
export const profileService = {

    // ========== COMMON PROFILE APIs ==========

    /**
     * Get complete profile for current user
     */
    getProfile: async () => {
        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch profile');
        }

        return response.json();
    },

    /**
     * Update user's name
     */
    updateName: async (name) => {
        const response = await fetch(`${API_BASE_URL}/profile/name`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({ name })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to update name');
        }

        return response.text();
    },

    /**
     * Change password
     */
    changePassword: async (currentPassword, newPassword) => {
        const response = await fetch(`${API_BASE_URL}/profile/password`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({ currentPassword, newPassword })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to change password');
        }

        return response.text();
    },


    /**
     * Get profile completion score (0-100)
     */
    getCompletionScore: async () => {
        const response = await fetch(`${API_BASE_URL}/profile/completion-score`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch completion score');
        }

        return response.json(); // Returns number
    },

    /**
     * Get detailed completion breakdown
     */
    getCompletionBreakdown: async () => {
        const response = await fetch(`${API_BASE_URL}/profile/completion-breakdown`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch completion breakdown');
        }

        return response.json(); // { totalScore, role, fields: [...] }
    },

    // ========== CITIZEN-SPECIFIC APIs ==========

    /**
     * Get citizen profile
     */
    getCitizenProfile: async () => {
        const response = await fetch(`${API_BASE_URL}/profile/citizen`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch citizen profile');
        }

        return response.json();
    },

    /**
     * Update citizen address
     */
    updateAddress: async (addressData) => {
        const response = await fetch(`${API_BASE_URL}/profile/citizen/address`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify(addressData)
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to update address');
        }

        return response.text();
    },

    /**
     * Request ward change (requires approval)
     */
    requestWardChange: async (wardId) => {
        const response = await fetch(`${API_BASE_URL}/profile/citizen/ward`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({ wardId })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to request ward change');
        }

        return response.text();
    },

    // ========== OFFICER-SPECIFIC APIs ==========

    /**
     * Get ward officer profile
     */
    getWardOfficerProfile: async () => {
        const response = await fetch(`${API_BASE_URL}/ward-officer/profile`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch ward officer profile');
        }

        return response.json();
    },

    /**
     * Get department officer profile
     */
    getDepartmentOfficerProfile: async () => {
        const response = await fetch(`${API_BASE_URL}/department/profile`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch department officer profile');
        }

        return response.json();
    },

    // ========== ADMIN-SPECIFIC APIs ==========

    /**
     * Get admin profile
     */
    getAdminProfile: async () => {
        const response = await fetch(`${API_BASE_URL}/admin/profile`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch admin profile');
        }

        return response.json();
    }
};


// ============================================================================
// 2. REACT COMPONENTS
// ============================================================================

// File: src/components/Profile/ProfileCompletionWidget.jsx

import React, { useState, useEffect } from 'react';
import { profileService } from '../../services/profileService';
import './ProfileCompletionWidget.css';

/**
 * Profile Completion Widget
 * Shows completion score and breakdown of fields
 */
export const ProfileCompletionWidget = () => {
    const [breakdown, setBreakdown] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchBreakdown();
    }, []);

    const fetchBreakdown = async () => {
        try {
            setLoading(true);
            const data = await profileService.getCompletionBreakdown();
            setBreakdown(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="completion-widget loading">Loading...</div>;
    }

    if (error) {
        return <div className="completion-widget error">Error: {error}</div>;
    }

    if (!breakdown) {
        return null;
    }

    const { totalScore, fields } = breakdown;
    const isComplete = totalScore === 100;

    return (
        <div className="completion-widget">
            <div className="completion-header">
                <h3>Profile Completion</h3>
                <span className={`score ${isComplete ? 'complete' : 'incomplete'}`}>
                    {totalScore}%
                </span>
            </div>

            {/* Progress Bar */}
            <div className="progress-bar-container">
                <div
                    className="progress-bar"
                    style={{ width: `${totalScore}%` }}
                >
                    <span className="progress-text">{totalScore}%</span>
                </div>
            </div>

            {/* Field Checklist */}
            <div className="fields-checklist">
                {fields.map((field, index) => (
                    <div
                        key={index}
                        className={`field-item ${field.completed ? 'completed' : 'incomplete'}`}
                    >
                        <div className="field-info">
                            <span className="field-icon">
                                {field.completed ? '✓' : '○'}
                            </span>
                            <span className="field-name">{field.name}</span>
                            {!field.editable && (
                                <span className="badge admin-assigned">Admin Assigned</span>
                            )}
                        </div>
                        <div className="field-weight">
                            {field.weight > 0 && `${field.weight}%`}
                        </div>
                    </div>
                ))}
            </div>

            {!isComplete && (
                <div className="completion-message">
                    <p>Complete your profile to unlock all features!</p>
                </div>
            )}
        </div>
    );
};


// File: src/components/Profile/ProfileForm.jsx

import React, { useState, useEffect } from 'react';
import { profileService } from '../../services/profileService';
import './ProfileForm.css';

/**
 * Profile Form Component
 * Handles profile viewing and editing
 */
export const ProfileForm = () => {
    const [profile, setProfile] = useState(null);
    const [editing, setEditing] = useState(false);
    const [formData, setFormData] = useState({});
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            setLoading(true);
            const data = await profileService.getProfile();
            setProfile(data);
            setFormData(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleNameUpdate = async () => {
        try {
            setSaving(true);
            await profileService.updateName(formData.name);
            setSuccess('Name updated successfully!');
            await fetchProfile();
            setEditing(false);
            setTimeout(() => setSuccess(null), 3000);
        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    };

    const handleAddressUpdate = async () => {
        try {
            setSaving(true);
            const addressData = {
                addressLine1: formData.addressLine1,
                addressLine2: formData.addressLine2,
                city: formData.city,
                pincode: formData.pincode
            };
            await profileService.updateAddress(addressData);
            setSuccess('Address updated successfully!');
            await fetchProfile();
            setEditing(false);
            setTimeout(() => setSuccess(null), 3000);
        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return <div className="profile-form loading">Loading profile...</div>;
    }

    if (!profile) {
        return <div className="profile-form error">Profile not found</div>;
    }

    const isCitizen = profile.role === 'CITIZEN';

    return (
        <div className="profile-form">
            <div className="profile-header">
                <h2>My Profile</h2>
                <button
                    className="btn-edit"
                    onClick={() => setEditing(!editing)}
                >
                    {editing ? 'Cancel' : 'Edit Profile'}
                </button>
            </div>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            {/* Basic Information */}
            <div className="form-section">
                <h3>Basic Information</h3>

                <div className="form-group">
                    <label>Name</label>
                    {editing ? (
                        <input
                            type="text"
                            name="name"
                            value={formData.name || ''}
                            onChange={handleInputChange}
                        />
                    ) : (
                        <p>{profile.name}</p>
                    )}
                </div>

                <div className="form-group">
                    <label>Email</label>
                    <p>{profile.email}</p>
                    <span className="field-note">Email cannot be changed</span>
                </div>

                <div className="form-group">
                    <label>Mobile</label>
                    <p>{profile.mobile}</p>
                    <span className="field-note">Mobile cannot be changed</span>
                </div>

                <div className="form-group">
                    <label>Role</label>
                    <p className="badge role-badge">{profile.role}</p>
                </div>
            </div>

            {/* Citizen-specific fields */}
            {isCitizen && (
                <div className="form-section">
                    <h3>Address Information</h3>

                    <div className="form-group">
                        <label>Ward</label>
                        <p>{profile.areaName || 'Not set'}</p>
                        {editing && (
                            <button className="btn-link">Request Ward Change</button>
                        )}
                    </div>

                    <div className="form-group">
                        <label>Address Line 1</label>
                        {editing ? (
                            <input
                                type="text"
                                name="addressLine1"
                                value={formData.addressLine1 || ''}
                                onChange={handleInputChange}
                            />
                        ) : (
                            <p>{profile.addressLine1 || 'Not set'}</p>
                        )}
                    </div>

                    <div className="form-group">
                        <label>Address Line 2</label>
                        {editing ? (
                            <input
                                type="text"
                                name="addressLine2"
                                value={formData.addressLine2 || ''}
                                onChange={handleInputChange}
                            />
                        ) : (
                            <p>{profile.addressLine2 || 'Not set'}</p>
                        )}
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label>City</label>
                            {editing ? (
                                <input
                                    type="text"
                                    name="city"
                                    value={formData.city || ''}
                                    onChange={handleInputChange}
                                />
                            ) : (
                                <p>{profile.city || 'Not set'}</p>
                            )}
                        </div>

                        <div className="form-group">
                            <label>Pincode</label>
                            {editing ? (
                                <input
                                    type="text"
                                    name="pincode"
                                    value={formData.pincode || ''}
                                    onChange={handleInputChange}
                                    maxLength="6"
                                />
                            ) : (
                                <p>{profile.pincode || 'Not set'}</p>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Officer-specific fields (read-only) */}
            {(profile.role === 'WARD_OFFICER' || profile.role === 'DEPARTMENT_OFFICER') && (
                <div className="form-section">
                    <h3>Assignment Information</h3>

                    {profile.ward && (
                        <div className="form-group">
                            <label>Ward</label>
                            <p>{profile.ward}</p>
                            <span className="field-note">Assigned by admin</span>
                        </div>
                    )}

                    {profile.department && (
                        <div className="form-group">
                            <label>Department</label>
                            <p>{profile.department}</p>
                            <span className="field-note">Assigned by admin</span>
                        </div>
                    )}

                    {profile.designation && (
                        <div className="form-group">
                            <label>Designation</label>
                            <p>{profile.designation}</p>
                        </div>
                    )}

                    {profile.employeeId && (
                        <div className="form-group">
                            <label>Employee ID</label>
                            <p>{profile.employeeId}</p>
                        </div>
                    )}
                </div>
            )}

            {/* Action Buttons */}
            {editing && (
                <div className="form-actions">
                    <button
                        className="btn-primary"
                        onClick={isCitizen ? handleAddressUpdate : handleNameUpdate}
                        disabled={saving}
                    >
                        {saving ? 'Saving...' : 'Save Changes'}
                    </button>
                    <button
                        className="btn-secondary"
                        onClick={() => {
                            setEditing(false);
                            setFormData(profile);
                        }}
                        disabled={saving}
                    >
                        Cancel
                    </button>
                </div>
            )}
        </div>
    );
};




// ============================================================================
// 3. CSS STYLES
// ============================================================================

// File: src/components/Profile/ProfileCompletionWidget.css

/*
.completion-widget {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.completion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.completion-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.score {
  font-size: 24px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 8px;
}

.score.complete {
  background: #10b981;
  color: white;
}

.score.incomplete {
  background: #f59e0b;
  color: white;
}

.progress-bar-container {
  width: 100%;
  height: 32px;
  background: #e5e7eb;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: width 0.3s ease;
}

.progress-text {
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.fields-checklist {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.field-item.completed {
  background: #f0fdf4;
  border-left: 4px solid #10b981;
}

.field-item.incomplete {
  background: #fef3c7;
  border-left: 4px solid #f59e0b;
}

.field-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.field-icon {
  font-size: 20px;
  font-weight: bold;
}

.field-item.completed .field-icon {
  color: #10b981;
}

.field-item.incomplete .field-icon {
  color: #f59e0b;
}

.field-name {
  font-weight: 500;
  color: #374151;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge.admin-assigned {
  background: #dbeafe;
  color: #1e40af;
}

.field-weight {
  font-weight: 600;
  color: #6b7280;
}

.completion-message {
  margin-top: 16px;
  padding: 12px;
  background: #eff6ff;
  border-radius: 8px;
  text-align: center;
}

.completion-message p {
  margin: 0;
  color: #1e40af;
  font-weight: 500;
}
*/


// ============================================================================
// 4. USAGE EXAMPLE
// ============================================================================

// File: src/pages/ProfilePage.jsx

import React from 'react';
import { ProfileCompletionWidget } from '../components/Profile/ProfileCompletionWidget';
import { ProfileForm } from '../components/Profile/ProfileForm';

export const ProfilePage = () => {

    return (
        <div className="profile-page">
            <div className="profile-container">
                <div className="profile-sidebar">
                    <ProfileCompletionWidget />
                </div>

                <div className="profile-main">
                    <ProfileForm />
                </div>
            </div>
        </div>
    );
};


// ============================================================================
// 5. VALIDATION UTILITIES
// ============================================================================

// File: src/utils/profileValidation.js

/**
 * Validation utilities for profile data
 */
export const profileValidation = {

    /**
     * Validate name
     */
    validateName: (name) => {
        if (!name || name.trim().length === 0) {
            return 'Name is required';
        }
        if (name.trim().length < 2) {
            return 'Name must be at least 2 characters';
        }
        if (name.trim().length > 100) {
            return 'Name must be less than 100 characters';
        }
        return null;
    },

    /**
     * Validate mobile number (Indian format)
     */
    validateMobile: (mobile) => {
        if (!mobile) {
            return 'Mobile number is required';
        }
        const mobileRegex = /^[6-9][0-9]{9}$/;
        if (!mobileRegex.test(mobile)) {
            return 'Invalid mobile number. Must be 10 digits starting with 6-9';
        }
        return null;
    },

    /**
     * Validate email
     */
    validateEmail: (email) => {
        if (!email) {
            return 'Email is required';
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            return 'Invalid email format';
        }
        return null;
    },

    /**
     * Validate pincode (Indian format)
     */
    validatePincode: (pincode) => {
        if (!pincode) {
            return null; // Optional field
        }
        const pincodeRegex = /^[1-9][0-9]{5}$/;
        if (!pincodeRegex.test(pincode)) {
            return 'Invalid pincode. Must be 6 digits';
        }
        return null;
    },

    /**
     * Validate password
     */
    validatePassword: (password) => {
        if (!password) {
            return 'Password is required';
        }
        if (password.length < 8) {
            return 'Password must be at least 8 characters';
        }
        if (!/[A-Z]/.test(password)) {
            return 'Password must contain at least one uppercase letter';
        }
        if (!/[a-z]/.test(password)) {
            return 'Password must contain at least one lowercase letter';
        }
        if (!/[0-9]/.test(password)) {
            return 'Password must contain at least one number';
        }
        return null;
    }
};


// ============================================================================
// 6. CUSTOM HOOKS
// ============================================================================

// File: src/hooks/useProfile.js

import { useState, useEffect } from 'react';
import { profileService } from '../services/profileService';

/**
 * Custom hook for profile management
 */
export const useProfile = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchProfile = async () => {
        try {
            setLoading(true);
            const data = await profileService.getProfile();
            setProfile(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProfile();
    }, []);

    return {
        profile,
        loading,
        error,
        refetch: fetchProfile
    };
};

/**
 * Custom hook for profile completion
 */
export const useProfileCompletion = () => {
    const [breakdown, setBreakdown] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchBreakdown = async () => {
        try {
            setLoading(true);
            const data = await profileService.getCompletionBreakdown();
            setBreakdown(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchBreakdown();
    }, []);

    return {
        breakdown,
        loading,
        error,
        refetch: fetchBreakdown
    };
};
