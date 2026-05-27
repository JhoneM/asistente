# Planificación del Proyecto: HabitPet MVP

## 1. Definición del MVP

El MVP de HabitPet es la versión mínima del sistema que permite validar la hipótesis central:

> ¿Mejora la adopción y mantenimiento de hábitos cuando el progreso está vinculado a una mascota virtual con consecuencias emocionales por el incumplimiento?

### 1.1. Entregables del MVP

1. Aplicación web funcional accesible vía navegador (sin instalación)
2. Sistema de autenticación completo (registro, login, refresh token)
3. Módulo de hábitos (crear, editar, eliminar, listar)
4. Módulo de registro de cumplimiento diario (check-in por hábito)
5. Mascota con 5 estados visuales reactivos en tiempo real
6. Sistema de experiencia y niveles básico (10 niveles)
7. Dashboard de estadísticas básico (racha, % semanal, historial 7 días)
8. Notificaciones in-app para estado crítico de mascota

### 1.2. Criterio de "Done" del MVP

La aplicación permite que un usuario real complete el flujo completo sin intervención técnica:
**Registro → Crear hábito → Marcar cumplimiento → Ver cambio en la mascota → Ver estadísticas**

---

## 2. Backlog Priorizado (MoSCoW)

### Must Have — Sin esto no hay MVP

| ID    | Historia                                               | Estimación |
|-------|--------------------------------------------------------|------------|
| US-01 | Registro de usuario con email y contraseña             | 3 pts      |
| US-02 | Login con JWT y refresh token                          | 2 pts      |
| US-03 | Crear hábito con nombre, categoría y frecuencia        | 3 pts      |
| US-09 | Editar y eliminar hábito existente                     | 2 pts      |
| US-04 | Marcar cumplimiento diario de un hábito (check-in)     | 3 pts      |
| US-05 | Mascota con 5 estados visuales reactivos en tiempo real | 5 pts     |
| US-06 | Dashboard con estado actual de mascota y nivel de XP   | 3 pts      |

**Total Must Have: 21 puntos**

### Should Have — Agrega valor significativo al MVP

| ID    | Historia                                               | Estimación |
|-------|--------------------------------------------------------|------------|
| US-07 | Estadísticas semanales de cumplimiento por hábito      | 3 pts      |
| US-08 | Notificación in-app cuando mascota está en estado crítico | 2 pts   |
| US-10 | Racha de días consecutivos con check-ins               | 2 pts      |
| —     | Historial de cumplimiento (últimos 30 días)            | 2 pts      |

**Total Should Have: 9 puntos**

### Could Have — Si el tiempo lo permite

| ID    | Historia                                               | Estimación |
|-------|--------------------------------------------------------|------------|
| —     | Selección de tipo de mascota al registrarse (3 opciones) | 3 pts    |
| —     | Perfil de usuario editable (nombre, foto)              | 2 pts      |
| —     | Historial visual de evolución de la mascota             | 3 pts     |

**Total Could Have: 8 puntos**

### Won't Have — No entra en el MVP

- Sistema social (compartir mascotas con amigos)
- Notificaciones push en dispositivos móviles
- Integración con wearables (Apple Health, Google Fit)
- Múltiples mascotas por usuario
- Gamificación avanzada (logros, medallas, rankings)
- IA para sugerencias de hábitos
- Exportación de datos

---

## 3. Fases de Desarrollo

Planificado para un equipo de 4 personas durante 10 semanas.

---

### Fase 0: Setup y Diseño Base (Semanas 1–2)

**Objetivo**: Repositorio configurado, entorno de desarrollo funcionando, decisiones de diseño tomadas.

**Actividades:**
- Configuración del repositorio (monorepo o repos separados)
- Docker Compose con PostgreSQL y pgAdmin
- Inicialización del proyecto Spring Boot con estructura de paquetes por dominio
- Inicialización del proyecto React + Vite + TypeScript
- Setup de ESLint, Prettier y Husky (pre-commit hooks)
- Diseño de entidades JPA y scripts de migración Flyway
- Definición del algoritmo de cálculo de estado de mascota
- Wireframes de las pantallas principales (Figma o papel)
- Setup de GitHub Actions: pipeline de lint y tests
- Sesión de capacitación técnica: Spring Boot, Spring Data JPA, patrones SOLID/GRASP

**Entregable**: Repositorio funcional donde `docker-compose up` levanta el entorno completo

**Riesgos de fase**: Divergencia en decisiones técnicas del equipo → Resolución en ADRs formales antes de implementar

---

### Fase 1: Core Backend (Semanas 3–4)

**Objetivo**: API REST funcional, testeable con Postman/Thunder Client, con toda la lógica de negocio del MVP.

