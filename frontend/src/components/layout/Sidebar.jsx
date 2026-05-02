import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import {
  LayoutDashboard, ShoppingCart, DollarSign, Package,
  Users, FileText, Settings, LogOut, ChevronLeft,
  ChevronRight, Bot, TrendingUp, Building2, UserCog
} from 'lucide-react';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard', roles: ['ADMIN', 'MANAGER', 'CASHIER'] },
  { to: '/sales',     icon: ShoppingCart,    label: 'Sales',      roles: ['ADMIN', 'MANAGER', 'CASHIER'] },
  { to: '/expenses',  icon: DollarSign,      label: 'Expenses',   roles: ['ADMIN', 'MANAGER'] },
  { to: '/products',  icon: Package,         label: 'Products',   roles: ['ADMIN', 'MANAGER'] },
  { to: '/customers', icon: Users,           label: 'Customers',  roles: ['ADMIN', 'MANAGER', 'CASHIER'] },
  { to: '/reports',   icon: FileText,        label: 'Reports',    roles: ['ADMIN', 'MANAGER'] },
  { to: '/forecast',  icon: TrendingUp,      label: 'AI Forecast', roles: ['ADMIN', 'MANAGER'] },
  { to: '/admin/users',    icon: UserCog,    label: 'Users',      roles: ['ADMIN'] },
  { to: '/admin/branches', icon: Building2,  label: 'Branches',   roles: ['ADMIN'] },
];

export const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const visibleItems = navItems.filter(item => item.roles.includes(user?.role));

  return (
    <aside style={{
      width: collapsed ? '64px' : '240px',
      minHeight: '100vh',
      background: 'var(--color-surface)',
      borderRight: '1px solid var(--color-border)',
      display: 'flex',
      flexDirection: 'column',
      transition: 'width 0.3s ease',
      flexShrink: 0,
    }}>
      {/* Logo */}
      <div style={{ padding: '1.25rem', borderBottom: '1px solid var(--color-border)', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <div style={{ width: 36, height: 36, borderRadius: '0.5rem', background: 'linear-gradient(135deg, #6366f1, #22d3ee)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, fontWeight: 800, fontSize: '1rem', color: 'white' }}>S</div>
        {!collapsed && <span style={{ fontWeight: 700, fontSize: '0.9rem', whiteSpace: 'nowrap' }}>SBADSS</span>}
      </div>

      {/* Nav */}
      <nav style={{ flex: 1, padding: '0.75rem 0.5rem', display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
        {visibleItems.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} style={({ isActive }) => ({
            display: 'flex',
            alignItems: 'center',
            gap: '0.75rem',
            padding: '0.625rem 0.75rem',
            borderRadius: '0.5rem',
            textDecoration: 'none',
            color: isActive ? 'white' : 'var(--color-text-muted)',
            background: isActive ? 'var(--color-primary)' : 'transparent',
            fontWeight: isActive ? 600 : 400,
            fontSize: '0.875rem',
            transition: 'all 0.15s',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
          })}>
            <Icon size={18} style={{ flexShrink: 0 }} />
            {!collapsed && label}
          </NavLink>
        ))}
      </nav>

      {/* User + Collapse */}
      <div style={{ padding: '0.75rem 0.5rem', borderTop: '1px solid var(--color-border)' }}>
        {!collapsed && (
          <div style={{ padding: '0.5rem 0.75rem', marginBottom: '0.5rem' }}>
            <p style={{ fontSize: '0.8rem', fontWeight: 600 }}>{user?.fullName || user?.username}</p>
            <p style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)' }}>{user?.role}</p>
          </div>
        )}
        <button onClick={handleLogout} className="btn btn-secondary" style={{ width: '100%', justifyContent: collapsed ? 'center' : 'flex-start' }}>
          <LogOut size={16} />
          {!collapsed && 'Logout'}
        </button>
        <button onClick={() => setCollapsed(!collapsed)} style={{ marginTop: '0.5rem', width: '100%', background: 'transparent', border: 'none', color: 'var(--color-text-muted)', cursor: 'pointer', display: 'flex', justifyContent: 'center', padding: '0.375rem' }}>
          {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
        </button>
      </div>
    </aside>
  );
};
