import React, { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { forecastApi, branchesApi } from '@/services/api';
import { TrendingUp, Zap, Search, Building2 } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import toast from 'react-hot-toast';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(n || 0);

export default function Forecast() {
  const [form, setForm] = useState({ branchId: '', horizonDays: 30 });
  const [branchSearch, setBranchSearch] = useState('');
  const [showBranchList, setShowBranchList] = useState(false);
  const [result, setResult] = useState(null);

  const { data: branches = [] } = useQuery({ 
    queryKey: ['branches'], 
    queryFn: () => branchesApi.getAll().then(r => r.data.data) 
  });

  const forecastMutation = useMutation({
    mutationFn: (d) => forecastApi.getSalesForecast({ ...d, branchId: parseInt(d.branchId), horizonDays: parseInt(d.horizonDays) }),
    onSuccess: (res) => { setResult(res.data.data); },
    onError: () => toast.error('Forecast failed. Ensure the branch has enough sales history.'),
  });

  const filteredBranches = branches.filter(b => 
    b.name.toLowerCase().includes(branchSearch.toLowerCase()) || 
    (b.branchCode && b.branchCode.toLowerCase().includes(branchSearch.toLowerCase()))
  );

  const selectBranch = (b) => {
    setForm({ ...form, branchId: b.id });
    setBranchSearch(b.name);
    setShowBranchList(false);
  };

  const chartData = result?.predictions?.map(p => ({ date: p.label, predicted: parseFloat(p.value) })) || [];

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">AI Sales Forecast</h1><p className="page-subtitle">Predict future revenue using machine learning</p></div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h3 style={{ fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Zap size={18} color="var(--color-accent)" /> Configure Forecast</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(250px,1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
          
          {/* Branch Selection with Search */}
          <div style={{ position: 'relative' }}>
            <label>Select Branch</label>
            <div style={{ position: 'relative' }}>
              <input 
                className="input" 
                style={{ paddingLeft: '2.5rem' }}
                placeholder="Search branch name or code..." 
                value={branchSearch}
                onFocus={() => setShowBranchList(true)}
                onChange={(e) => {
                  setBranchSearch(e.target.value);
                  setShowBranchList(true);
                  if (form.branchId) setForm({ ...form, branchId: '' });
                }}
              />
              <div style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-muted)' }}>
                <Search size={16} />
              </div>
            </div>

            {showBranchList && (
              <div className="card" style={{ 
                position: 'absolute', 
                top: '100%', 
                left: 0, 
                right: 0, 
                zIndex: 50, 
                marginTop: '0.25rem', 
                maxHeight: '200px', 
                overflowY: 'auto',
                padding: '0.5rem',
                boxShadow: 'var(--shadow-lg)'
              }}>
                {filteredBranches.length === 0 ? (
                  <div style={{ padding: '0.5rem', textAlign: 'center', color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>No branches found</div>
                ) : filteredBranches.map(b => (
                  <div 
                    key={b.id} 
                    className="dropdown-item"
                    style={{ 
                      padding: '0.6rem 0.75rem', 
                      borderRadius: '0.375rem', 
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.75rem',
                      transition: 'background 0.2s'
                    }}
                    onClick={() => selectBranch(b)}
                    onMouseEnter={(e) => e.currentTarget.style.background = 'var(--color-surface-2)'}
                    onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                  >
                    <Building2 size={16} color="var(--color-primary)" />
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{b.name}</div>
                      <div style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)' }}>{b.branchCode} | {b.location}</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div><label>Forecast Horizon</label>
            <select className="input" value={form.horizonDays} onChange={e => setForm({ ...form, horizonDays: e.target.value })}>
              <option value={7}>7 days</option><option value={30}>30 days</option><option value={60}>60 days</option><option value={90}>90 days</option>
            </select>
          </div>
        </div>
        
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className="btn btn-primary" onClick={() => forecastMutation.mutate(form)} disabled={!form.branchId || forecastMutation.isPending}>
            <TrendingUp size={16} /> {forecastMutation.isPending ? 'Generating Forecast...' : 'Generate Forecast'}
          </button>
          {form.branchId && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-success)', fontSize: '0.875rem', fontWeight: 600 }}>
              ✓ Branch Selected
            </div>
          )}
        </div>
      </div>

      {result && (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
            <div className="card">
              <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Model Used</p>
              <p style={{ fontWeight: 700, marginTop: '0.375rem' }}>{result.modelUsed}</p>
            </div>
            <div className="card">
              <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Confidence</p>
              <p style={{ fontWeight: 700, marginTop: '0.375rem', color: 'var(--color-success)' }}>{(result.overallConfidence * 100).toFixed(0)}%</p>
            </div>
            <div className="card">
              <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Horizon</p>
              <p style={{ fontWeight: 700, marginTop: '0.375rem' }}>{result.horizonDays} days</p>
            </div>
          </div>

          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <h3 style={{ fontWeight: 700, marginBottom: '1rem' }}>📈 Predicted Revenue</h3>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} tickFormatter={v => `$${(v/1000).toFixed(0)}k`} />
                <Tooltip contentStyle={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: 8 }} formatter={v => [fmt(v), 'Predicted Revenue']} />
                <Line type="monotone" dataKey="predicted" stroke="#22d3ee" strokeWidth={2.5} dot={false} strokeDasharray="6 3" />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="card" style={{ background: 'rgba(99,102,241,0.05)', border: '1px solid rgba(99,102,241,0.2)' }}>
            <h4 style={{ fontWeight: 700, marginBottom: '0.5rem', color: 'var(--color-primary)' }}>🤖 AI Recommendation</h4>
            <p style={{ color: 'var(--color-text)', fontSize: '0.9rem', lineHeight: 1.6 }}>{result.recommendation}</p>
          </div>
        </>
      )}
    </div>
  );
}
