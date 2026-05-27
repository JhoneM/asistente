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
Backend organizado por dominio (DDD packages): auth, habit, record, pet, stats, notification, websocket, shared.
Cada paquete contiene: controller, service, domain, repository, dto.

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
- [x] Documentación completa
- [ ] Scaffolding inicial
- [ ] Implementación auth module
- [ ] Implementación habit module
- [ ] Implementación record + pet modules
- [ ] WebSocket en tiempo real
- [ ] Frontend completo

## Decisiones críticas
- DB: PostgreSQL (no MySQL) → ver ADR-003
- Tiempo real: Spring WebSocket + STOMP (no Socket.io) → ver ADR-006
- Backend: Java/Spring Boot (no NestJS) → ver ADR-002
- Patrones de diseño: NO forzar. Aplicar solo los que el código realmente pida durante la implementación. Documentar el descarte si no aplica.
