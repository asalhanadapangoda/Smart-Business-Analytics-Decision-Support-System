import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { authApi } from '@/services/api';
import toast from 'react-hot-toast';

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuthStore();
  const navigate = useNavigate();

  const resetSession = () => {
    useAuthStore.getState().logout();
    try {
      localStorage.removeItem('sbadss-auth');
    } catch (_) {
      // Ignore storage errors in restricted browser modes.
    }
    setError('');
    setForm({ username: '', password: '' });
    toast.success('Session reset. Please sign in again.');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.username || !form.password) {
      setError('Please enter both username and password');
      return;
    }
    setLoading(true);
    try {
      const res = await authApi.login(form);
      const { token, role, username, fullName, id } = res.data.data;
      login({ username, fullName, role, id }, token);
      toast.success(`Welcome back, ${fullName || username}!`);
      navigate('/dashboard');
    } catch (err) {
      const backendMessage = err.response?.data?.message;
      const fallbackMessage = err.response?.status === 401
        ? 'Invalid username or password.'
        : 'Login failed. Please check your credentials.';
      setError(backendMessage || fallbackMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--color-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
      <div style={{ width: '100%', maxWidth: '420px' }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{ width: 64, height: 64, borderRadius: '1rem', background: 'linear-gradient(135deg, #6366f1, #22d3ee)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem', fontSize: '1.75rem', fontWeight: 800, color: 'white' }}>S</div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.25rem' }}>SBADSS</h1>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Smart Business Analytics & Decision Support System</p>
        </div>

        <div className="card">
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1.5rem' }}>Sign In</h2>

          {error && (
            <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: '0.5rem', padding: '0.75rem', marginBottom: '1rem', fontSize: '0.875rem', color: 'var(--color-danger)' }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <label>Username</label>
              <input className="input" type="text" placeholder="Enter your username" value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })} autoFocus />
            </div>
            <div>
              <label>Password</label>
              <input className="input" type="password" placeholder="Enter your password" value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
            <button type="submit" className="btn btn-primary" style={{ marginTop: '0.5rem', padding: '0.75rem', justifyContent: 'center', fontSize: '1rem' }} disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
            <button
              type="button"
              className="btn"
              style={{ justifyContent: 'center', padding: '0.65rem' }}
              onClick={resetSession}
            >
              Reset Session
            </button>
          </form>

          <div style={{ marginTop: '1.5rem', padding: '0.75rem', background: 'var(--color-surface-2)', borderRadius: '0.5rem', fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
            <strong>Demo Credentials:</strong><br />
            Admin: admin / admin123 &nbsp;|&nbsp; Manager: manager / manager123
          </div>
        </div>
      </div>
    </div>
  );
}
