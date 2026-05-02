import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { expensesApi } from '@/services/api';
import { Plus, Pencil, Trash2, DollarSign } from 'lucide-react';
import toast from 'react-hot-toast';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n || 0);

export default function Expenses() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ description: '', amount: '', expenseDate: '', categoryId: '', branchId: '' });
  const [showForm, setShowForm] = useState(false);

  const { data, isLoading } = useQuery({ queryKey: ['expenses'], queryFn: () => expensesApi.getAll().then(r => r.data.data) });

  const createMutation = useMutation({
    mutationFn: (d) => expensesApi.create(d),
    onSuccess: () => { toast.success('Expense added'); qc.invalidateQueries(['expenses']); setShowForm(false); setForm({ description: '', amount: '', expenseDate: '', categoryId: '', branchId: '' }); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => expensesApi.delete(id),
    onSuccess: () => { toast.success('Expense deleted'); qc.invalidateQueries(['expenses']); },
  });

  const expenses = data || [];
  const total = expenses.reduce((s, e) => s + (parseFloat(e.amount) || 0), 0);

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">Expenses</h1><p className="page-subtitle">Total: {fmt(total)} this period</p></div>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}><Plus size={16} /> Add Expense</button>
      </div>
      {showForm && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ fontWeight: 700, marginBottom: '1rem' }}>New Expense</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(180px,1fr))', gap: '1rem', marginBottom: '1rem' }}>
            <div><label>Description</label><input className="input" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Expense description" /></div>
            <div><label>Amount</label><input className="input" type="number" value={form.amount} onChange={e => setForm({ ...form, amount: e.target.value })} placeholder="0.00" /></div>
            <div><label>Date</label><input type="date" className="input" value={form.expenseDate} onChange={e => setForm({ ...form, expenseDate: e.target.value })} /></div>
          </div>
          <button className="btn btn-primary" onClick={() => createMutation.mutate(form)} disabled={createMutation.isPending}>{createMutation.isPending ? 'Saving...' : 'Save Expense'}</button>
        </div>
      )}
      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrapper">
          {isLoading ? <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>Loading...</div>
            : expenses.length === 0 ? <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--color-text-muted)' }}><DollarSign size={48} style={{ margin: '0 auto 1rem', opacity: 0.3 }} /><p>No expenses recorded.</p></div>
            : <table>
                <thead><tr><th>Description</th><th>Amount</th><th>Date</th><th>Category</th><th>Branch</th><th>Actions</th></tr></thead>
                <tbody>{expenses.map(e => (
                  <tr key={e.id}>
                    <td style={{ fontWeight: 500 }}>{e.description}</td>
                    <td style={{ fontWeight: 600, color: 'var(--color-danger)' }}>{fmt(e.amount)}</td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>{e.expenseDate}</td>
                    <td>{e.categoryName || '—'}</td>
                    <td>{e.branchName || '—'}</td>
                    <td><button className="btn btn-danger" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }} onClick={() => { if (confirm('Delete expense?')) deleteMutation.mutate(e.id); }}><Trash2 size={14} /></button></td>
                  </tr>
                ))}</tbody>
              </table>}
        </div>
      </div>
    </div>
  );
}
