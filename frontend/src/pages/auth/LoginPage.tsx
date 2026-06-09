import { useState, FormEvent } from 'react'
import { useAuthStore } from '../../store/authStore'
import { login, register } from '../../services/api/authApi'

export function LoginPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const loginStore = useAuthStore(s => s.login)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const data = mode === 'login'
        ? await login({ email, password })
        : await register({
            email,
            password,
            displayName,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'UTC',
          })
      loginStore(data.token, data.userId, data.email, data.displayName)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed')
    } finally {
      setLoading(false)
    }
  }

  const toggleMode = () => {
    setMode(current => current === 'login' ? 'register' : 'login')
    setError(null)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 to-amber-50 flex items-center justify-center px-4">
      <div className="bg-white rounded-lg shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-emerald-700">HabitPet</h1>
          <p className="text-gray-500 mt-1">
            {mode === 'login' ? 'Your habits. Your pet. Your progress.' : 'Create your pet and start today.'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {mode === 'register' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Display name</label>
              <input
                type="text"
                value={displayName}
                onChange={e => setDisplayName(e.target.value)}
                placeholder="Your name"
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="demo@habitpet.com"
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="demo1234"
              required
              minLength={6}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
            />
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 text-sm rounded-lg px-4 py-2">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-2 rounded-lg transition disabled:opacity-50"
          >
            {loading ? 'Please wait...' : mode === 'login' ? 'Login' : 'Create account'}
          </button>
        </form>

        <button
          type="button"
          onClick={toggleMode}
          className="w-full text-sm text-emerald-600 hover:text-emerald-800 mt-4"
        >
          {mode === 'login' ? 'Need an account? Register' : 'Already have an account? Login'}
        </button>

        <p className="text-center text-xs text-gray-400 mt-6">
          Demo users: demo@habitpet.com | maria@habitpet.com | lucas@habitpet.com<br />
          Password: demo1234
        </p>
      </div>
    </div>
  )
}
