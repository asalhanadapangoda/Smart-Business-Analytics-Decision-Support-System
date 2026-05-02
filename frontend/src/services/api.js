import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Request interceptor — attach JWT
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor — global error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || 'An unexpected error occurred';

    if (status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
      toast.error('Session expired. Please log in again.');
    } else if (status === 403) {
      toast.error('You do not have permission to perform this action.');
    } else if (status === 422) {
      // Validation errors — let the form handle them
    } else if (status >= 500) {
      toast.error('Server error. Please try again later.');
    } else if (status !== 404) {
      toast.error(message);
    }
    return Promise.reject(error);
  }
);

export default api;

// ── API Modules ──────────────────────────────────────────────────────────────

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
};

export const salesApi = {
  getAll: (params) => api.get('/sales', { params }),
  create: (data) => api.post('/sales', data),
  complete: (id) => api.patch(`/sales/${id}/complete`),
  delete: (id) => api.delete(`/sales/${id}`),
};


export const expensesApi = {
  getAll: (params) => api.get('/expenses', { params }),
  create: (data) => api.post('/expenses', data),
  update: (id, data) => api.put(`/expenses/${id}`, data),
  delete: (id) => api.delete(`/expenses/${id}`),
};

export const productsApi = {
  getAll: (params) => api.get('/products', { params }),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
};

export const categoriesApi = {
  getAll: () => api.get('/categories'),
  create: (data) => api.post('/categories', data),
};

export const customersApi = {
  getAll: (params) => api.get('/customers', { params }),
  create: (data) => api.post('/customers', data),
};

export const analyticsApi = {
  getDashboard: (branchId) => api.get('/analytics/dashboard', { params: { branchId } }),
};


export const reportsApi = {
  generate: (data) => api.post('/v1/reports/generate', data),
  download: (id) => api.get(`/v1/reports/${id}/download`, { responseType: 'blob' }),
  getHistory: (branchId) => api.get('/v1/reports/history', { params: { branchId } }),
};

export const branchesApi = {
  getAll: () => api.get('/v1/branches'),
  create: (data) => api.post('/v1/branches', data),
  update: (id, data) => api.put(`/v1/branches/${id}`, data),
  deactivate: (id) => api.delete(`/v1/branches/${id}`),
};

export const usersApi = {
  getAll: () => api.get('/v1/users'),
  updateRole: (id, roleName) => api.patch(`/v1/users/${id}/role`, null, { params: { roleName } }),
  toggleStatus: (id) => api.patch(`/v1/users/${id}/toggle-status`),
};

export const chatbotApi = {
  query: (data) => api.post('/v1/chatbot/query', data),
};

export const forecastApi = {
  getSalesForecast: (data) => api.post('/v1/forecasts/sales', data),
};
