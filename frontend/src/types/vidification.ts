export type EstadoAvatar = 'SALUDABLE' | 'NORMAL' | 'DECAIDO' | 'CRITICO' | string;

export type Avatar = {
  nombreUsuario: string;
  vitalidad: number;
  estado: EstadoAvatar;
  emoji: string;
  mensaje: string;
  puntos: number;
};

export type Habito = {
  id: number;
  nombre: string;
  categoria: string;
  dificultad: string;
  rachaActual: number;
  completadoHoy: boolean;
  puntaje: number;
};

export type ResultadoCompletar = {
  habito: string;
  puntosGanados: number;
  puntosTotales: number;
  vitalidad: number;
  estado: EstadoAvatar;
  emoji: string;
};
