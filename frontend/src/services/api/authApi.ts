const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  userId: string
  email: string
  displayName: string
}

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.message ?? 'Login failed')
  }

  return response.json()
}
