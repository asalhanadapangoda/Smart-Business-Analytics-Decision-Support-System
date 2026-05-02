import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { DashboardLayout } from './components/layout/DashboardLayout';
import Login from './pages/Login';
import Dashboard from './pages/dashboard/Dashboard';
import SalesList from './pages/sales/SalesList';
import NewSale from './pages/sales/NewSale';
import Reports from './pages/reports/Reports';
import ChatbotWidget from './components/chatbot/ChatbotWidget';
import { useAuthStore } from './store/authStore';

// Lazy placeholders for remaining pages
import { lazy, Suspense } from 'react';
const Expenses = lazy(() => import('./pages/expenses/Expenses'));
const Products = lazy(() => import('./pages/products/Products'));
const Customers = lazy(() => import('./pages/customers/Customers'));
const Forecast = lazy(() => import('./pages/forecast/Forecast'));
const AdminUsers = lazy(() => import('./pages/admin/AdminUsers'));
const AdminBranches = lazy(() => import('./pages/admin/AdminBranches'));

const qc = new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 30000 } } });

const PageLoader = () => (
  <div style={{ display:'flex',alignItems:'center',justifyContent:'center',height:'60vh' }}>
    <div style={{ textAlign:'center' }}>
      <div style={{ width:40,height:40,border:'3px solid var(--color-border)',borderTop:'3px solid var(--color-primary)',borderRadius:'50%',animation:'spin 1s linear infinite',margin:'0 auto 1rem' }}/>
      <p style={{ color:'var(--color-text-muted)' }}>Loading...</p>
    </div>
    <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
  </div>
);

export default function App() {
  const { isAuthenticated } = useAuthStore();

  return (
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <Login />} />
          <Route path="/" element={<ProtectedRoute><DashboardLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="sales" element={<SalesList />} />
            <Route path="sales/new" element={<NewSale />} />
            <Route path="expenses" element={<Suspense fallback={<PageLoader />}><ProtectedRoute allowedRoles={['ADMIN','MANAGER']}><Expenses /></ProtectedRoute></Suspense>} />
            <Route path="products" element={<Suspense fallback={<PageLoader />}><ProtectedRoute allowedRoles={['ADMIN','MANAGER']}><Products /></ProtectedRoute></Suspense>} />
            <Route path="customers" element={<Suspense fallback={<PageLoader />}><Customers /></Suspense>} />
            <Route path="reports" element={<ProtectedRoute allowedRoles={['ADMIN','MANAGER']}><Reports /></ProtectedRoute>} />
            <Route path="forecast" element={<Suspense fallback={<PageLoader />}><ProtectedRoute allowedRoles={['ADMIN','MANAGER']}><Forecast /></ProtectedRoute></Suspense>} />
            <Route path="admin/users" element={<Suspense fallback={<PageLoader />}><ProtectedRoute allowedRoles={['ADMIN']}><AdminUsers /></ProtectedRoute></Suspense>} />
            <Route path="admin/branches" element={<Suspense fallback={<PageLoader />}><ProtectedRoute allowedRoles={['ADMIN']}><AdminBranches /></ProtectedRoute></Suspense>} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        {isAuthenticated && <ChatbotWidget />}
        <Toaster position="top-right" toastOptions={{ style: { background:'var(--color-surface)',color:'var(--color-text)',border:'1px solid var(--color-border)' } }} />
      </BrowserRouter>
    </QueryClientProvider>
  );
}
