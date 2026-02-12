// ============================================
// CITIZEN COMPLAINT API SERVICE
// Frontend Integration Guide for React/JavaScript
// ============================================

import axios from 'axios';

const API_BASE_URL = 'http://localhost:8083/api';

// ============================================
// AXIOS INSTANCE WITH AUTH
// ============================================

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add auth token to all requests
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// ============================================
// COMPLAINT API FUNCTIONS
// ============================================

export const complaintAPI = {

    // 1️⃣ REGISTER COMPLAINT (JSON - No Images)
    registerComplaint: async (complaintData) => {
        try {
            const response = await api.post('/citizens/complaints', complaintData);
            return response.data;
        } catch (error) {
            console.error('Error registering complaint:', error);
            throw error;
        }
    },

    // 1️⃣ REGISTER COMPLAINT (FormData - With Images)
    registerComplaintWithImages: async (complaintData, images) => {
        try {
            const formData = new FormData();

            // Add complaint data
            formData.append('title', complaintData.title);
            formData.append('description', complaintData.description);
            formData.append('latitude', complaintData.latitude);
            formData.append('longitude', complaintData.longitude);
            formData.append('address', complaintData.address);
            formData.append('departmentId', complaintData.departmentId);
            formData.append('priority', complaintData.priority || 'MEDIUM');

            // Add images
            if (images && images.length > 0) {
                images.forEach((image) => {
                    formData.append('images', image);
                });
            }

            const response = await axios.post(
                `${API_BASE_URL}/citizens/complaints`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    },
                }
            );

            return response.data;
        } catch (error) {
            console.error('Error registering complaint with images:', error);
            throw error;
        }
    },

    // 2️⃣ GET MY COMPLAINTS (Paginated)
    getMyComplaints: async (page = 0, size = 10, filters = {}) => {
        try {
            const params = {
                page,
                size,
                ...filters, // { status, priority, slaStatus }
            };

            const response = await api.get('/citizens/complaints', { params });
            return response.data;
        } catch (error) {
            console.error('Error fetching complaints:', error);
            throw error;
        }
    },

    // 3️⃣ GET WARD COMPLAINTS
    getWardComplaints: async (page = 0, size = 20) => {
        try {
            const response = await api.get('/citizens/complaints/ward', {
                params: { page, size },
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching ward complaints:', error);
            throw error;
        }
    },

    // 4️⃣ GET COMPLAINT DETAILS
    getComplaintDetails: async (complaintId) => {
        try {
            const response = await api.get(`/citizens/complaints/${complaintId}`);
            return response.data;
        } catch (error) {
            console.error('Error fetching complaint details:', error);
            throw error;
        }
    },

    // 5️⃣ GET COMPLAINT TIMELINE
    getComplaintTimeline: async (complaintId) => {
        try {
            const response = await api.get(`/citizens/complaints/${complaintId}/timeline`);
            return response.data;
        } catch (error) {
            console.error('Error fetching timeline:', error);
            throw error;
        }
    },

    // 6️⃣ GET SLA DETAILS
    getSLADetails: async (complaintId) => {
        try {
            const response = await api.get(`/citizens/complaints/${complaintId}/sla`);
            return response.data;
        } catch (error) {
            console.error('Error fetching SLA details:', error);
            throw error;
        }
    },

    // 7️⃣ GET SLA COUNTDOWN
    getSLACountdown: async (complaintId) => {
        try {
            const response = await api.get(`/citizens/complaints/${complaintId}/sla/countdown`);
            return response.data;
        } catch (error) {
            console.error('Error fetching SLA countdown:', error);
            throw error;
        }
    },

    // 8️⃣ REOPEN COMPLAINT
    reopenComplaint: async (complaintId, remarks) => {
        try {
            const response = await api.put(`/citizens/complaints/${complaintId}/reopen`, {
                remarks,
            });
            return response.data;
        } catch (error) {
            console.error('Error reopening complaint:', error);
            throw error;
        }
    },

    // 9️⃣ SUBMIT FEEDBACK
    submitFeedback: async (complaintId, rating, comments) => {
        try {
            const response = await api.put(`/citizens/complaints/${complaintId}/feedback`, {
                rating,
                comments,
            });
            return response.data;
        } catch (error) {
            console.error('Error submitting feedback:', error);
            throw error;
        }
    },
};

// ============================================
// REFERENCE DATA API
// ============================================

export const referenceAPI = {

    // GET ALL WARDS
    getWards: async () => {
        try {
            const response = await axios.get(`${API_BASE_URL}/wards`);
            return response.data;
        } catch (error) {
            console.error('Error fetching wards:', error);
            throw error;
        }
    },

    // GET ALL DEPARTMENTS
    getDepartments: async () => {
        try {
            const response = await axios.get(`${API_BASE_URL}/departments`);
            return response.data;
        } catch (error) {
            console.error('Error fetching departments:', error);
            throw error;
        }
    },
};

// ============================================
// REACT COMPONENT EXAMPLES
// ============================================

// EXAMPLE 1: Register Complaint Component
export const RegisterComplaintExample = () => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        latitude: 0,
        longitude: 0,
        address: '',
        departmentId: '',
        priority: 'MEDIUM',
    });
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const result = await complaintAPI.registerComplaintWithImages(formData, images);
            console.log('Complaint registered:', result);
            alert(`Complaint registered successfully! ID: ${result.complaintId}`);
            // Reset form or redirect
        } catch (error) {
            console.error('Error:', error);
            alert('Failed to register complaint. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleImageChange = (e) => {
        setImages(Array.from(e.target.files));
    };

    return (
        <form onSubmit={handleSubmit}>
            <input
                type="text"
                placeholder="Title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                required
            />
            <textarea
                placeholder="Description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
            />
            <input
                type="text"
                placeholder="Address"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                required
            />
            <select
                value={formData.departmentId}
                onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                required
            >
                <option value="">Select Department</option>
                <option value="1">Electrical</option>
                <option value="2">Roads</option>
                <option value="3">Sanitation</option>
            </select>
            <select
                value={formData.priority}
                onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
            >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
            </select>
            <input
                type="file"
                multiple
                accept="image/*"
                onChange={handleImageChange}
            />
            <button type="submit" disabled={loading}>
                {loading ? 'Submitting...' : 'Register Complaint'}
            </button>
        </form>
    );
};

