import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi, branchesApi, authApi } from '@/services/api';
import { UserCog, ToggleLeft, ToggleRight, Plus } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AdminUsers() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ username: '', password: '', email: '', fullName: '', roleName: 'CASHIER', branchId: '' });

  const [editId, setEditId] = useState(null);

  const { data: usersData, isLoading: loadingUsers } = useQuery({ queryKey: ['admin-users'], queryFn: () => usersApi.getAll().then(r => r.data.data) });
  const { data: branchesData } = useQuery({ queryKey: ['admin-branches-list'], queryFn: () => branchesApi.getAll().then(r => r.data.data) });

  const createMutation = useMutation({ 
    mutationFn: (d) => editId ? usersApi.update(editId, d) : authApi.register(d), 
    onSuccess: () => { 
      toast.success(editId ? 'User updated successfully' : 'User created successfully'); 
      qc.invalidateQueries(['admin-users']); 
      setShowForm(false);
      setEditId(null);
      setForm({ username: '', password: '', email: '', fullName: '', roleName: 'CASHIER', branchId: '' });
    },
    onError: (err) => toast.error('Failed: ' + (err.response?.data?.message || 'Check inputs'))
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => usersApi.delete(id),
    onSuccess: () => {
      toast.success('User deleted');
      qc.invalidateQueries(['admin-users']);
    }
  });

  const toggleMutation = useMutation({ mutationFn: (id) => usersApi.toggleStatus(id), onSuccess: () => { toast.success('Status updated'); qc.invalidateQueries(['admin-users']); } });
  
  const users = usersData || [];
  const branches = branchesData || [];

  const handleEdit = (u) => {
    setEditId(u.id);
    setForm({ 
      username: u.username, 
      password: '', // Password not editable here for security
      email: u.email, 
      fullName: u.fullName, 
      roleName: u.roleName, 
      branchId: branches.find(b => b.name === u.branchName)?.id || ''
    });
    setShowForm(true);
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">User Management</h1>
          <p className="page-subtitle">{users.length} system users</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditId(null); setForm({ username: '', password: '', email: '', fullName: '', roleName: 'CASHIER', branchId: '' }); }}>
          <Plus size={16} /> Create User
        </button>
      </div>

      {showForm && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ fontWeight: 700, marginBottom: '1rem' }}>{editId ? 'Edit User' : 'Create New User'}</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
            <div><label>Full Name</label><input className="input" value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} /></div>
            <div><label>Email</label><input className="input" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></div>
            <div><label>Username</label><input className="input" value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} disabled={!!editId} /></div>
            {!editId && <div><label>Password</label><input className="input" type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} /></div>}
            <div>
              <label>Role</label>
              <select className="input" value={form.roleName} onChange={e => setForm({ ...form, roleName: e.target.value })}>
                <option value="ADMIN">ADMIN</option>
                <option value="MANAGER">MANAGER</option>
                <option value="CASHIER">CASHIER</option>
              </select>
            </div>
            <div>
              <label>Assign Branch</label>
              <select className="input" value={form.branchId} onChange={e => setForm({ ...form, branchId: e.target.value })}>
                <option value="">No Branch (Admin Only)</option>
                {branches.map(b => (
                  <option key={b.id} value={b.id}>{b.name} ({b.branchCode})</option>
                ))}
              </select>
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button className="btn btn-primary" onClick={() => createMutation.mutate(form)} disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Saving...' : editId ? 'Update User' : 'Create User'}
            </button>
            <button className="btn btn-secondary" onClick={() => { setShowForm(false); setEditId(null); }}>Cancel</button>
          </div>
        </div>
      )}

      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrapper">
          {loadingUsers ? <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>Loading...</div>
            : users.length === 0 ? <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--color-text-muted)' }}><p>No users found.</p></div>
            : <table>
                <thead><tr><th>Name</th><th>Username</th><th>Role</th><th>Branch</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>{users.map(u => (
                  <tr key={u.id}>
                    <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                    <td style={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>{u.username}</td>
                    <td><span className="badge badge-secondary">{u.roleName}</span></td>
                    <td>{u.branchName || '—'}</td>
                    <td><span className={`badge ${u.active ? 'badge-success' : 'badge-danger'}`}>{u.active ? 'Active' : 'Inactive'}</span></td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }} onClick={() => handleEdit(u)}>Edit</button>
                        <button className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }} onClick={() => toggleMutation.mutate(u.id)}>{u.active ? 'Deactivate' : 'Activate'}</button>
                        <button className="btn btn-danger" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }} onClick={() => { if(confirm('Delete user permanently?')) deleteMutation.mutate(u.id); }}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}</tbody>
              </table>}
        </div>
      </div>
    </div>
  );
}
