import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return (
      <div className="flex flex-col items-center justify-center h-screen" style={{ background: 'var(--color-bg)' }}>
        <div className="card text-center max-w-md">
          <div className="text-5xl mb-4">🚫</div>
          <h2 className="text-xl font-bold mb-2">Access Denied</h2>
          <p style={{ color: 'var(--color-text-muted)' }}>
            You don't have permission to view this page.
          </p>
        </div>
      </div>
    );
  }

  return children;
};
