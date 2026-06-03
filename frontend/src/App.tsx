import { useAuthStore } from './store/authStore'
import { LoginPage } from './pages/auth/LoginPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'

function App() {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated())

  return isAuthenticated ? <DashboardPage /> : <LoginPage />
}

export default App
