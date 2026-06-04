# Frontend - HabitPet

React 18 + TypeScript + Vite + Tailwind CSS + MUI.

La pantalla actual consume la API del módulo `vidification` y traslada la experiencia de consola a
un dashboard web: avatar, vitalidad, hábitos diarios, completar hábito y terminar día.

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
npm run build

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

## Vidification UI

Para ver el dashboard con datos reales, levantá primero la API de `vidification` en el puerto 8080:

```bash
cd ../vidification
./mvnw spring-boot:run
```

En otra terminal:

```bash
cd ../frontend
npm install
npm run dev
```

Abrí `http://localhost:5173`. Vite proxya `/api` hacia `http://localhost:8080`, así que no hace
falta cambiar CORS en el backend.
