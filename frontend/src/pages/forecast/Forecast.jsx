import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { forecastApi } from '@/services/api';
import { TrendingUp, Zap } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';
import toast from 'react-hot-toast';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(n || 0);

export default function Forecast() {
  const [form, setForm] = useState({ branchId: '', horizonDays: 30 });
  const [result, setResult] = useState(null);

  const forecastMutation = useMutation({
    mutationFn: (d) => forecastApi.getSalesForecast({ ...d, branchId: parseInt(d.branchId), horizonDays: parseInt(d.horizonDays) }),
    onSuccess: (res) => { setResult(res.data.data); },
    onError: () => toast.error('Forecast failed. Check your branch ID.'),
  });

  const chartData = result?.predictions?.map(p => ({ date: p.label, predicted: parseFloat(p.value) })) || [];

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">AI Sales Forecast</h1><p className="page-subtitle">Predict future revenue using machine learning</p></div>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h3 style={{ fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Zap size={18} color="var(--color-accent)" /> Configure Forecast</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(180px,1fr))', gap: '1rem', marginBottom: '1rem' }}>
          <div><label>Branch ID</label><input type="number" className="input" value={form.branchId} onChange={e => setForm({ ...form, branchId: e.target.value })} placeholder="e.g. 1" /></div>
          <div><label>Forecast Horizon</label>
            <select className="input" value={form.horizonDays} onChange={e => setForm({ ...form, horizonDays: e.target.value })}>
              <option value={7}>7 days</option><option value={30}>30 days</option><option value={60}>60 days</option><option value={90}>90 days</option>
            </select>
          </div>
        </div>
        <button className="btn btn-primary" onClick={() => forecastMutation.mutate(form)} disabled={!form.branchId || forecastMutation.isPending}>
          <TrendingUp size={16} /> {forecastMutation.isPending ? 'Generating Forecast...' : 'Generate Forecast'}
        </button>
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
