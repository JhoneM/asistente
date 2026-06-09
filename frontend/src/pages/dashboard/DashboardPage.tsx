import { useEffect, useState, useCallback, FormEvent } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '../../store/authStore'

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

type PetState = 'CRITICAL' | 'POOR' | 'NEUTRAL' | 'GOOD' | 'EXCELLENT'
type HabitCategory = 'HEALTH' | 'STUDY' | 'SPORT' | 'WELLNESS' | 'NUTRITION'

interface PetData {
  petId: string
  petName: string
  state: PetState
  xp: number
  level: number
  wellnessScore: number
}

interface HabitData {
  id: string
  name: string
  description?: string
  category: HabitCategory
  weeklyFrequency: number
}

interface HabitForm {
  name: string
  description: string
  category: HabitCategory
  weeklyFrequency: number
}

const EMPTY_FORM: HabitForm = {
  name: '',
  description: '',
  category: 'HEALTH',
  weeklyFrequency: 7,
}

const PET_VISUALS: Record<PetState, { emoji: string; color: string; label: string; bg: string }> = {
  CRITICAL: { emoji: '🥚', color: 'text-red-600', label: 'Critical', bg: 'bg-red-50 border-red-200' },
  POOR: { emoji: '🐣', color: 'text-orange-500', label: 'Poor', bg: 'bg-orange-50 border-orange-200' },
  NEUTRAL: { emoji: '🐥', color: 'text-yellow-500', label: 'Stable', bg: 'bg-yellow-50 border-yellow-200' },
  GOOD: { emoji: '🐤', color: 'text-green-500', label: 'Good', bg: 'bg-green-50 border-green-200' },
  EXCELLENT: { emoji: '🐦', color: 'text-emerald-600', label: 'Excellent', bg: 'bg-emerald-50 border-emerald-200' },
}

const XP_THRESHOLDS = [0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700]
const CATEGORIES: HabitCategory[] = ['HEALTH', 'STUDY', 'SPORT', 'WELLNESS', 'NUTRITION']
const CATEGORY_LABELS: Record<HabitCategory, string> = {
  HEALTH: 'Salud',
  STUDY: 'Estudio',
  SPORT: 'Deporte',
  WELLNESS: 'Bienestar',
  NUTRITION: 'Alimentacion',
}