**Actividades:**
- Módulo de autenticación: registro, login, refresh token, guards JWT
- Módulo de usuarios: perfil básico
- Módulo de hábitos: CRUD completo con validaciones de dominio
- Módulo de registros: check-in diario con regla de "una vez por día"
- Motor de cálculo de estado de mascota (patrón Strategy)
- Módulo de mascota: estado, XP, niveles
- Tests unitarios de la lógica de negocio (>70% cobertura)
- Documentación Swagger automática (SpringDoc OpenAPI @Operation, @Schema)

**Entregable**: Colección de Postman con todos los endpoints funcionales

**Dependencias críticas**: Entidades JPA y schema de Flyway aprobados en Fase 0

---

### Fase 2: Core Frontend (Semanas 5–6)

**Objetivo**: Flujo principal funcional end-to-end.

**Actividades:**
- Setup de React Router (rutas públicas y privadas)
- Pantallas de autenticación (login y registro)
- Dashboard principal con mascota y estado
- Implementación de la mascota con estados visuales (SVG + CSS animations)
- Pantalla de gestión de hábitos (lista, crear, editar, eliminar)
- Pantalla de check-in diario
- Zustand store: usuario autenticado, mascota, hábitos
- Integración con API backend (axios + interceptores JWT)
- Manejo de loading states y errores en UI

**Entregable**: Demo funcional del flujo completo en localhost

**Dependencias críticas**: Fase 1 completada (API REST funcional)

---

### Fase 3: Tiempo Real y Features Complementarias (Semanas 7–8)

**Objetivo**: MVP feature-complete con tiempo real y estadísticas.

**Actividades:**
- Integración de Spring WebSocket (STOMP broker + @MessageMapping)
- Integración de SockJS + STOMP client en frontend (cliente React)
- Notificaciones in-app (toast + badge) para estado crítico de mascota
- Módulo de estadísticas: racha, % semanal, historial 7 días
- Pantalla de estadísticas con gráficos (Recharts)
- Refinamiento de UX: animaciones de transición de estados de mascota
- Manejo de errores global (interceptores, páginas de error)

**Entregable**: MVP feature-complete corriendo en localhost con tiempo real funcional

---

### Fase 4: Testing, QA y Documentación (Semanas 9–10)

**Objetivo**: MVP listo para presentación y evaluación académica.

**Actividades:**
- Tests de integración del backend (Supertest sobre los controllers)
- Tests end-to-end básicos (Playwright): flujo registro → check-in → ver mascota
- Revisión de seguridad básica (OWASP Top 10: inyección SQL, auth, XSS)
- Documentación técnica del proyecto (README, guía de setup)
- Documentación académica: RFC, Arquitectura, SOLID/GRASP, Patrones de diseño
- Deploy en entorno de demo (Railway, Render o similar para backend; Vercel para frontend)
- Preparación de la presentación y demo script

**Entregable**: MVP deployado, documentado y listo para defensa

---

## 4. Diagrama de Gantt

```
Tarea                          | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10|
-------------------------------|----|----|----|----|----|----|----|----|----|----|
Setup repositorio y Docker     | ██ |    |    |    |    |    |    |    |    |    |
Entidades JPA y ADRs           | ██ | ██ |    |    |    |    |    |    |    |    |
Wireframes y diseño visual     |    | ██ |    |    |    |    |    |    |    |    |
CI/CD GitHub Actions           | ██ |    |    |    |    |    |    |    |    |    |
Auth módulo (BE)               |    |    | ██ |    |    |    |    |    |    |    |
Hábitos módulo (BE)            |    |    | ██ | ██ |    |    |    |    |    |    |
Records módulo (BE)            |    |    |    | ██ |    |    |    |    |    |    |
Algoritmo estado mascota (BE)  |    |    |    | ██ |    |    |    |    |    |    |
Tests unitarios BE             |    |    | ██ | ██ |    |    |    |    |    |    |
Auth pantallas (FE)            |    |    |    |    | ██ |    |    |    |    |    |
Dashboard + Mascota (FE)       |    |    |    |    | ██ | ██ |    |    |    |    |
Gestión hábitos (FE)           |    |    |    |    | ██ | ██ |    |    |    |    |
Integración API (FE)           |    |    |    |    |    | ██ |    |    |    |    |
WebSocket Gateway (BE)         |    |    |    |    |    |    | ██ |    |    |    |
WebSocket client (FE)          |    |    |    |    |    |    | ██ |    |    |    |
Notificaciones in-app          |    |    |    |    |    |    | ██ |    |    |    |
Estadísticas + gráficos (FE)   |    |    |    |    |    |    |    | ██ |    |    |
Refinamiento UX/animaciones    |    |    |    |    |    |    |    | ██ |    |    |
Tests integración + E2E        |    |    |    |    |    |    |    |    | ██ |    |
Deploy + documentación         |    |    |    |    |    |    |    |    | ██ | ██ |
Preparación presentación       |    |    |    |    |    |    |    |    |    | ██ |
```

