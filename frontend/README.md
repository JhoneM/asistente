# Frontend — HabitPet

React 18 + TypeScript + Vite + Tailwind CSS.

## Estructura

```
src/
├── components/      Componentes React (common, layout, pet, habits)
├── pages/           Páginas (auth, dashboard, habits, stats)
├── hooks/           Custom hooks (useAuth, usePet, useWebSocket)
├── store/           Zustand stores (auth, pet, habit)
├── services/        API client (axios) y WebSocket (STOMP)
├── types/           TypeScript tipos y DTOs
├── utils/           Helpers (fechas, formateo)
├── styles/          Estilos globales
├── routes/          Router config
└── App.tsx          Componente raíz
```

## Build y arranque

```bash
# Instalar dependencias
npm install

# Desarrollo (hot reload)
npm run dev

# Build producción
npm build

# Preview
npm run preview

# Tests
npm run test

# Lint
npm run lint
```

## En Docker

```bash
docker compose up --build frontend
```

El frontend estará disponible en `http://localhost:5173`.
