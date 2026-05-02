import { useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import api, { analyticsApi } from '@/services/api';
import { useAuthStore } from '@/store/authStore';
import { TrendingUp, TrendingDown, DollarSign, Users, AlertCircle, Sparkles } from 'lucide-react';
import {
  LineChart, Line, BarChart, Bar, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(n || 0);
const fmtPct = (n) => `${n >= 0 ? '+' : ''}${(n || 0).toFixed(1)}%`;

const SkeletonCard = () => (
  <div className="kpi-card">
    <div className="skeleton" style={{ height: 14, width: '60%', marginBottom: '0.75rem' }} />
    <div className="skeleton" style={{ height: 28, width: '80%', marginBottom: '0.5rem' }} />
    <div className="skeleton" style={{ height: 12, width: '40%' }} />
  </div>
);

const KPICard = ({ title, value, growth, icon: Icon, variant }) => (
  <div className={`kpi-card kpi-${variant}`}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
      <div>
        <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{title}</p>
        <p style={{ fontSize: '1.75rem', fontWeight: 800, marginTop: '0.375rem' }}>{value}</p>
        {growth !== undefined && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', marginTop: '0.375rem', fontSize: '0.8rem' }}>
            {growth >= 0 ? <TrendingUp size={14} color="var(--color-success)" /> : <TrendingDown size={14} color="var(--color-danger)" />}
            <span style={{ color: growth >= 0 ? 'var(--color-success)' : 'var(--color-danger)', fontWeight: 600 }}>{fmtPct(growth)}</span>
            <span style={{ color: 'var(--color-text-muted)' }}>vs last month</span>
          </div>
        )}
      </div>
      <div style={{ width: 44, height: 44, borderRadius: '0.625rem', background: 'rgba(99,102,241,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Icon size={22} color="var(--color-primary)" />
      </div>
    </div>
  </div>
);

const COLORS = ['#6366f1', '#22d3ee', '#10b981', '#f59e0b', '#ef4444'];

export default function Dashboard() {
  const { user } = useAuthStore();
  const stompRef = useRef(null);

  const { data: dashData, refetch, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => analyticsApi.getDashboard().then(r => r.data.data),
    refetchInterval: 30000,
  });

  // WebSocket real-time connection
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws-analytics'),
      onConnect: () => {
        client.subscribe('/topic/dashboard/global', () => refetch());
      },
      reconnectDelay: 5000,
    });
    client.activate();
    stompRef.current = client;
    return () => client.deactivate();
  }, [refetch]);

  const { data: recs } = useQuery({
    queryKey: ['recommendations'],
    queryFn: () => api.get('/v1/recommendations/business', { params: { branchId: user?.branchId || 1 } }).then(r => r.data.data),
    enabled: !!user,
  });

  const kpi = dashData?.kpiMetrics;
  const salesTrends = dashData?.salesTrends?.dataPoints?.map(d => ({ date: d.label, revenue: parseFloat(d.value) })) || [];
  const topProducts = dashData?.topProducts?.dataPoints?.map(d => ({ name: d.label, qty: parseFloat(d.value) })) || [];

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Welcome back, {user?.fullName || user?.username} — Here's your business overview</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--color-success)', animation: 'pulse 2s infinite' }} />
          Live • refreshes every 30s
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        {isLoading ? (
          [1,2,3,4].map(i => <SkeletonCard key={i} />)
        ) : (
          <>
            <KPICard title="Total Revenue" value={fmt(kpi?.totalRevenue)} growth={kpi?.revenueGrowth} icon={DollarSign} variant="revenue" />
            <KPICard title="Net Profit" value={fmt(kpi?.totalProfit)} growth={kpi?.profitGrowth} icon={TrendingUp} variant="profit" />
            <KPICard title="Total Expenses" value={fmt(kpi?.totalExpenses)} growth={kpi?.expenseGrowth} icon={AlertCircle} variant="expense" />
            <KPICard title="Active Customers" value="—" icon={Users} variant="customers" />
          </>
        )}
      </div>

      {/* Recommendations & Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 0.8fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Sales Trend */}
        <div className="card">
          <h3 style={{ fontWeight: 700, marginBottom: '1rem', fontSize: '0.95rem' }}>📈 Sales Trend (This Month)</h3>
          {isLoading ? <div className="skeleton" style={{ height: 220 }} /> : (
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={salesTrends}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} tickFormatter={v => `$${(v/1000).toFixed(0)}k`} />
                <Tooltip contentStyle={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: 8 }} formatter={v => [fmt(v), 'Revenue']} />
                <Line type="monotone" dataKey="revenue" stroke="#6366f1" strokeWidth={2.5} dot={{ r: 3, fill: '#6366f1' }} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* AI Recommendations */}
        <div className="card" style={{ background: 'linear-gradient(135deg, var(--color-surface), rgba(99,102,241,0.05))', border: '1px solid rgba(99,102,241,0.2)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <Sparkles size={18} color="var(--color-primary)" />
            <h3 style={{ fontWeight: 700, fontSize: '0.95rem' }}>AI Decision Support</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {recs?.map((rec, i) => (
              <div key={i} style={{ display: 'flex', gap: '0.75rem', padding: '0.75rem', background: 'var(--color-surface-2)', borderRadius: '0.5rem', border: '1px solid var(--color-border)' }}>
                <div style={{ color: 'var(--color-primary)', fontWeight: 800 }}>0{i+1}</div>
                <p style={{ fontSize: '0.85rem', lineHeight: 1.4 }}>{rec}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '1rem', marginBottom: '1rem' }}>
        {/* Top Products */}
        <div className="card">
          <h3 style={{ fontWeight: 700, marginBottom: '1rem', fontSize: '0.95rem' }}>🏆 Top Products</h3>
          {isLoading ? <div className="skeleton" style={{ height: 220 }} /> : (
            topProducts.length === 0 ? (
              <div style={{ height: 220, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>
                No sales data available yet
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={topProducts} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                  <XAxis type="number" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
                  <YAxis type="category" dataKey="name" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} width={80} />
                  <Tooltip contentStyle={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: 8 }} />
                  <Bar dataKey="qty" radius={[0,4,4,0]}>
                    {topProducts.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )
          )}
        </div>
      </div>
    </div>
  );
}
