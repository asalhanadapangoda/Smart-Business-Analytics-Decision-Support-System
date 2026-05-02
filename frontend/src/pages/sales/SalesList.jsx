import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { salesApi } from '@/services/api';
import { Plus, CheckCircle, Trash2, ShoppingCart } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/store/authStore';
import { format } from 'date-fns';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n || 0);

export default function SalesList() {
  const { user } = useAuthStore();
  const qc = useQueryClient();
  const isManager = ['ADMIN', 'MANAGER'].includes(user?.role);

  const { data, isLoading } = useQuery({
    queryKey: ['sales'],
    queryFn: () => salesApi.getAll().then(r => r.data.data),
  });

  const completeMutation = useMutation({
    mutationFn: (id) => salesApi.complete(id),
    onSuccess: () => { toast.success('Sale completed'); qc.invalidateQueries(['sales']); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => salesApi.delete(id),
    onSuccess: () => { toast.success('Sale deleted'); qc.invalidateQueries(['sales']); },
  });

  const sales = data || [];

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Sales</h1>
          <p className="page-subtitle">{sales.length} transaction{sales.length !== 1 ? 's' : ''} found</p>
        </div>
        <a href="/sales/new" className="btn btn-primary"><Plus size={16} /> New Sale</a>
      </div>

      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrapper">
          {isLoading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>Loading...</div>
          ) : sales.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>
              <ShoppingCart size={48} style={{ margin: '0 auto 1rem', opacity: 0.3 }} />
              <p>No sales found. Create your first sale!</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Invoice #</th>
                  <th>Customer</th>
                  <th>Cashier</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sales.map(sale => (
                  <tr key={sale.id}>
                    <td style={{ fontWeight: 600, fontFamily: 'monospace' }}>{sale.invoiceNumber}</td>
                    <td>{sale.customerName || '—'}</td>
                    <td>{sale.cashierName}</td>
                    <td style={{ fontWeight: 600 }}>{fmt(sale.totalAmount)}</td>
                    <td>
                      <span className={`badge badge-${sale.status === 'COMPLETED' ? 'success' : sale.status === 'DRAFT' ? 'warning' : 'danger'}`}>
                        {sale.status}
                      </span>
                    </td>
                    <td style={{ color: 'var(--color-text-muted)', fontSize: '0.8rem' }}>
                      {sale.createdAt ? format(new Date(sale.createdAt), 'MMM dd, yyyy HH:mm') : '—'}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        {sale.status === 'DRAFT' && (
                          <>
                            <button className="btn btn-success" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                              onClick={() => completeMutation.mutate(sale.id)}>
                              <CheckCircle size={14} /> Complete
                            </button>
                            {(user?.role === 'CASHIER' || isManager) && (
                              <button className="btn btn-danger" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                                onClick={() => { if(confirm('Delete this sale?')) deleteMutation.mutate(sale.id); }}>
                                <Trash2 size={14} />
                              </button>
                            )}
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
