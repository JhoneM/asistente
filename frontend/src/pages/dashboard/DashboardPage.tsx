import { useEffect, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '../../store/authStore'

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const WS_URL = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws'

type PetState = 'CRITICAL' | 'POOR' | 'NEUTRAL' | 'GOOD' | 'EXCELLENT'

interface PetData {
  petId: string
  petName: string
  state: PetState
  xp: number
  level: number
  wellnessScore: number
}

const PET_VISUALS: Record<PetState, { emoji: string; color: string; label: string; bg: string }> = {
  CRITICAL:  { emoji: '😵', color: 'text-red-600',    label: 'Critical',  bg: 'bg-red-50 border-red-200' },
  POOR:      { emoji: '😢', color: 'text-orange-500', label: 'Poor',      bg: 'bg-orange-50 border-orange-200' },
  NEUTRAL:   { emoji: '😐', color: 'text-yellow-500', label: 'Stable',    bg: 'bg-yellow-50 border-yellow-200' },
  GOOD:      { emoji: '😊', color: 'text-green-500',  label: 'Good',      bg: 'bg-green-50 border-green-200' },
  EXCELLENT: { emoji: '🌟', color: 'text-indigo-500', label: 'Excellent', bg: 'bg-indigo-50 border-indigo-200' },
}

const XP_THRESHOLDS = [0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700]

export function DashboardPage() {
  const { token, userId, displayName, logout } = useAuthStore()
  const [pet, setPet] = useState<PetData | null>(null)
  const [connected, setConnected] = useState(false)
  const [lastUpdate, setLastUpdate] = useState<string | null>(null)
  const [habits, setHabits] = useState<{ id: string; name: string; category: string }[]>([])
  const [checkingIn, setCheckingIn] = useState<string | null>(null)
  const [feedback, setFeedback] = useState<{ habitId: string; message: string; ok: boolean } | null>(null)

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

    // NOTE: Subscribing to a public topic per user (/topic/pet/{userId}).
    // This is a simplification for the university demo — we understand the risk
    // that any client knowing the userId could subscribe to this channel.
    // In a real environment, use authenticated WebSocket with convertAndSendToUser()
    // and a ChannelInterceptor that validates the JWT on each STOMP CONNECT frame.
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
    return () => { client.deactivate() }
  }, [userId])

  const handleCheckIn = async (habitId: string) => {
    setCheckingIn(habitId)
    setFeedback(null)
    try {
      const res = await fetch(`${API_BASE}/api/records`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ habitId }),
      })
      const data = await res.json()
      if (res.ok) {
        setFeedback({ habitId, message: 'Check-in recorded!', ok: true })
      } else {
        setFeedback({ habitId, message: data.message ?? 'Error', ok: false })
      }
    } finally {
      setCheckingIn(null)
      setTimeout(() => setFeedback(null), 3000)
    }
  }

  const visual = pet ? PET_VISUALS[pet.state] : null
  const xpForNext = pet && pet.level < 10 ? XP_THRESHOLDS[pet.level] : null
  const xpProgress = pet && xpForNext ? ((pet.xp - XP_THRESHOLDS[pet.level - 1]) / (xpForNext - XP_THRESHOLDS[pet.level - 1])) * 100 : 100

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-100">
      <header className="bg-white shadow-sm px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-indigo-700">HabitPet</h1>
        <div className="flex items-center gap-4">
          <span className="text-gray-600 text-sm">{displayName}</span>
          <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-400' : 'bg-gray-300'}`} title={connected ? 'WebSocket connected' : 'Connecting...'} />
          <button onClick={logout} className="text-sm text-gray-500 hover:text-red-500 transition">Logout</button>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-6 py-8 grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* Pet Card */}
        <div className={`rounded-2xl border-2 p-6 text-center shadow-sm ${visual?.bg ?? 'bg-white border-gray-200'}`}>
          <div className="text-8xl mb-4 transition-all duration-500" role="img">
            {visual?.emoji ?? '🐣'}
          </div>
          <h2 className="text-2xl font-bold text-gray-800">{pet?.petName ?? '—'}</h2>
          <p className={`text-lg font-semibold mt-1 ${visual?.color ?? 'text-gray-400'}`}>
            {visual?.label ?? 'Loading...'}
          </p>

          {pet && (
            <div className="mt-4 space-y-2 text-sm text-gray-600">
              <div className="flex justify-between">
                <span>Level {pet.level}</span>
                <span>{pet.xp} XP</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className="bg-indigo-500 h-2 rounded-full transition-all duration-700"
                  style={{ width: `${Math.min(xpProgress, 100)}%` }}
                />
              </div>
              <p className="text-xs text-gray-400">
                Wellness: {pet.wellnessScore.toFixed(1)}%
                {lastUpdate && <span className="ml-2">· Updated {lastUpdate}</span>}
              </p>
            </div>
          )}
        </div>

        {/* Habits */}
        <div className="bg-white rounded-2xl shadow-sm p-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Today's Habits</h3>
          {habits.length === 0 ? (
            <p className="text-gray-400 text-sm">No active habits found.</p>
          ) : (
            <ul className="space-y-3">
              {habits.map(h => (
                <li key={h.id} className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-gray-700">{h.name}</p>
                    <p className="text-xs text-gray-400">{h.category}</p>
                  </div>
                  <div className="text-right">
                    <button
                      onClick={() => handleCheckIn(h.id)}
                      disabled={checkingIn === h.id}
                      className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-3 py-1.5 rounded-lg transition disabled:opacity-50"
                    >
                      {checkingIn === h.id ? '...' : '✓ Done'}
                    </button>
                    {feedback?.habitId === h.id && (
                      <p className={`text-xs mt-1 ${feedback.ok ? 'text-green-500' : 'text-red-500'}`}>
                        {feedback.message}
                      </p>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </div>
  )
}
