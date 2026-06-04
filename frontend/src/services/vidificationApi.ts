import axios from 'axios'
import type { Avatar, Habito, ResultadoCompletar } from '../types/vidification'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

const api = axios.create({
  baseURL: API_URL,
})

export const USUARIO_DEMO_ID = 1

export async function obtenerAvatar(usuarioId: number): Promise<Avatar> {
  const { data } = await api.get<Avatar>(`/api/usuarios/${usuarioId}/avatar`)
  return data
}

export async function obtenerHabitos(usuarioId: number): Promise<Habito[]> {
  const { data } = await api.get<Habito[]>(`/api/usuarios/${usuarioId}/habitos`)
  return data
}

export async function completarHabito(habitoId: number): Promise<ResultadoCompletar> {
  const { data } = await api.post<ResultadoCompletar>(`/api/habitos/${habitoId}/completar`)
  return data
}

export async function pasarDia(usuarioId: number): Promise<void> {
  await api.post(`/api/usuarios/${usuarioId}/pasar-dia`)
}
