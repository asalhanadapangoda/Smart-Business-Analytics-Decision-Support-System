import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi } from '@/services/api';
import { UserCog, ToggleLeft, ToggleRight } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AdminUsers() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['admin-users'], queryFn: () => usersApi.getAll().then(r => r.data.data) });
  const toggleMutation = useMutation({ mutationFn: (id) => usersApi.toggleStatus(id), onSuccess: () => { toast.success('Status updated'); qc.invalidateQueries(['admin-users']); } });
  const rolesMutation = useMutation({ mutationFn: ({ id, roleName }) => usersApi.updateRole(id, roleName), onSuccess: () => { toast.success('Role updated'); qc.invalidateQueries(['admin-users']); } });
  const users = data || [];
  return (
    <div>
      <div className="page-header"><div><h1 className="page-title">User Management</h1><p className="page-subtitle">{users.length} system users</p></div></div>
      <div className="card" style={{ padding:0 }}>
        <div className="table-wrapper">
          {isLoading ? <div style={{ padding:'2rem',textAlign:'center',color:'var(--color-text-muted)' }}>Loading...</div>
            : users.length===0 ? <div style={{ padding:'3rem',textAlign:'center',color:'var(--color-text-muted)' }}><p>No users found.</p></div>
            : <table>
                <thead><tr><th>Name</th><th>Username</th><th>Email</th><th>Role</th><th>Branch</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>{users.map(u=>(
                  <tr key={u.id}>
                    <td style={{ fontWeight:600 }}>{u.fullName}</td>
                    <td style={{ fontFamily:'monospace',fontSize:'0.875rem' }}>{u.username}</td>
                    <td style={{ color:'var(--color-text-muted)',fontSize:'0.875rem' }}>{u.email}</td>
                    <td><select className="input" style={{ padding:'0.25rem 0.5rem',fontSize:'0.75rem',width:'auto' }} value={u.roleName} onChange={e=>rolesMutation.mutate({id:u.id,roleName:e.target.value})}>{['ADMIN','MANAGER','CASHIER'].map(r=><option key={r}>{r}</option>)}</select></td>
                    <td>{u.branchName||'—'}</td>
                    <td><span className={`badge ${u.active?'badge-success':'badge-danger'}`}>{u.active?'Active':'Inactive'}</span></td>
                    <td><button className="btn btn-secondary" style={{ padding:'0.3rem 0.6rem',fontSize:'0.75rem' }} onClick={()=>toggleMutation.mutate(u.id)}>{u.active?<ToggleRight size={14}/>:<ToggleLeft size={14}/>} {u.active?'Deactivate':'Activate'}</button></td>
                  </tr>
                ))}</tbody>
              </table>}
        </div>
      </div>
    </div>
  );
}
