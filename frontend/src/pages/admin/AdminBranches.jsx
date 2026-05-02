import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { branchesApi } from '@/services/api';
import { Plus, Building2 } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AdminBranches() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ name: '', location: '', contactNumber: '' });
  const [showForm, setShowForm] = useState(false);
  const { data, isLoading } = useQuery({ queryKey: ['admin-branches'], queryFn: () => branchesApi.getAll().then(r => r.data.data) });
  const createMutation = useMutation({ mutationFn: (d) => branchesApi.create(d), onSuccess: () => { toast.success('Branch created'); qc.invalidateQueries(['admin-branches']); setShowForm(false); } });
  const deactivateMutation = useMutation({ mutationFn: (id) => branchesApi.deactivate(id), onSuccess: () => { toast.success('Branch deactivated'); qc.invalidateQueries(['admin-branches']); } });
  const branches = data || [];
  return (
    <div>
      <div className="page-header"><div><h1 className="page-title">Branch Management</h1><p className="page-subtitle">{branches.length} branches</p></div><button className="btn btn-primary" onClick={()=>setShowForm(!showForm)}><Plus size={16}/> Add Branch</button></div>
      {showForm && (
        <div className="card" style={{ marginBottom:'1.5rem' }}>
          <h3 style={{ fontWeight:700,marginBottom:'1rem' }}>New Branch</h3>
          <div style={{ display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(180px,1fr))',gap:'1rem',marginBottom:'1rem' }}>
            <div><label>Name</label><input className="input" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} /></div>
            <div><label>Location</label><input className="input" value={form.location} onChange={e=>setForm({...form,location:e.target.value})} /></div>
            <div><label>Contact Number</label><input className="input" value={form.contactNumber} onChange={e=>setForm({...form,contactNumber:e.target.value})} /></div>
          </div>
          <button className="btn btn-primary" onClick={()=>createMutation.mutate(form)} disabled={createMutation.isPending}>{createMutation.isPending?'Saving...':'Save Branch'}</button>
        </div>
      )}
      <div style={{ display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(260px,1fr))',gap:'1rem' }}>
        {isLoading ? [1,2,3].map(i=><div key={i} className="skeleton" style={{ height:140 }}/>)
          : branches.length===0 ? <div className="card" style={{ gridColumn:'1/-1',textAlign:'center',padding:'3rem',color:'var(--color-text-muted)' }}><Building2 size={48} style={{ margin:'0 auto 1rem',opacity:0.3 }}/><p>No branches yet.</p></div>
          : branches.map(b=>(
              <div key={b.id} className="card">
                <div style={{ display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:'0.75rem' }}>
                  <div style={{ width:40,height:40,borderRadius:'0.5rem',background:'rgba(99,102,241,0.1)',display:'flex',alignItems:'center',justifyContent:'center' }}><Building2 size={20} color="var(--color-primary)"/></div>
                  <span className={`badge ${b.active?'badge-success':'badge-danger'}`}>{b.active?'Active':'Inactive'}</span>
                </div>
                <h4 style={{ fontWeight:700,marginBottom:'0.25rem' }}>{b.name}</h4>
                <p style={{ color:'var(--color-primary)',fontSize:'0.7rem',fontWeight:600,marginBottom:'0.5rem' }}>Code: {b.branchCode || 'N/A'}</p>
                <p style={{ color:'var(--color-text-muted)',fontSize:'0.8rem',marginBottom:'0.5rem' }}>📍 {b.location}</p>
                {b.contactNumber && <p style={{ color:'var(--color-text-muted)',fontSize:'0.8rem' }}>📞 {b.contactNumber}</p>}
                {b.active && <button className="btn btn-danger" style={{ marginTop:'0.75rem',padding:'0.3rem 0.6rem',fontSize:'0.75rem' }} onClick={()=>{if(confirm('Deactivate branch?'))deactivateMutation.mutate(b.id)}}>Deactivate</button>}
              </div>
            ))}
      </div>
    </div>
  );
}
