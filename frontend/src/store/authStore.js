import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (user, token) => set({ user, token, isAuthenticated: true }),
      logout: () => set({ user: null, token: null, isAuthenticated: false }),

      hasRole: (role) => {
        const state = useAuthStore.getState();
        return state.user?.role === role;
      },
      hasAnyRole: (roles) => {
        const state = useAuthStore.getState();
        return roles.includes(state.user?.role);
      },
    }),
    { name: 'sbadss-auth' }
  )
);
