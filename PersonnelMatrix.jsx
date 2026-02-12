import React, { useState, useEffect } from 'react';
import {
    Users, Search, Shield, Building2, MapPin,
    Phone, Mail, Activity, ArrowRight, Download,
    Briefcase, ChevronRight, UserCircle
} from 'lucide-react';

/**
 * CivicConnect Personnel Matrix
 * A high-performance, role-aware directory for Citizens and Officers.
 * Fits the 'Tactical Command Center' aesthetic.
 */
const PersonnelMatrix = ({ userRole }) => {
    const [personnel, setPersonnel] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterRole, setFilterRole] = useState('ALL');

    useEffect(() => {
        fetchPersonnel();
    }, [userRole]);

    const fetchPersonnel = async () => {
        try {
            setLoading(true);
            // Determine endpoint based on user role
            let endpoint = '/api/officers/citizen/ward';
            if (userRole === 'ADMIN') endpoint = '/api/officers/admin/all';
            if (userRole === 'WARD_OFFICER') endpoint = '/api/officers/ward-officer/dept-officers';
            if (userRole === 'DEPARTMENT_OFFICER') endpoint = '/api/officers/department-officer/peers';

            const response = await fetch(endpoint, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
            });
            const data = await response.json();
            setPersonnel(Array.isArray(data) ? data : data.content || []);
        } catch (error) {
            console.error('Failed to fetch personnel:', error);
        } finally {
            setLoading(false);
        }
    };

    const filteredData = personnel.filter(p => {
        const matchesSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (p.department && p.department.toLowerCase().includes(searchTerm.toLowerCase()));
        const matchesRole = filterRole === 'ALL' || p.role === filterRole;
        return matchesSearch && matchesRole;
    });

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-2xl">
            {/* Header Area */}
            <div className="p-6 border-b border-slate-800 bg-slate-900/50 backdrop-blur-md">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                    <div>
                        <h2 className="text-xl font-bold text-white flex items-center gap-3">
                            <Users className="w-6 h-6 text-blue-400" />
                            Personnel & Officer Matrix
                        </h2>
                        <p className="text-slate-400 text-sm">Real-time duty roster and workload overview</p>
                    </div>

                    <div className="flex bg-slate-800 p-1 rounded-lg border border-slate-700">
                        <button
                            onClick={() => setFilterRole('ALL')}
                            className={`px-4 py-1.5 text-xs font-medium rounded-md transition-all ${filterRole === 'ALL' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}
                        >
                            All Personnel
                        </button>
                        <button
                            onClick={() => setFilterRole('DEPARTMENT_OFFICER')}
                            className={`px-4 py-1.5 text-xs font-medium rounded-md transition-all ${filterRole === 'DEPARTMENT_OFFICER' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}
                        >
                            Field Units
                        </button>
                        <button
                            onClick={() => setFilterRole('WARD_OFFICER')}
                            className={`px-4 py-1.5 text-xs font-medium rounded-md transition-all ${filterRole === 'WARD_OFFICER' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}
                        >
                            Command
                        </button>
                    </div>
                </div>

                {/* Search Bar */}
                <div className="mt-6 relative">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                    <input
                        type="text"
                        placeholder="Search by name, department, or specialization..."
                        className="w-full bg-slate-950/50 border border-slate-700 text-white pl-11 pr-4 py-3 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/50 transition-all placeholder:text-slate-600"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            {/* Roster Grid */}
            <div className="p-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 max-h-[600px] overflow-y-auto custom-scrollbar bg-slate-950/20">
                {loading ? (
                    [...Array(6)].map((_, i) => (
                        <div key={i} className="h-40 bg-slate-800/20 rounded-xl animate-pulse border border-slate-800/50" />
                    ))
                ) : filteredData.length > 0 ? (
                    filteredData.map((person) => (
                        <div key={person.userId} className="group p-5 bg-slate-900 border border-slate-800 rounded-xl hover:border-blue-500/50 transition-all hover:bg-slate-800/40 relative overflow-hidden">
                            <div className="flex justify-between items-start mb-4">
                                <div className="flex items-center gap-3">
                                    <div className="w-12 h-12 rounded-xl bg-slate-800 flex items-center justify-center border border-slate-700 group-hover:bg-blue-600/10 group-hover:border-blue-500/50 transition-all">
                                        {person.role === 'WARD_OFFICER' ? (
                                            <Shield className="w-6 h-6 text-yellow-400" />
                                        ) : (
                                            <Briefcase className="w-6 h-6 text-blue-400" />
                                        )}
                                    </div>
                                    <div>
                                        <h3 className="font-semibold text-white group-hover:text-blue-400 transition-colors">
                                            {person.name}
                                        </h3>
                                        <span className="text-[10px] font-bold tracking-widest text-slate-500 uppercase">
                                            {person.role.replace('_', ' ')}
                                        </span>
                                    </div>
                                </div>

                                {person.activeComplaintsCount !== undefined && (
                                    <div className="text-right">
                                        <div className={`text-xs font-bold ${person.activeComplaintsCount > 15 ? 'text-red-400' : 'text-green-400'}`}>
                                            {person.activeComplaintsCount} ACTIVE
                                        </div>
                                        <div className="h-1 w-12 bg-slate-800 rounded-full mt-1 ml-auto overflow-hidden">
                                            <div
                                                className={`h-full ${person.activeComplaintsCount > 15 ? 'bg-red-500' : 'bg-green-500'}`}
                                                style={{ width: `${Math.min(person.activeComplaintsCount * 5, 100)}%` }}
                                            />
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="space-y-2 mb-4">
                                <div className="flex items-center gap-2 text-xs text-slate-400">
                                    <Building2 className="w-3.5 h-3.5" />
                                    {person.department || 'Ward Office'}
                                </div>
                                <div className="flex items-center gap-2 text-xs text-slate-400">
                                    <MapPin className="w-3.5 h-3.5" />
                                    {person.wardNumber || person.wardName}
                                </div>
                            </div>

                            <div className="pt-4 border-t border-slate-800/50 flex gap-2">
                                <a href={`tel:${person.mobile}`} className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-300 py-2 rounded-lg text-xs font-medium flex items-center justify-center gap-2 transition-colors">
                                    <Phone className="w-3 h-3 text-blue-400" /> Call
                                </a>
                                <a href={`mailto:${person.email}`} className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-300 py-2 rounded-lg text-xs font-medium flex items-center justify-center gap-2 transition-colors">
                                    <Mail className="w-3 h-3 text-blue-400" /> Email
                                </a>
                            </div>
                        </div>
                    ))
                ) : (
                    <div className="col-span-full py-20 text-center">
                        <UserCircle className="w-12 h-12 text-slate-700 mx-auto mb-4" />
                        <p className="text-slate-500">No personnel found matching your filters.</p>
                    </div>
                )}
            </div>

            {/* Footer Info */}
            <div className="p-4 bg-slate-900 border-t border-slate-800 flex justify-between items-center">
                <div className="text-[10px] text-slate-500 uppercase tracking-tighter">
                    Personnel Encryption: AES-256 Enabled
                </div>
                <button className="text-[10px] text-blue-400 font-bold hover:text-blue-300 flex items-center gap-1 uppercase tracking-tighter">
                    Export Matrix <Download className="w-3 h-3" />
                </button>
            </div>
        </div>
    );
};

export default PersonnelMatrix;
