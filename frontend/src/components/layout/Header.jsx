import { useState, useEffect } from 'react';
import { Bell, Search, User } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import api from '@/services/api';

export const Header = () => {
  const { user } = useAuthStore();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showDropdown, setShowDropdown] = useState(false);

  async function fetchNotifications() {
    try {
      const res = await api.get('/v1/notifications');
      setNotifications(res.data.data);
      const countRes = await api.get('/v1/notifications/unread-count');
      setUnreadCount(countRes.data.data);
    } catch (err) {
      console.error('Failed to fetch notifications', err);
    }
  }

  useEffect(() => {
    fetchNotifications();
  }, []);

  const markAsRead = async (id) => {
    try {
      await api.patch(`/v1/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <header style={{
      height: '64px',
      background: 'var(--color-surface)',
      borderBottom: '1px solid var(--color-border)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 1.5rem',
      position: 'sticky',
      top: 0,
      zIndex: 50
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: 1 }}>
        <div style={{ position: 'relative', maxWidth: '400px', width: '100%' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-muted)' }} />
          <input className="input" placeholder="Search analytics..." style={{ paddingLeft: '40px' }} />
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
        {/* Notifications */}
        <div style={{ position: 'relative' }}>
          <button 
            onClick={() => setShowDropdown(!showDropdown)}
            style={{ background: 'none', border: 'none', cursor: 'pointer', position: 'relative', color: 'var(--color-text)' }}
          >
            <Bell size={20} />
            {unreadCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '-4px',
                right: '-4px',
                background: 'var(--color-danger)',
                color: 'white',
                fontSize: '10px',
                fontWeight: 800,
                width: '16px',
                height: '16px',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '2px solid var(--color-surface)'
              }}>
                {unreadCount}
              </span>
            )}
          </button>

          {showDropdown && (
            <div style={{
              position: 'absolute',
              top: '40px',
              right: 0,
              width: '320px',
              background: 'var(--color-surface)',
              border: '1px solid var(--color-border)',
              borderRadius: '0.75rem',
              boxShadow: '0 10px 25px rgba(0,0,0,0.3)',
              overflow: 'hidden'
            }}>
              <div style={{ padding: '0.75rem 1rem', borderBottom: '1px solid var(--color-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>Notifications</span>
                <button style={{ fontSize: '0.75rem', color: 'var(--color-primary)', background: 'none', border: 'none', cursor: 'pointer' }}>Mark all read</button>
              </div>
              <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                {notifications.length === 0 ? (
                  <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)', fontSize: '0.8rem' }}>No new notifications</div>
                ) : (
                  notifications.map(n => (
                    <div 
                      key={n.id} 
                      onClick={() => markAsRead(n.id)}
                      style={{ 
                        padding: '0.75rem 1rem', 
                        borderBottom: '1px solid var(--color-border)', 
                        background: n.read ? 'transparent' : 'rgba(99,102,241,0.05)',
                        cursor: 'pointer'
                      }}
                    >
                      <p style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.25rem' }}>{n.title}</p>
                      <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>{n.message}</p>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.5rem', borderRadius: '0.5rem', background: 'var(--color-surface-2)' }}>
          <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <User size={18} color="white" />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{user?.fullName}</span>
            <span style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)' }}>{user?.role}</span>
          </div>
        </div>
      </div>
    </header>
  );
};
