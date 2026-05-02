import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { analyticsApi } from '@/services/api';
import { TrendingUp, TrendingDown, DollarSign, PieChart, Info } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n || 0);

export default function ProfitLoss() {
  const { user } = useAuthStore();
  const { data, isLoading } = useQuery({
    queryKey: ['profit-loss', user?.branchId],
    queryFn: () => analyticsApi.getProfitLoss(user?.branchId).then(r => r.data.data)
  });

  if (isLoading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading Report...</div>;

  const pl = data || {};

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div className="page-header">
        <div>
          <h1 className="page-title">Profit & Loss Statement</h1>
          <p className="page-subtitle">Monthly financial overview for {user?.branchName || 'all branches'}</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Gross Revenue */}
        <div className="card" style={{ borderLeft: '4px solid var(--color-primary)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
            <div>
              <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', fontWeight: 600 }}>GROSS REVENUE</p>
              <h2 style={{ fontSize: '2rem', fontWeight: 800, margin: '0.5rem 0' }}>{fmt(pl.grossRevenue)}</h2>
            </div>
            <div style={{ padding: '0.75rem', background: 'rgba(99,102,241,0.1)', borderRadius: '0.75rem' }}>
              <TrendingUp size={24} color="var(--color-primary)" />
            </div>
          </div>
        </div>

        {/* COGS */}
        <div className="card" style={{ borderLeft: '4px solid var(--color-danger)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
            <div>
              <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', fontWeight: 600 }}>COST OF GOODS SOLD</p>
              <h2 style={{ fontSize: '2rem', fontWeight: 800, margin: '0.5rem 0' }}>{fmt(pl.costOfGoodsSold)}</h2>
            </div>
            <div style={{ padding: '0.75rem', background: 'rgba(239,68,68,0.1)', borderRadius: '0.75rem' }}>
              <TrendingDown size={24} color="var(--color-danger)" />
            </div>
          </div>
        </div>
      </div>

      <div className="card" style={{ padding: '2rem' }}>
        <h3 style={{ fontWeight: 800, marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <PieChart size={20} color="var(--color-primary)" />
          Statement Breakdown
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.1rem' }}>
            <span>Gross Revenue</span>
            <span style={{ fontWeight: 700 }}>{fmt(pl.grossRevenue)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.1rem', color: 'var(--color-danger)' }}>
            <span>Cost of Goods Sold (COGS)</span>
            <span style={{ fontWeight: 700 }}>- {fmt(pl.costOfGoodsSold)}</span>
          </div>
          <div style={{ height: '1px', background: 'var(--color-border)' }}></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.25rem', fontWeight: 800 }}>
            <span>Gross Profit</span>
            <span style={{ color: 'var(--color-success)' }}>{fmt(pl.grossProfit)}</span>
          </div>

          <div style={{ marginTop: '1rem' }}></div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.1rem', color: 'var(--color-text-muted)' }}>
            <span>Operating Expenses</span>
            <span style={{ fontWeight: 700 }}>- {fmt(pl.operatingExpenses)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.1rem', color: 'var(--color-text-muted)' }}>
            <span>Estimated Taxes</span>
            <span style={{ fontWeight: 700 }}>- {fmt(pl.taxAmount)}</span>
          </div>

          <div style={{ height: '2px', background: 'var(--color-primary)', marginTop: '1rem' }}></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.5rem', fontWeight: 900, paddingTop: '1rem' }}>
            <span>NET PROFIT</span>
            <span style={{ color: 'var(--color-primary)' }}>{fmt(pl.netProfit)}</span>
          </div>
        </div>

        <div style={{ marginTop: '3rem', padding: '1rem', background: 'var(--color-surface-2)', borderRadius: '0.75rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <Info size={20} color="var(--color-text-muted)" />
          <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            This statement is generated based on recorded sales, product purchase prices, and tracked expenses for the current month. 
            Taxes are calculated based on the branch-specific tax rate.
          </p>
        </div>
      </div>
    </div>
  );
}