---

## 5. Dependencias entre Módulos

Las dependencias representan qué debe estar completado antes de poder empezar cada módulo:

```
AuthModule (JWT, Guards)
    │
    ├── HabitModule (CRUD de hábitos del usuario)
    │       │
    │       └── RecordModule (check-in diario)
    │               │
    │               └── PetModule (motor de cálculo de estado)
    │                       │
    │                       ├── WebSocketModule (broadcasting en tiempo real)
    │                       │
    │                       └── NotificationModule (alertas de estado crítico)
    │
    └── StatsModule (estadísticas — puede desarrollarse en paralelo a WS)
```

**Paralelismo posible en Fase 3**: WebSocketModule y StatsModule pueden desarrollarse en paralelo una vez que PetModule esté completo.

---

## 6. Distribución de Responsabilidades

Para un equipo de 4 personas con roles complementarios:

| Rol                      | Responsabilidades Principales                                          |
|--------------------------|------------------------------------------------------------------------|
| Tech Lead / Arquitecto   | ADRs, code review, CI/CD, integración final, resolución de blockers    |
| Backend Developer        | Spring Boot services/repos, entidades JPA, algoritmo de mascota, tests unitarios (JUnit 5)   |
| Frontend Developer       | React components, Zustand store, animaciones SVG de mascota            |
| Full Stack / QA          | WebSockets, estadísticas, tests E2E, documentación técnica             |

**Nota sobre el Tech Lead**: Además de las responsabilidades técnicas, el Tech Lead facilita las decisiones de arquitectura (ADRs), revisa que el código cumpla los principios SOLID/GRASP y coordina las integraciones entre Frontend y Backend.

---

## 7. Riesgos del Equipo y Plan de Contingencia

| Riesgo                                     | Probabilidad | Impacto | Plan de Contingencia                                               |
|--------------------------------------------|-------------|---------|---------------------------------------------------------------------|
| Curva de aprendizaje Spring Boot + JPA                | Alta        | Medio   | Spike técnico en S1 (2 días). Recursos: documentación oficial + ejemplos |
| Diseño visual de mascota sin diseñador     | Media       | Bajo    | Usar SVG open source (Phosphor Icons, OpenGameArt). Iteración en v2 |
| Ausencia de miembro del equipo en parciales | Alta       | Alto    | README de setup detallado; pair programming frecuente; backlog priorizado permite reasignación |
| Deuda técnica acumulada en Fase 2          | Media       | Medio   | Revisión de arquitectura obligatoria al final de Fase 2 antes de agregar features |
| Problemas de integración FE–BE             | Media       | Alto    | Contrato de API (Swagger) definido en Fase 1 antes de que FE empiece |
| Scope creep informal                       | Alta        | Medio   | Definition of Done por historia; toda nueva feature va al backlog y se prioriza formalmente |

---

## 8. Definición de "Done" por Tipo de Tarea

### Historia de Usuario
- Código implementado y revisado por al menos un compañero
- Tests unitarios escritos (si aplica a lógica de negocio)
- Integración verificada localmente end-to-end
- Criterio de aceptación de la historia validado manualmente

### Módulo de Backend
- Todos los endpoints documentados en Swagger
- Cobertura de tests mayor al 70% en la lógica de negocio
- Sin errores de compilación Java en modo estricto
- Validación de inputs con class-validator

### Componente de Frontend
- Render correcto en desktop (1280px) y mobile (375px)
- Loading state y error state implementados
- Sin errores en consola del navegador en flujo normal
- Accesibilidad básica (labels en inputs, roles ARIA donde aplica)

---

## 9. Estimación de Esfuerzo Total

| Fase                        | Semanas | Puntos de Historia |
|-----------------------------|---------|-------------------|
| Fase 0: Setup y Diseño      | 1–2     | 10 pts (no stories, tasks técnicas) |
| Fase 1: Core Backend        | 3–4     | 13 pts             |
| Fase 2: Core Frontend       | 5–6     | 13 pts             |
| Fase 3: Tiempo Real + Stats | 7–8     | 9 pts              |
| Fase 4: Testing + Docs      | 9–10    | 5 pts              |
| **Total**                   | **10**  | **50 pts**         |

**Velocidad estimada del equipo**: 5–8 puntos por persona por semana (equipo de 4 → 20–32 pts/semana disponibles). El MVP (Must + Should = 30 pts de lógica + tasks técnicas) cabe holgadamente en 10 semanas con buffer para imprevistos.
