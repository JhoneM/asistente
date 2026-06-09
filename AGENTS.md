# HabitPet — Contexto para Agentes IA

## Qué es
Proyecto universitario UADE. App web tipo Tamagotchi vinculado a hábitos saludables.

## Stack
- Frontend: React 18 + TypeScript + Vite + Zustand + Tailwind
- Backend: Spring Boot 3 + Java 21 + Spring Data JPA + Spring Security + Spring WebSocket
- DB: PostgreSQL 16
- Build: Maven (BE), npm (FE), Docker Compose

## Arrancar
`docker compose up --build` (o `make up`)
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Postgres: localhost:5432 (user/pass: habitpet/habitpet)

## Datos de prueba
La primera vez que se arranca, el seeder pobla la DB con 3 usuarios de prueba:
- demo@habitpet.com / demo1234
- maria@habitpet.com / demo1234
- lucas@habitpet.com / demo1234

Cada uno con hábitos, registros e historial precargado.
Para resetear: `make reset` (borra volumen y vuelve a poblar).

## Estructura
Backend actual organizado por capas en `com.habitpet`: `controllers`, `services`, `models`, `repositories`, `dtos`, `exceptions`, `configs`, `websockets`, `events` y `bases/seed`.
La documentacion propone una evolucion hacia paquetes DDD por dominio (`auth`, `habit`, `record`, `pet`, `stats`, `notification`, `websocket`, `shared`), pero esa no es la estructura implementada hoy. No asumir paquetes por dominio al codear.

## Convenciones
- Idioma de comentarios y commits: español
- Constructor injection (sin @Autowired en campos)
- Lombok permitido: @RequiredArgsConstructor, @Data, @Builder
- Value Objects como Java records (Java 21)
- Tests con nombres descriptivos en español

## Documentación
- docs/RFC.md — Propuesta técnica y alcance
- docs/PLANIFICACION.md — Backlog priorizado y fases
- docs/ARQUITECTURA.md — Diagramas C4, capas, flujos
- docs/MODELO_DOMINIO.md — Entidades JPA + scripts Flyway
- docs/SOLID_Y_GRASP.md — Principios aplicados
- docs/PATRONES_DISENO.md — Patrones GoF (revisar al codear cuáles aplican)
- docs/ADR/ — Decisiones de arquitectura
- docs/diagramas.html — Visor interactivo de la arquitectura
- docs/SCAFFOLDING_PLAN.md — Plan de scaffolding inicial

## Estado del proyecto
- [x] Documentacion academica completa
- [x] Scaffolding inicial frontend/backend/Docker
- [x] Migraciones Flyway y datos de prueba
- [x] Auth backend/frontend: registro y login con JWT stateless
- [x] Factory simple: se crea mascota inicial al registrar usuario
- [x] Habit backend/frontend: crear, listar, editar y archivar habitos
- [x] Record + pet backend: check-in diario, recalculo de bienestar, XP y niveles
- [x] Decorator simple: bonus de XP segun frecuencia semanal del habito
- [x] WebSocket backend/frontend para actualizacion de mascota
- [x] Tests unitarios backend existentes
- [x] Encoding de archivos revisado: contenido UTF-8 correcto; si PowerShell muestra mojibake, es salida de consola
- [~] Frontend demo: login/registro, dashboard, mascota, gestion de habitos y check-in
- [ ] Refresh token real y cookies HttpOnly
- [ ] Estadisticas, rachas y notificaciones conectadas a API/UI

## Decisiones críticas
- DB: PostgreSQL (no MySQL) → ver ADR-003
- Tiempo real: Spring WebSocket + STOMP (no Socket.io) → ver ADR-006
- Backend: Java/Spring Boot (no NestJS) → ver ADR-002
- Patrones de diseño: NO forzar. Aplicar solo los que el código realmente pida durante la implementación. Documentar el descarte si no aplica.