export function DashboardPage() {
  const { token, userId, displayName, logout } = useAuthStore()
  const [pet, setPet] = useState<PetData | null>(null)
  const [connected, setConnected] = useState(false)
  const [lastUpdate, setLastUpdate] = useState<string | null>(null)
  const [habits, setHabits] = useState<HabitData[]>([])
  const [form, setForm] = useState<HabitForm>(EMPTY_FORM)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [checkingIn, setCheckingIn] = useState<string | null>(null)
  const [completedToday, setCompletedToday] = useState<Set<string>>(new Set())
  const [savingHabit, setSavingHabit] = useState(false)
  const [feedback, setFeedback] = useState<{ habitId?: string; message: string; ok: boolean } | null>(null)

  const authHeaders = useCallback(() => ({
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  }), [token])

  const fetchHabits = useCallback(async () => {
    const res = await fetch(`${API_BASE}/api/habits`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (res.ok) setHabits(await res.json())
  }, [token])

  const fetchPet = useCallback(async () => {
    const res = await fetch(`${API_BASE}/api/pets/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (res.ok) setPet(await res.json())
  }, [token])

  useEffect(() => {
    fetchHabits()
    fetchPet()
  }, [fetchHabits, fetchPet])

  useEffect(() => {
    if (!userId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE}/ws`),
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/pet/${userId}`, (message) => {
          const data: PetData = JSON.parse(message.body)
          setPet(data)
          setLastUpdate(new Date().toLocaleTimeString())
        })
      },
      onDisconnect: () => setConnected(false),
      reconnectDelay: 3000,
    })

    client.activate()
    return () => { void client.deactivate() }
  }, [userId])

  const readError = async (res: Response) => {
    try {
      const data = await res.json()
      return data.message ?? 'Request failed'
    } catch {
      return 'Request failed'
    }
  }

  const resetForm = () => {
    setForm(EMPTY_FORM)
    setEditingId(null)
  }

  const handleHabitSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setSavingHabit(true)
    setFeedback(null)
    try {
      const url = editingId ? `${API_BASE}/api/habits/${editingId}` : `${API_BASE}/api/habits`
      const res = await fetch(url, {
        method: editingId ? 'PUT' : 'POST',
        headers: authHeaders(),
        body: JSON.stringify(form),
      })

      if (!res.ok) {
        setFeedback({ message: await readError(res), ok: false })
        return
      }

      setFeedback({ message: editingId ? 'Habit updated.' : 'Habit created.', ok: true })
      resetForm()
      await fetchHabits()
    } finally {
      setSavingHabit(false)
      setTimeout(() => setFeedback(null), 3000)
    }
  }

  const handleEdit = (habit: HabitData) => {
    setEditingId(habit.id)
    setForm({
      name: habit.name,
      description: habit.description ?? '',
      category: habit.category,
      weeklyFrequency: habit.weeklyFrequency,
    })
  }

  const handleArchive = async (habitId: string) => {
    setFeedback(null)
    setCompletedToday(previous => {
      const next = new Set(previous)
      next.delete(habitId)
      return next
    })
    const res = await fetch(`${API_BASE}/api/habits/${habitId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })

    if (!res.ok) {
      setFeedback({ habitId, message: await readError(res), ok: false })
      return
    }

    setFeedback({ habitId, message: 'Habit archived.', ok: true })
    if (editingId === habitId) resetForm()
    await fetchHabits()
    setTimeout(() => setFeedback(null), 3000)
  }

  const handleCheckIn = async (habitId: string) => {
    setCheckingIn(habitId)
    setFeedback(null)
    try {
      const res = await fetch(`${API_BASE}/api/records`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify({ habitId }),
      })

      if (res.ok) {
        setCompletedToday(previous => new Set(previous).add(habitId))
        setFeedback({ habitId, message: 'Registro guardado.', ok: true })
        await fetchPet()
      } else {
        setFeedback({ habitId, message: await readError(res), ok: false })
      }
    } finally {
      setCheckingIn(null)
      setTimeout(() => setFeedback(null), 3000)
    }
  }

  const visual = pet ? PET_VISUALS[pet.state] : null
  const xpForNext = pet && pet.level < 10 ? XP_THRESHOLDS[pet.level] : null
  const xpProgress = pet && xpForNext
    ? ((pet.xp - XP_THRESHOLDS[pet.level - 1]) / (xpForNext - XP_THRESHOLDS[pet.level - 1])) * 100
    : 100
  const xpRemaining = pet && xpForNext ? Math.max(xpForNext - pet.xp, 0) : 0

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 to-amber-50">
      <header className="bg-white shadow-sm px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-emerald-700">HabitPet</h1>
        <div className="flex items-center gap-4">
          <span className="text-gray-600 text-sm">{displayName}</span>
          <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-400' : 'bg-gray-300'}`} title={connected ? 'WebSocket connected' : 'Connecting...'} />
          <button onClick={logout} className="text-sm text-gray-500 hover:text-red-500 transition">Salir</button>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-[0.8fr_1.2fr] gap-6">
        <section className={`rounded-lg border-2 p-6 text-center shadow-sm ${visual?.bg ?? 'bg-white border-gray-200'}`}>
          <div className="text-8xl mb-4 transition-all duration-500" role="img" aria-label={visual?.label ?? 'Pet'}>
            {visual?.emoji ?? '🐣'}
          </div>
          <h2 className="text-2xl font-bold text-gray-800">{pet?.petName ?? '-'}</h2>
          <p className={`text-lg font-semibold mt-1 ${visual?.color ?? 'text-gray-400'}`}>
            {visual?.label ?? 'Loading...'}
          </p>

          {pet && (
            <div className="mt-4 space-y-2 text-sm text-gray-600">
              <div className="flex justify-between">
                <span>Nivel {pet.level}</span>
                <span>{pet.xp} XP</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className="bg-emerald-500 h-2 rounded-full transition-all duration-700"
                  style={{ width: `${Math.min(xpProgress, 100)}%` }}
                />
              </div>
              <p className="text-xs text-gray-400">
                Bienestar: {pet.wellnessScore.toFixed(1)}%
                {lastUpdate && <span className="ml-2">Actualizado {lastUpdate}</span>}
              </p>
              <div className="grid grid-cols-3 gap-2 pt-3">
                <div className="bg-white/70 rounded-lg px-2 py-2">
                  <p className="text-lg font-bold text-gray-800">{habits.length}</p>
                  <p className="text-[11px] text-gray-500">Habitos</p>
                </div>
                <div className="bg-white/70 rounded-lg px-2 py-2">
                  <p className="text-lg font-bold text-gray-800">{xpRemaining}</p>
                  <p className="text-[11px] text-gray-500">XP falta</p>
                </div>
                <div className="bg-white/70 rounded-lg px-2 py-2">
                  <p className={`text-lg font-bold ${connected ? 'text-emerald-600' : 'text-gray-400'}`}>
                    {connected ? 'Live' : 'Off'}
                  </p>
                  <p className="text-[11px] text-gray-500">Tiempo real</p>
                </div>
              </div>
            </div>
          )}
        </section>

        <section className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between gap-4 mb-4">
            <h3 className="text-lg font-semibold text-gray-800">Habitos</h3>
            {editingId && (
              <button type="button" onClick={resetForm} className="text-sm text-gray-500 hover:text-gray-800">
                Cancelar
              </button>
            )}
          </div>

          <form onSubmit={handleHabitSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-6">
            <input
              value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })}
              placeholder="Nombre del habito"
              required
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
            />
            <select
              value={form.category}
              onChange={e => setForm({ ...form, category: e.target.value as HabitCategory })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
            >
              {CATEGORIES.map(category => <option key={category} value={category}>{CATEGORY_LABELS[category]}</option>)}
            </select>
            <input
              value={form.description}
              onChange={e => setForm({ ...form, description: e.target.value })}
              placeholder="Descripcion"
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
            />
            <select
              value={form.weeklyFrequency}
              onChange={e => setForm({ ...form, weeklyFrequency: Number(e.target.value) })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-400 focus:outline-none"
            >
              {[1, 2, 3, 4, 5, 6, 7].map(day => <option key={day} value={day}>{day} dia{day > 1 ? 's' : ''} por semana</option>)}
            </select>
            <button
              type="submit"
              disabled={savingHabit}
              className="md:col-span-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-2 rounded-lg transition disabled:opacity-50"
            >
              {savingHabit ? 'Guardando...' : editingId ? 'Guardar habito' : 'Agregar habito'}
            </button>
          </form>

          {feedback && !feedback.habitId && (
            <p className={`text-sm mb-3 ${feedback.ok ? 'text-green-600' : 'text-red-600'}`}>
              {feedback.message}
            </p>
          )}

          {habits.length === 0 ? (
            <p className="text-gray-400 text-sm">No hay habitos activos.</p>
          ) : (
            <ul className="space-y-3">
              {habits.map(habit => (
                <li key={habit.id} className="border border-gray-100 rounded-lg p-3">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="font-medium text-gray-700">{habit.name}</p>
                      <p className="text-xs text-gray-400">
                        {CATEGORY_LABELS[habit.category]} | {habit.weeklyFrequency} dia{habit.weeklyFrequency > 1 ? 's' : ''}/semana
                      </p>
                      {habit.description && <p className="text-sm text-gray-500 mt-1">{habit.description}</p>}
                    </div>
                    <div className="flex flex-wrap justify-end gap-2">
                      {(() => {
                        const isCompleted = completedToday.has(habit.id)
                        return (
                      <button
                        onClick={() => handleCheckIn(habit.id)}
                        disabled={checkingIn === habit.id || isCompleted}
                        className="bg-emerald-600 hover:bg-emerald-700 text-white text-sm px-3 py-1.5 rounded-lg transition disabled:opacity-50"
                      >
                        {checkingIn === habit.id ? '...' : isCompleted ? 'Hecho hoy' : 'Hecho'}
                      </button>
                        )
                      })()}
                      <button
                        onClick={() => handleEdit(habit)}
                        className="border border-gray-300 text-gray-600 hover:text-emerald-600 text-sm px-3 py-1.5 rounded-lg transition"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => handleArchive(habit.id)}
                        className="border border-gray-300 text-gray-600 hover:text-red-600 text-sm px-3 py-1.5 rounded-lg transition"
                      >
                        Archivar
                      </button>
                    </div>
                  </div>
                  {feedback?.habitId === habit.id && (
                    <p className={`text-xs mt-2 ${feedback.ok ? 'text-green-500' : 'text-red-500'}`}>
                      {feedback.message}
                    </p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  )
}
