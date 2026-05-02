import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customersApi } from '@/services/api';
import { Plus, Users } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Customers() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ name: '', email: '', phoneNumber: '', address: '', branchId: '' });
  const [showForm, setShowForm] = useState(false);

  const { data, isLoading } = useQuery({ queryKey: ['customers'], queryFn: () => customersApi.getAll().then(r => r.data.data) });

  const createMutation = useMutation({
    mutationFn: (d) => customersApi.create(d),
    onSuccess: () => { toast.success('Customer added'); qc.invalidateQueries(['customers']); setShowForm(false); },
  });

  const customers = data || [];

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">Customers</h1><p className="page-subtitle">{customers.length} registered customers</p></div>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}><Plus size={16} /> Add Customer</button>
      </div>
      {showForm && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ fontWeight: 700, marginBottom: '1rem' }}>New Customer</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(180px,1fr))', gap: '1rem', marginBottom: '1rem' }}>
            <div><label>Full Name</label><input className="input" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></div>
            <div><label>Email</label><input type="email" className="input" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></div>
            <div><label>Phone</label><input className="input" value={form.phoneNumber} onChange={e => setForm({ ...form, phoneNumber: e.target.value })} /></div>
            <div><label>Address</label><input className="input" value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} /></div>
          </div>
          <button className="btn btn-primary" onClick={() => createMutation.mutate(form)} disabled={createMutation.isPending}>{createMutation.isPending ? 'Saving...' : 'Save Customer'}</button>
        </div>
      )}
      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrapper">
          {isLoading ? <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>Loading...</div>
            : customers.length === 0 ? <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--color-text-muted)' }}><Users size={48} style={{ margin: '0 auto 1rem', opacity: 0.3 }} /><p>No customers yet.</p></div>
            : <table>
                <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Address</th><th>Branch</th></tr></thead>
                <tbody>{customers.map(c => (
                  <tr key={c.id}>
                    <td style={{ fontWeight: 600 }}>{c.name}</td>
                    <td style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>{c.email || '—'}</td>
                    <td>{c.phoneNumber}</td>
                    <td style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>{c.address || '—'}</td>
                    <td>{c.branchId}</td>
                  </tr>
                ))}</tbody>
              </table>}
        </div>
      </div>
    </div>
  );
}
