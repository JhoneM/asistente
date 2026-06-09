# HabitPet

> Sistema web tipo Tamagotchi vinculado a hábitos saludables. La mascota virtual evoluciona o se deteriora según el cumplimiento de los hábitos del usuario.

Proyecto universitario — UADE.

---

## Tabla de contenidos

1. [Sobre el proyecto](#sobre-el-proyecto)
2. [Cómo funciona](#cómo-funciona)
3. [Estado actual](#estado-actual)
4. [Stack técnico](#stack-técnico)
5. [Arquitectura](#arquitectura)
6. [Requisitos previos](#requisitos-previos)
7. [Primera vez (arranque inicial)](#primera-vez-arranque-inicial)
8. [Encender el sistema después de la primera vez](#encender-el-sistema-después-de-la-primera-vez)
9. [Comandos útiles](#comandos-útiles)
10. [Modo desarrollo con hot reload](#modo-desarrollo-con-hot-reload)
11. [Usuarios de prueba](#usuarios-de-prueba)
12. [URLs del sistema](#urls-del-sistema)
13. [Estructura del proyecto](#estructura-del-proyecto)
14. [Persistencia de datos](#persistencia-de-datos)
15. [Documentación](#documentación)
16. [Resolución de problemas](#resolución-de-problemas)

---

## Sobre el proyecto

HabitPet es una aplicación web que combina la mecánica de mascotas virtuales (estilo Tamagotchi/Pou) con sistemas de seguimiento de hábitos saludables. El usuario configura los hábitos que quiere incorporar a su vida (estudiar, hacer ejercicio, tomar agua, leer, meditar, etc.) y a medida que los cumple, su mascota virtual evoluciona, mejora su apariencia y sube de nivel. Si los hábitos se descuidan, la mascota se deteriora visualmente.

**Hipótesis**: vincular el progreso de hábitos a una entidad virtual con la que se desarrolla un vínculo emocional aumenta la retención y la consistencia frente a las apps tradicionales de tracking con rachas y porcentajes abstractos.

Para detalles formales del proyecto, ver [docs/RFC.md](./docs/RFC.md).

---

## Cómo funciona

1. **Login** — El usuario inicia sesión con email y contraseña. El token JWT se almacena en localStorage.
2. **Dashboard** — El usuario ve el estado actual de su mascota (emoji + nivel + XP + wellness score).
3. **Check-in diario** — Marca como cumplido cada hábito que realizó en el día. Una sola vez por día por hábito.
4. **Reacción en tiempo real** — Tras cada check-in, el backend recalcula el bienestar de la mascota (promedio ponderado exponencial de los últimos 7 días) y envía el nuevo estado al frontend vía WebSocket STOMP. La mascota cambia su estado visualmente sin recargar la página.
5. **Estados de mascota** — Cinco niveles según el % de cumplimiento:
   - **CRITICAL** (0–20%) — 😵 La mascota está en peligro
   - **POOR** (21–40%) — 😢 La mascota no está bien
   - **NEUTRAL** (41–60%) — 😐 La mascota está estable
   - **GOOD** (61–80%) — 😊 La mascota está contenta
   - **EXCELLENT** (81–100%) — 🌟 La mascota está radiante
6. **Sistema de niveles** — La mascota acumula 10 XP por cada hábito completado. Tiene 10 niveles con umbrales progresivos.

---

## Estado actual

El repositorio ya contiene una demo funcional end-to-end del flujo principal: login, dashboard, listado de hábitos, check-in diario, recálculo de bienestar y actualización de mascota por WebSocket.

| Área | Estado |
|---|---|
| Documentación académica | Completa, con RFC, arquitectura, modelo de dominio, ADRs, SOLID/GRASP y patrones |
| Backend base | Implementado con Spring Boot, seguridad JWT, JPA, Flyway, WebSocket y tests unitarios |
| Auth | Registro y login backend/frontend con JWT stateless; falta refresh token real y cookies HttpOnly |
| Hábitos | Crear, listar, editar y archivar |
| Records + mascota | Check-in diario, regla de duplicado, wellness score, XP con bonus por frecuencia, niveles y broadcast STOMP |
| Frontend | Demo con login/registro, dashboard, mascota, gestión de hábitos y check-in; faltan estadísticas y notificaciones |
| Datos de prueba | Tres usuarios precargados con hábitos, registros, mascotas y notificaciones seed |
| Encoding | Archivos revisados como UTF-8; si PowerShell muestra caracteres rotos, es un problema de salida de consola |

---

## Stack técnico

| Capa | Tecnología |
|---|---|
| Frontend | React 18 + TypeScript + Vite + Zustand + TailwindCSS |
| Backend | Spring Boot 3.3 + Java 21 + Spring Data JPA + Spring Security + Spring WebSocket |
| Base de datos | PostgreSQL 16 |
| Tiempo real | STOMP sobre WebSocket con SockJS (topic público por usuario) |
| Migraciones | Flyway (V1 schema + V2 seed + V3 fix) |
| Autenticación | JWT stateless con Spring Security (jjwt 0.12.3) |
| Build | Maven (backend), npm + Vite (frontend) |
| Contenedores | Docker + Docker Compose |
| Documentación API | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5 + Mockito + AssertJ (46 tests unitarios) |

Cada decisión técnica está justificada en `docs/ADR/`.

---

## Arquitectura

El sistema corre en **3 contenedores** orquestados con Docker Compose, comunicados por una red privada (`habitpet-network`):

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  frontend   │───►│  backend    │───►│  postgres   │
│ React+Vite  │    │ Spring Boot │    │  v16-alpine │
│ :5173       │    │ :8080       │    │ :5432       │
└─────────────┘    └─────────────┘    └─────────────┘
       └──────── WebSocket (STOMP) ──────┘
```

**Flujo de un check-in:**
```
POST /api/records
  → RecordService (valida, persiste)
  → publica CheckInCompletedEvent (Observer)
       ↓ @EventListener
  → WellnessService (orquesta — Facade)
  → WellnessCalculator (Strategy — exponential weighted average)
  → PetService (actualiza estado + XP)
  → PetWebSocketGateway (broadcast STOMP)
       ↓ /topic/pet/{userId}
  Frontend actualiza mascota en tiempo real
```

**Paquetes del backend** (arquitectura en capas):
```
com.habitpet/
├── models/        Entidades JPA + enums + value objects
├── repositories/  Interfaces Spring Data JPA
├── services/      Lógica de negocio + Strategy + Observer
├── controllers/   Endpoints REST
├── dtos/          Request/Response records
├── exceptions/    Jerarquía AppException (Open/Closed Principle)
├── configs/       Spring Security + WebSocket + JWT filter
├── websockets/    Gateway STOMP
└── bases/seed/    DatabaseSeeder
```

Para diagramas C4, modelo de dominio y patrones aplicados, ver [docs/ARQUITECTURA.md](./docs/ARQUITECTURA.md) y [docs/diagramas.html](./docs/diagramas.html).

---

## Requisitos previos

Solo necesitás tener instalado:

| Herramienta | Versión mínima | Comando para verificar |
|---|---|---|
| Docker | 20.10+ | `docker --version` |
| Docker Compose | v2 (plugin de Docker) | `docker compose version` |
| Make (opcional) | cualquiera | `make --version` |

**No necesitás** Java, Node.js, Maven ni Postgres instalados en tu máquina — todo corre dentro de contenedores.

---

## Primera vez (arranque inicial)

### 1. Clonar el repositorio

```bash
git clone <url-del-repo>
cd asistente-develop
```

### 2. Crear el archivo de variables de entorno

```bash
cp .env.example .env
```

Editá `.env` si querés cambiar algún valor (por defecto está listo para correr en local).

### 3. Levantar el sistema completo

```bash
docker compose up --build
```

O con make:

```bash
make up
```

Esto va a:

1. **Descargar las imágenes base** (Postgres, Maven, JRE 21, Node 20) — solo la primera vez
2. **Construir las imágenes** del frontend y el backend
3. **Crear el volumen** `habitpet_postgres_data` para persistir la base de datos
4. **Arrancar Postgres** y esperar a que pase su healthcheck
5. **Arrancar el backend**, ejecutar las migraciones de Flyway (schema + seed data)
6. **Arrancar el frontend** con Vite

El primer arranque puede tardar **3 a 5 minutos** por la descarga y compilación. Los siguientes son cuestión de segundos.

### 4. Validar que todo funciona

- **App web**: http://localhost:5173
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health check**: http://localhost:8080/actuator/health

Iniciá sesión con uno de los [usuarios de prueba](#usuarios-de-prueba).

---

## Encender el sistema después de la primera vez

```bash
docker compose up
# o
make up
```

> Sin el `--build` no se reconstruyen las imágenes — solo necesario si cambiaste `Dockerfile`, `pom.xml` o `package.json`.

Para apagarlo (conservando datos):

```bash
docker compose down
```

---

## Comandos útiles

| Comando | Qué hace |
|---|---|
| `make up` | Arranca el sistema (construye imágenes si es necesario) |
| `make up-detached` | Arranca en segundo plano |
| `make dev` | Modo desarrollo con hot reload del backend |
| `make down` | Detiene los contenedores. **Conserva los datos.** |
| `make logs` | Muestra los logs en vivo de los 3 servicios |
| `make status` | Estado de los contenedores |
| `make build` | Solo construye las imágenes sin arrancar |
| `make clean` | Detiene contenedores **y borra el volumen** |
| `make reset` | `clean` + `up` — arranca limpio desde cero |
| `make seed` | Borra el volumen y vuelve a generar datos de prueba |

Equivalentes sin `make`:

```bash
docker compose up --build              # = make up
docker compose up --build -d           # = make up-detached
docker compose down                    # = make down
docker compose logs -f                 # = make logs
docker compose ps                      # = make status
docker compose build                   # = make build
docker compose down -v                 # = make clean
```

---

## Modo desarrollo con hot reload

Para desarrollo activo del backend sin rebuildar la imagen Docker en cada cambio:

```bash
make dev
```

Esto levanta el backend con `spring-boot:run` (fuente montada como volumen) y Spring Boot DevTools activo. Cuando el IDE compila al guardar, DevTools detecta los `.class` nuevos y reinicia el contexto de Spring automáticamente.

> El frontend **siempre** tiene hot reload de Vite — cualquier cambio en los `.tsx` se refleja instantáneamente en el browser.

---

## Usuarios de prueba

Los datos de prueba se cargan automáticamente vía Flyway al primer arranque:

| Email | Password | Patrón | Estado de mascota |
|---|---|---|---|
| `demo@habitpet.com` | `demo1234` | Cumple consistentemente | EXCELLENT |
| `maria@habitpet.com` | `demo1234` | Cumple irregularmente | NEUTRAL |
| `lucas@habitpet.com` | `demo1234` | Cumple poco | POOR |

Cada usuario tiene 3–5 hábitos con 14 días de historial de check-ins.

> Para regenerar datos frescos: `make seed` (borra volumen y reinicia).

---

## URLs del sistema

| Servicio | URL | Puerto |
|---|---|---|
| Frontend (app web) | http://localhost:5173 | 5173 |
| Backend (API REST) | http://localhost:8080 | 8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html | 8080 |
| Health check | http://localhost:8080/actuator/health | 8080 |
| WebSocket endpoint | http://localhost:8080/ws | 8080 |
| PostgreSQL | localhost:5432 (user/pass: `habitpet`) | 5432 |

> Para conectarte a Postgres desde un cliente externo:
> ```bash
> psql -h localhost -p 5432 -U habitpet -d habitpet
> # password: habitpet
> ```

---

## Estructura del proyecto

```
asistente-develop/
├── frontend/                    React 18 + Vite + TypeScript
│   ├── src/
│   │   ├── pages/auth/          LoginPage
│   │   ├── pages/dashboard/     DashboardPage (mascota + WebSocket)
│   │   ├── store/               Zustand (authStore)
│   │   └── services/api/        REST client (fetch)
│   ├── vite.config.ts
│   └── Dockerfile
│
├── backend/                     Spring Boot 3.3 + Java 21
│   ├── src/main/java/com/habitpet/
│   │   ├── models/              Entidades JPA + enums + value objects
│   │   ├── repositories/        Spring Data JPA interfaces
│   │   ├── services/            AuthService, HabitService, RecordService,
│   │   │                        WellnessService, WellnessCalculator,
│   │   │                        ExponentialWeightedWellnessCalculator, PetService
│   │   ├── controllers/         AuthController, HabitController,
│   │   │                        RecordController, PetController
│   │   ├── dtos/                DTOs de request/response
│   │   ├── exceptions/          AppException + jerarquía (OCP)
│   │   ├── configs/             SecurityConfig, JwtAuthFilter, WebSocketConfig
│   │   ├── websockets/          PetWebSocketGateway
│   │   ├── events/              CheckInCompletedEvent
│   │   └── bases/seed/          DatabaseSeeder
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-docker.yml
│   │   └── db/migration/        V1__schema, V2__seed, V3__fix_passwords
│   ├── src/test/                46 tests unitarios
│   ├── Dockerfile               Producción (multi-stage Maven → JRE)
│   └── Dockerfile.dev           Desarrollo (spring-boot:run + hot reload)
│
├── docs/                        Documentación técnica completa
├── docker-compose.yml           Orquestación de los 3 servicios
├── docker-compose.dev.yml       Override para modo desarrollo
├── Makefile                     Atajos de comandos
├── .env.example                 Plantilla de variables de entorno
└── CLAUDE.md                    Contexto para agentes IA
```

---

## Persistencia de datos

La base de datos vive en un volumen Docker nombrado: `habitpet_postgres_data`.

| Comando | Efecto |
|---|---|
| `docker compose down` | Detiene contenedores. **Los datos se conservan.** |
| `docker compose up` | Los datos siguen ahí. Flyway detecta migraciones ya aplicadas. |
| `docker compose down -v` / `make clean` | Borra el volumen. Próximo arranque empieza de cero. |

> Para inspeccionar datos:
> ```bash
> docker exec -it habitpet-postgres psql -U habitpet -d habitpet
> ```

---

## Documentación

| Documento | Contenido |
|---|---|
| [docs/RFC.md](./docs/RFC.md) | Propuesta técnica formal, alcance del MVP |
| [docs/PLANIFICACION.md](./docs/PLANIFICACION.md) | Backlog priorizado (MoSCoW), Gantt |
| [docs/ARQUITECTURA.md](./docs/ARQUITECTURA.md) | Diagramas C4, capas, flujos |
| [docs/MODELO_DOMINIO.md](./docs/MODELO_DOMINIO.md) | Entidades JPA, Flyway, reglas de negocio |
| [docs/SOLID_Y_GRASP.md](./docs/SOLID_Y_GRASP.md) | Principios SOLID y GRASP con código |
| [docs/PATRONES_DISENO.md](./docs/PATRONES_DISENO.md) | Strategy, Observer, Facade, Repository |
| [docs/ADR/](./docs/ADR/) | Architecture Decision Records |
| [docs/diagramas.html](./docs/diagramas.html) | Visor interactivo de arquitectura |

---

## Resolución de problemas

### El backend no arranca — error de conexión a Postgres

```bash
docker compose ps   # verificar que habitpet-postgres está healthy
docker compose logs postgres
```

### El puerto 5432, 8080 o 5173 ya está en uso

Apagá el servicio que lo ocupa o cambiá el puerto en `docker-compose.yml` (ej: `"5433:5432"`).

### Cambié código del backend y no veo los cambios

Con `make up` (imagen compilada): reconstruir con `docker compose up --build backend`.

Con `make dev` (hot reload): el IDE debe compilar al guardar para que DevTools detecte el cambio.

### Quiero empezar de cero

```bash
make reset
# equivalente a:
docker compose down -v && docker compose up --build
```

### Los datos de prueba no se generaron

Verificar que `.env` tenga `APP_SEED_ENABLED=true`. Si la DB ya tiene usuarios, Flyway detecta que V2 ya fue aplicado y no repite. Usar `make seed` para forzar reset.

---

## Licencia

Proyecto académico — UADE 2026. No licenciado para uso comercial.
