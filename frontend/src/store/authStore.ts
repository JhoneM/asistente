import { create } from 'zustand'

interface AuthState {
  token: string | null
  userId: string | null
  email: string | null
  displayName: string | null
  login: (token: string, userId: string, email: string, displayName: string) => void
  logout: () => void
  isAuthenticated: () => boolean
}

// NOTE: Using localStorage for token persistence for demo/presentation purposes.
// This approach is simple but exposes the token to XSS attacks.
// In a real production environment, use HttpOnly cookies managed by the server.
const STORAGE_KEY = 'habitpet_auth'

const loadFromStorage = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

const saved = loadFromStorage()

export const useAuthStore = create<AuthState>((set, get) => ({
  token: saved?.token ?? null,
  userId: saved?.userId ?? null,
  email: saved?.email ?? null,
  displayName: saved?.displayName ?? null,

  login: (token, userId, email, displayName) => {
    const state = { token, userId, email, displayName }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
    set(state)
  },

  logout: () => {
    localStorage.removeItem(STORAGE_KEY)
    set({ token: null, userId: null, email: null, displayName: null })
  },

  isAuthenticated: () => get().token !== null,
}))