// EXAMPLE 2: My Complaints List Component
export const MyComplaintsExample = () => {
    const [complaints, setComplaints] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [filters, setFilters] = useState({});
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchComplaints();
    }, [page, filters]);

    const fetchComplaints = async () => {
        setLoading(true);
        try {
            const data = await complaintAPI.getMyComplaints(page, 10, filters);
            setComplaints(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.error('Error fetching complaints:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h2>My Complaints</h2>

            {/* Filters */}
            <div>
                <select onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
                    <option value="">All Status</option>
                    <option value="PENDING">Pending</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="RESOLVED">Resolved</option>
                </select>

                <select onChange={(e) => setFilters({ ...filters, priority: e.target.value })}>
                    <option value="">All Priority</option>
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                </select>
            </div>

            {/* Complaints List */}
            {loading ? (
                <p>Loading...</p>
            ) : (
                <div>
                    {complaints.map((complaint) => (
                        <div key={complaint.complaintId} className="complaint-card">
                            <h3>{complaint.title}</h3>
                            <p>Status: {complaint.status}</p>
                            <p>Priority: {complaint.priority}</p>
                            <p>Department: {complaint.departmentName}</p>
                            <p>Created: {new Date(complaint.createdAt).toLocaleDateString()}</p>
                            {complaint.imageUrl && <img src={complaint.imageUrl} alt="Complaint" />}
                        </div>
                    ))}
                </div>
            )}

            {/* Pagination */}
            <div>
                <button onClick={() => setPage(page - 1)} disabled={page === 0}>
                    Previous
                </button>
                <span>Page {page + 1} of {totalPages}</span>
                <button onClick={() => setPage(page + 1)} disabled={page >= totalPages - 1}>
                    Next
                </button>
            </div>
        </div>
    );
};

// EXAMPLE 3: Complaint Details Component
export const ComplaintDetailsExample = ({ complaintId }) => {
    const [complaint, setComplaint] = useState(null);
    const [timeline, setTimeline] = useState([]);
    const [slaCountdown, setSlaCountdown] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchComplaintData();
    }, [complaintId]);

    const fetchComplaintData = async () => {
        try {
            const [details, timelineData, slaData] = await Promise.all([
                complaintAPI.getComplaintDetails(complaintId),
                complaintAPI.getComplaintTimeline(complaintId),
                complaintAPI.getSLACountdown(complaintId),
            ]);

            setComplaint(details);
            setTimeline(timelineData);
            setSlaCountdown(slaData);
        } catch (error) {
            console.error('Error fetching complaint data:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <p>Loading...</p>;
    if (!complaint) return <p>Complaint not found</p>;

    return (
        <div>
            <h2>{complaint.title}</h2>
            <p>{complaint.description}</p>

            <div>
                <strong>Status:</strong> {complaint.status}
            </div>
            <div>
                <strong>Priority:</strong> {complaint.priority}
            </div>
            <div>
                <strong>Department:</strong> {complaint.departmentName}
            </div>
            <div>
                <strong>Ward:</strong> {complaint.wardName}
            </div>

            {/* SLA Countdown */}
            {slaCountdown && (
                <div className={slaCountdown.breached ? 'sla-breached' : 'sla-on-track'}>
                    <strong>SLA Status:</strong> {slaCountdown.status}
                    <br />
                    <strong>Time Remaining:</strong> {slaCountdown.remainingHours.toFixed(1)} hours
                    {slaCountdown.breached && <span> (BREACHED)</span>}
                </div>
            )}

            {/* Assigned Officer */}
            {complaint.assignedOfficer && (
                <div>
                    <h3>Assigned Officer</h3>
                    <p>Name: {complaint.assignedOfficer.name}</p>
                    <p>Mobile: {complaint.assignedOfficer.mobile}</p>
                    <p>Email: {complaint.assignedOfficer.email}</p>
                </div>
            )}

            {/* Images */}
            {complaint.images && complaint.images.length > 0 && (
                <div>
                    <h3>Images</h3>
                    {complaint.images.map((img) => (
                        <img key={img.imageId} src={img.imageUrl} alt="Complaint" />
                    ))}
                </div>
            )}

            {/* Timeline */}
            <div>
                <h3>Timeline</h3>
                {timeline.map((event, index) => (
                    <div key={index} className="timeline-event">
                        <strong>{event.status}</strong>
                        <p>{new Date(event.timestamp).toLocaleString()}</p>
                        <p>{event.remarks}</p>
                        <small>By: {event.updatedBy}</small>
                    </div>
                ))}
            </div>

            {/* Actions */}
            {complaint.status === 'RESOLVED' && (
                <button onClick={() => handleSubmitFeedback(complaintId)}>
                    Submit Feedback
                </button>
            )}
        </div>
    );
};

// EXAMPLE 4: Submit Feedback Component
export const SubmitFeedbackExample = ({ complaintId, onSuccess }) => {
    const [rating, setRating] = useState(5);
    const [comments, setComments] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            await complaintAPI.submitFeedback(complaintId, rating, comments);
            alert('Feedback submitted successfully!');
            if (onSuccess) onSuccess();
        } catch (error) {
            console.error('Error submitting feedback:', error);
            alert('Failed to submit feedback');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>Submit Feedback</h3>

            <div>
                <label>Rating:</label>
                <select value={rating} onChange={(e) => setRating(Number(e.target.value))}>
                    <option value="1">1 - Very Poor</option>
                    <option value="2">2 - Poor</option>
                    <option value="3">3 - Average</option>
                    <option value="4">4 - Good</option>
                    <option value="5">5 - Excellent</option>
                </select>
            </div>

            <div>
                <label>Comments:</label>
                <textarea
                    value={comments}
                    onChange={(e) => setComments(e.target.value)}
                    placeholder="Share your experience..."
                    rows="4"
                />
            </div>

            <button type="submit" disabled={loading}>
                {loading ? 'Submitting...' : 'Submit Feedback'}
            </button>
        </form>
    );
};

// ============================================
// CUSTOM HOOKS
// ============================================

// Hook for fetching complaints with auto-refresh
export const useComplaints = (filters = {}, autoRefresh = false) => {
    const [complaints, setComplaints] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchComplaints = useCallback(async () => {
        setLoading(true);
        try {
            const data = await complaintAPI.getMyComplaints(page, 10, filters);
            setComplaints(data.content);
            setTotalPages(data.totalPages);
            setError(null);
        } catch (err) {
            setError(err);
        } finally {
            setLoading(false);
        }
    }, [page, filters]);

    useEffect(() => {
        fetchComplaints();

        if (autoRefresh) {
            const interval = setInterval(fetchComplaints, 30000); // Refresh every 30 seconds
            return () => clearInterval(interval);
        }
    }, [fetchComplaints, autoRefresh]);

    return { complaints, loading, error, page, setPage, totalPages, refresh: fetchComplaints };
};

// Hook for SLA countdown with real-time updates
export const useSLACountdown = (complaintId) => {
    const [sla, setSla] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchSLA = async () => {
            try {
                const data = await complaintAPI.getSLACountdown(complaintId);
                setSla(data);
            } catch (error) {
                console.error('Error fetching SLA:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchSLA();
        const interval = setInterval(fetchSLA, 60000); // Update every minute

        return () => clearInterval(interval);
    }, [complaintId]);

    return { sla, loading };
};

// ============================================
// UTILITY FUNCTIONS
// ============================================

export const getStatusColor = (status) => {
    const colors = {
        PENDING: '#FFA500',
        ASSIGNED: '#4169E1',
        IN_PROGRESS: '#1E90FF',
        RESOLVED: '#32CD32',
        CLOSED: '#808080',
        REJECTED: '#DC143C',
        REOPENED: '#FF6347',
    };
    return colors[status] || '#000000';
};

export const getPriorityColor = (priority) => {
    const colors = {
        LOW: '#90EE90',
        MEDIUM: '#FFD700',
        HIGH: '#FF8C00',
        URGENT: '#FF0000',
    };
    return colors[priority] || '#000000';
};

export const formatTimeRemaining = (minutes) => {
    if (minutes < 0) return 'Breached';

    const days = Math.floor(minutes / 1440);
    const hours = Math.floor((minutes % 1440) / 60);
    const mins = minutes % 60;

    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h ${mins}m`;
    return `${mins}m`;
};

export default complaintAPI;
