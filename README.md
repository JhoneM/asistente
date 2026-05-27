# HabitPet

> Sistema web tipo Tamagotchi vinculado a hábitos saludables. La mascota virtual evoluciona o se deteriora según el cumplimiento de los hábitos del usuario.

Proyecto universitario — UADE.

---

## Tabla de contenidos

1. [Sobre el proyecto](#sobre-el-proyecto)
2. [Cómo funciona](#cómo-funciona)
3. [Stack técnico](#stack-técnico)
4. [Arquitectura](#arquitectura)
5. [Requisitos previos](#requisitos-previos)
6. [Primera vez (arranque inicial)](#primera-vez-arranque-inicial)
7. [Encender el sistema después de la primera vez](#encender-el-sistema-después-de-la-primera-vez)
8. [Comandos útiles](#comandos-útiles)
9. [Usuarios de prueba](#usuarios-de-prueba)
10. [URLs del sistema](#urls-del-sistema)
11. [Estructura del proyecto](#estructura-del-proyecto)
12. [Persistencia de datos](#persistencia-de-datos)
13. [Documentación](#documentación)
14. [Resolución de problemas](#resolución-de-problemas)

---

## Sobre el proyecto

HabitPet es una aplicación web que combina la mecánica de mascotas virtuales (estilo Tamagotchi/Pou) con sistemas de seguimiento de hábitos saludables. El usuario configura los hábitos que quiere incorporar a su vida (estudiar, hacer ejercicio, tomar agua, leer, meditar, etc.) y a medida que los cumple, su mascota virtual evoluciona, mejora su apariencia y sube de nivel. Si los hábitos se descuidan, la mascota se deteriora visualmente.

**Hipótesis**: vincular el progreso de hábitos a una entidad virtual con la que se desarrolla un vínculo emocional aumenta la retención y la consistencia frente a las apps tradicionales de tracking con rachas y porcentajes abstractos.

Para detalles formales del proyecto, ver [docs/RFC.md](./docs/RFC.md).

---

## Cómo funciona

1. **Registro y login** — El usuario se registra con email y contraseña.
2. **Configuración de hábitos** — Crea hábitos personalizados con categoría (Salud, Estudio, Deporte, Bienestar, Alimentación) y frecuencia semanal (1 a 7 días).
3. **Check-in diario** — Marca como cumplido cada hábito que realizó en el día. Una sola vez por día por hábito.
4. **Reacción de la mascota** — Tras cada check-in, el backend recalcula el bienestar de la mascota (promedio ponderado exponencial de los últimos 7 días) y le envía al frontend el nuevo estado vía WebSocket. La mascota cambia de animación y mensaje en menos de 500 ms.
5. **Estados de mascota** — Cinco niveles visuales según el % de cumplimiento:
   - **CRITICAL** (0–20%) — La mascota está en peligro
   - **POOR** (21–40%) — La mascota no está bien
   - **NEUTRAL** (41–60%) — La mascota está estable
   - **GOOD** (61–80%) — La mascota está contenta
   - **EXCELLENT** (81–100%) — La mascota está radiante
6. **Sistema de niveles** — La mascota acumula XP por cada hábito completado. Tiene 10 niveles con umbrales progresivos.
7. **Estadísticas** — Dashboard con racha de días consecutivos, % semanal por hábito e historial de los últimos 7 días.

---

## Stack técnico

| Capa | Tecnología |
|---|---|
| Frontend | React 18 + TypeScript + Vite + Zustand + TailwindCSS |
| Backend | Spring Boot 3 + Java 21 + Spring Data JPA + Spring Security + Spring WebSocket |
| Base de datos | PostgreSQL 16 |
| Tiempo real | STOMP sobre WebSocket (con fallback SockJS) |
| Migraciones | Flyway |
| Autenticación | JWT (access + refresh token) con Spring Security |
| Build | Maven (backend), npm + Vite (frontend) |
| Contenedores | Docker + Docker Compose |
| Documentación API | SpringDoc OpenAPI (Swagger UI) |

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

- El **frontend** habla con el backend vía REST (`axios`) y WebSocket (`@stomp/stompjs`).
- El **backend** sigue una arquitectura de capas (Layered) con paquetes organizados por dominio (DDD): `auth`, `habit`, `record`, `pet`, `stats`, `notification`, `websocket`, `shared`.
- **Postgres** persiste los datos en un volumen Docker (`habitpet_postgres_data`) que sobrevive a los reinicios.

Para diagramas C4 (Contexto, Contenedores, Componentes), modelo de dominio y patrones aplicados, ver [docs/ARQUITECTURA.md](./docs/ARQUITECTURA.md) y abrir [docs/diagramas.html](./docs/diagramas.html) en el navegador.

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

La primera vez que levantás el proyecto:

### 1. Clonar el repositorio

```bash
git clone <url-del-repo>
cd asistente-virtual
```

### 2. Crear el archivo de variables de entorno

```bash
cp .env.example .env
```

Editá `.env` si querés cambiar algún valor (por defecto está listo para correr en local).

### 3. Levantar el sistema completo con un solo comando

```bash
docker compose up --build
```

O, si tenés `make` disponible:

```bash
make up
```

Esto va a:

1. **Descargar las imágenes base** (Postgres, Maven, JRE 21, Node 20) — solo la primera vez
2. **Construir las imágenes** del frontend y el backend
3. **Crear el volumen** `habitpet_postgres_data` para persistir la base de datos
4. **Arrancar Postgres** y esperar a que pase su healthcheck
5. **Arrancar el backend**, correr las migraciones de Flyway y poblar la base con datos de prueba (gracias al seeder)
6. **Arrancar el frontend** con Vite en modo dev (hot reload)

El primer arranque puede tardar **3 a 5 minutos** porque tiene que descargar y compilar todo. Los siguientes son cuestión de segundos.

### 4. Validar que todo funciona

Una vez que veas en la consola un mensaje similar a `Started HabitPetApplication in X.X seconds`, podés acceder a:

- **App web**: http://localhost:5173
- **API backend**: http://localhost:8080
- **Swagger UI** (docs API): http://localhost:8080/swagger-ui.html
- **Health check del backend**: http://localhost:8080/actuator/health

Para iniciar sesión, usá uno de los usuarios de prueba listados [más abajo](#usuarios-de-prueba).

---

## Encender el sistema después de la primera vez

Las siguientes veces que quieras trabajar con el proyecto, simplemente ejecutá:

```bash
docker compose up
```

O con make:

```bash
make up
```

> Sin el `--build` no se reconstruyen las imágenes — solo si cambiaste el `Dockerfile`, `pom.xml` o `package.json` necesitás reconstruir.

**El sistema arranca en segundos** y los datos de la base se conservan tal como los dejaste la última vez.

Para apagarlo:

```bash
docker compose down
```

> Este comando detiene los contenedores **pero conserva los datos** en el volumen. La próxima vez que arranques, todo va a estar como lo dejaste.

---

## Comandos útiles

El proyecto incluye un `Makefile` con atajos para las operaciones más comunes:

| Comando | Qué hace |
|---|---|
| `make up` | Arranca el sistema (construye imágenes si es necesario) |
| `make up-detached` | Arranca en segundo plano (sin atachar logs) |
| `make down` | Detiene los contenedores. **Conserva los datos.** |
| `make logs` | Muestra los logs en vivo de los 3 servicios |
| `make status` | Muestra el estado de los contenedores |
| `make build` | Solo construye las imágenes sin arrancar |
| `make clean` | Detiene contenedores **y borra el volumen** (limpia la base) |
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

## Usuarios de prueba

La primera vez que arranca el sistema, el **seeder** puebla la base de datos con tres usuarios de demo, cada uno con un patrón distinto de cumplimiento para mostrar todos los estados de mascota:

| Email | Password | Patrón de uso | Estado esperado de mascota |
|---|---|---|---|
| `demo@habitpet.com` | `demo1234` | Cumple consistentemente | EXCELLENT |
| `maria@habitpet.com` | `demo1234` | Cumple irregularmente | NEUTRAL |
| `lucas@habitpet.com` | `demo1234` | Cumple poco | POOR / CRITICAL |

Cada usuario tiene 3–5 hábitos precargados con 14 días de historial de check-ins simulados.

> Para regenerar estos datos en cualquier momento: `make seed` (borra todo y reinicia con datos frescos).

---

## URLs del sistema

| Servicio | URL local | Puerto |
|---|---|---|
| Frontend (app web) | http://localhost:5173 | 5173 |
| Backend (API REST) | http://localhost:8080 | 8080 |
| Swagger UI (docs API) | http://localhost:8080/swagger-ui.html | 8080 |
| Backend healthcheck | http://localhost:8080/actuator/health | 8080 |
| WebSocket endpoint | ws://localhost:8080/ws | 8080 |
| PostgreSQL | localhost:5432 (user: `habitpet`, pass: `habitpet`, db: `habitpet`) | 5432 |

> Para conectarte a Postgres desde un cliente externo (DBeaver, TablePlus, psql):
> ```bash
> psql -h localhost -p 5432 -U habitpet -d habitpet
> # password: habitpet
> ```

---

## Estructura del proyecto

```
asistente-virtual/
├── frontend/                    React + Vite + TypeScript
│   ├── public/
│   ├── src/
│   │   ├── components/         Componentes reutilizables
│   │   ├── pages/              Pantallas (auth, dashboard, habits, stats)
│   │   ├── hooks/              Custom hooks (useAuth, usePet, useWebSocket)
│   │   ├── store/              Zustand stores
│   │   ├── services/           Cliente REST + WebSocket
│   │   └── ...
│   └── Dockerfile
│
├── backend/                     Spring Boot 3 + Java 21
│   ├── src/main/java/com/habitpet/
│   │   ├── HabitPetApplication.java
│   │   ├── shared/             Cross-cutting: exception, config, seed
│   │   ├── auth/               Bounded context: autenticación
│   │   ├── habit/              Bounded context: hábitos
│   │   ├── record/             Bounded context: check-ins
│   │   ├── pet/                Bounded context: mascota + Strategy
│   │   ├── stats/              Estadísticas
│   │   ├── notification/       Notificaciones in-app
│   │   └── websocket/          Gateway STOMP
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/       Scripts Flyway
│   └── Dockerfile
│
├── docker/postgres/             Scripts de inicialización opcionales
├── docs/                        Documentación técnica y arquitectura
│
├── docker-compose.yml           Orquestación de los 3 servicios
├── Makefile                     Atajos de comandos
├── start.sh                     Script alternativo de arranque
├── .env.example                 Plantilla de variables de entorno
├── CLAUDE.md                    Contexto para agentes IA
└── README.md                    Este archivo
```

Para el detalle completo de carpetas y archivos, ver [docs/SCAFFOLDING_PLAN.md](./docs/SCAFFOLDING_PLAN.md).

---

## Persistencia de datos

La base de datos vive en un volumen Docker nombrado: `habitpet_postgres_data`.

| Comando | Efecto sobre los datos |
|---|---|
| `docker compose down` | Detiene los contenedores. **Los datos se conservan.** |
| `docker compose up` siguientes | Los datos siguen ahí. El seeder detecta que la DB ya tiene contenido y **no vuelve a poblar**. |
| `docker compose down -v` (o `make clean`) | Detiene contenedores **y borra el volumen**. Próximo arranque empieza de cero. |
| `docker volume ls` | Lista volúmenes. Vas a ver `habitpet_postgres_data`. |
| `docker volume rm habitpet_postgres_data` | Borra el volumen manualmente. |

> Para inspeccionar los datos sin abrir un cliente SQL:
> ```bash
> docker exec -it habitpet-postgres psql -U habitpet -d habitpet
> ```

---

## Documentación

Toda la documentación técnica vive en `docs/`:

| Documento | Contenido |
|---|---|
| [docs/RFC.md](./docs/RFC.md) | Propuesta técnica formal, alcance del MVP, historias de usuario |
| [docs/PLANIFICACION.md](./docs/PLANIFICACION.md) | Backlog priorizado (MoSCoW), fases de desarrollo, Gantt |
| [docs/ARQUITECTURA.md](./docs/ARQUITECTURA.md) | Diagramas C4, capas, flujos de datos, seguridad |
| [docs/MODELO_DOMINIO.md](./docs/MODELO_DOMINIO.md) | Entidades JPA, scripts Flyway, reglas de negocio |
| [docs/SOLID_Y_GRASP.md](./docs/SOLID_Y_GRASP.md) | Principios SOLID y patrones GRASP con código Java |
| [docs/PATRONES_DISENO.md](./docs/PATRONES_DISENO.md) | Patrones GoF aplicados (Strategy, Observer, Repository, etc.) |
| [docs/ADR/](./docs/ADR/) | Architecture Decision Records (decisiones justificadas) |
| [docs/diagramas.html](./docs/diagramas.html) | Visor interactivo de la arquitectura (abrir en navegador) |
| [docs/SCAFFOLDING_PLAN.md](./docs/SCAFFOLDING_PLAN.md) | Plan detallado del scaffolding inicial |
| [CLAUDE.md](./CLAUDE.md) | Contexto del proyecto para agentes IA (Claude Code) |

---

## Resolución de problemas

### El backend no arranca y los logs muestran error de conexión a Postgres

Verificá que Postgres pasó su healthcheck:
```bash
docker compose ps
```
Si `habitpet-postgres` no aparece como `healthy`, dale unos segundos más o revisá los logs:
```bash
docker compose logs postgres
```

### El puerto 5432, 8080 o 5173 ya está en uso

Tenés otro servicio corriendo en ese puerto. Opciones:
- Apagá el otro servicio
- O cambiá el puerto en `docker-compose.yml` (ej: `"5433:5432"` para usar 5433 en tu máquina)

### Cambié código del backend y no veo los cambios

El backend NO tiene hot reload por defecto. Tenés que reconstruir:
```bash
docker compose up --build backend
```

### El frontend no toma los cambios

El frontend SÍ tiene hot reload de Vite. Si no funciona, revisá que el volumen del código esté montado correctamente (definido en `docker-compose.dev.yml`).

### Quiero empezar de cero (reset completo)

```bash
make reset
```
O sin make:
```bash
docker compose down -v
docker compose up --build
```

### Los datos de prueba no se generaron

Verificá que en `.env` esté `APP_SEED_ENABLED=true` y que sea la primera vez que arranca (o que hayas hecho `make clean`/`make reset` antes). Si la DB ya tiene usuarios, el seeder no corre por diseño (es idempotente).

### Quiero ver las queries SQL que ejecuta el backend

Cambiá en `backend/src/main/resources/application-dev.yml`:
```yaml
spring:
  jpa:
    show-sql: true
```
Y arrancá con perfil dev: `SPRING_PROFILES_ACTIVE=dev docker compose up`.

---

## Licencia

Proyecto académico — UADE 2026. No licenciado para uso comercial.
