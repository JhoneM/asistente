# Plan de Scaffolding Inicial — HabitPet

Este documento describe la estructura de carpetas y archivos de configuración mínimos que el agente de implementación debe crear para levantar el scaffolding del proyecto. **Solo carpetas y archivos de configuración** — sin lógica de negocio.

---

## 1. Arquitectura de Contenedores

3 contenedores comunicados mediante una Docker network privada (`habitpet-network`). Para este contexto universitario **no se incluye nginx/reverse proxy ni endurecimiento de seguridad**: los contenedores se llaman entre sí por nombre de servicio Docker, y CORS está permisivo para desarrollo.

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  frontend   │───►│  backend    │───►│  postgres   │
│ React+Vite  │    │ Spring Boot │    │  v16-alpine │
│ :5173       │    │ :8080       │    │ :5432       │
└─────────────┘    └─────────────┘    └─────────────┘
       └──────── WebSocket (STOMP) ──────┘
```

---

## 2. Estructura de Carpetas

### 2.1. Raíz del proyecto

```
asistente-virtual/
├── frontend/                React + Vite + TypeScript
├── backend/                 Spring Boot 3 + Java 21
├── docker/
│   └── postgres/
│       └── init.sql         Init opcional (Flyway gestiona el schema)
├── docs/                    Ya existe con toda la documentación
├── docker-compose.yml       Orquestación de los 3 servicios
├── docker-compose.dev.yml   Overrides para desarrollo (hot reload)
├── Makefile                 Atajos: make up, make down, make clean, make seed
├── start.sh                 Script de arranque único (alternativa al Makefile)
├── .env.example             Plantilla de variables de entorno
├── .gitignore
├── CLAUDE.md                Contexto para futuros agentes IA
└── README.md                Guía de setup y arranque rápido
```

### 2.2. Frontend (`frontend/`)

```
frontend/
├── public/
│   └── assets/pet/          SVGs por estado de mascota (placeholder)
├── src/
│   ├── assets/              Recursos importados en componentes
│   ├── components/
│   │   ├── common/          Button, Input, Card, Modal, Toast
│   │   ├── layout/          AppShell, Header, Sidebar
│   │   ├── pet/             PetAvatar, PetStats, PetAnimations
│   │   └── habits/          HabitCard, HabitForm, CheckInButton
│   ├── pages/
│   │   ├── auth/            LoginPage, RegisterPage
│   │   ├── dashboard/       DashboardPage
│   │   ├── habits/          HabitsPage
│   │   └── stats/           StatsPage
│   ├── hooks/               useAuth, usePet, useWebSocket
│   ├── store/               Zustand stores (auth, pet, habit)
│   ├── services/
│   │   ├── api/             Clientes REST (axios)
│   │   └── websocket/       Cliente STOMP/SockJS
│   ├── types/               TypeScript types/DTOs
│   ├── utils/               Helpers (fechas, formato)
│   ├── styles/              global.css + Tailwind
│   ├── routes/              Router config
│   ├── App.tsx
│   └── main.tsx
├── tests/                   Vitest
├── .env.example
├── .gitignore
├── Dockerfile
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── postcss.config.js
└── README.md
```

### 2.3. Backend (`backend/`)

Paquetes organizados por dominio según ADR-004 (Layered Architecture + DDD packages).

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/habitpet/
│   │   │   ├── HabitPetApplication.java         Clase main de Spring Boot
│   │   │   ├── shared/                          Cross-cutting
│   │   │   │   ├── config/                      CORS, beans globales
│   │   │   │   ├── exception/                   GlobalExceptionHandler, base exceptions
│   │   │   │   ├── seed/                        DatabaseSeeder (CommandLineRunner)
│   │   │   │   └── util/
│   │   │   ├── auth/                            Bounded context: autenticación
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── domain/                      User entity
│   │   │   │   ├── repository/
│   │   │   │   ├── security/                    JwtFilter, SecurityConfig, JwtService
│   │   │   │   └── dto/
│   │   │   ├── habit/                           Bounded context: hábitos
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── domain/                      Habit, HabitCategory enum
│   │   │   │   ├── repository/
│   │   │   │   └── dto/
│   │   │   ├── record/                          Bounded context: check-ins
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── domain/                      CompletionRecord
│   │   │   │   ├── repository/
│   │   │   │   ├── event/                       RecordCompletedEvent
│   │   │   │   └── dto/
│   │   │   ├── pet/                             Bounded context: mascota
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── domain/                      Pet, PetState, WellnessScore (Java record)
│   │   │   │   ├── strategy/                    WellnessCalculator interface + impl
│   │   │   │   ├── repository/
│   │   │   │   ├── event/                       PetStateChangedEvent
│   │   │   │   └── dto/
│   │   │   ├── stats/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   └── dto/
│   │   │   ├── notification/
│   │   │   │   ├── service/
│   │   │   │   └── domain/
│   │   │   └── websocket/
│   │   │       ├── config/                      WebSocketConfig (STOMP broker)
│   │   │       └── gateway/                     PetWebSocketGateway
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-docker.yml
│   │       └── db/migration/                    Scripts Flyway (vacío al inicio)
│   └── test/
│       └── java/com/habitpet/                   Tests espejando estructura main
├── .gitignore
├── .mvn/wrapper/                                Maven wrapper
├── mvnw
├── mvnw.cmd
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 3. Archivos de Configuración Mínimos

### 3.1. `docker-compose.yml`

3 servicios + 1 network + 1 volume nombrado para persistencia de Postgres. Incluye **healthcheck en Postgres** y `depends_on` con condición de salud para que el backend espere a que la DB esté lista antes de arrancar.

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: habitpet-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-habitpet}
      POSTGRES_USER: ${POSTGRES_USER:-habitpet}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-habitpet}
    ports:
      - "5432:5432"
    volumes:
      - habitpet_postgres_data:/var/lib/postgresql/data   # Volumen persistente
      - ./docker/postgres/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-habitpet}"]
      interval: 5s
      timeout: 5s
      retries: 10
    networks:
      - habitpet-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: habitpet-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-habitpet}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-habitpet}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-habitpet}
      JWT_SECRET: ${JWT_SECRET:-dev-secret-change-in-prod}
      APP_SEED_ENABLED: ${APP_SEED_ENABLED:-true}   # Controla si el seeder corre
    depends_on:
      postgres:
        condition: service_healthy   # Espera a que Postgres esté listo
    networks:
      - habitpet-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: habitpet-frontend
    restart: unless-stopped
    ports:
      - "5173:5173"
    environment:
      VITE_API_URL: http://localhost:8080
      VITE_WS_URL: ws://localhost:8080/ws
    depends_on:
      - backend
    networks:
      - habitpet-network

volumes:
  habitpet_postgres_data:
    name: habitpet_postgres_data   # Nombre explícito para fácil identificación

networks:
  habitpet-network:
    driver: bridge
```

#### Persistencia de datos

El volumen `habitpet_postgres_data` vive **fuera del ciclo de vida de los contenedores**. Comportamiento:

| Comando | Efecto sobre los datos |
|---|---|
| `docker compose down` | Detiene los contenedores. **Los datos se conservan.** |
| `docker compose up` (siguientes veces) | Los datos siguen ahí. El seeder detecta que ya hay registros y **no vuelve a poblar**. |
| `docker compose down -v` | Detiene contenedores **y borra el volumen**. Próximo `up` arranca de cero y el seeder vuelve a correr. |
| `docker volume ls` | Lista volúmenes. Verás `habitpet_postgres_data`. |
| `docker volume rm habitpet_postgres_data` | Borra el volumen manualmente. |

### 3.2. `backend/Dockerfile`

Multi-stage build: imagen de Maven para compilar, imagen JRE 21 para ejecutar.

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.3. `frontend/Dockerfile`

Node 20-alpine con hot reload de Vite.

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
```

### 3.4. `backend/pom.xml`

Dependencias mínimas:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-websocket`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `org.postgresql:postgresql`
- `org.flywaydb:flyway-core`
- `io.jsonwebtoken:jjwt-api` (+ impl + jackson)
- `org.projectlombok:lombok`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- Test: `spring-boot-starter-test`, `spring-security-test`

Java 21, Spring Boot 3.3+.

### 3.5. `frontend/package.json`

Dependencias mínimas:
- `react`, `react-dom`, `react-router-dom`
- `zustand` (estado global)
- `axios` (HTTP client)
- `@stomp/stompjs`, `sockjs-client` (WebSocket)
- `tailwindcss`, `postcss`, `autoprefixer`
- DevDeps: `vite`, `@vitejs/plugin-react`, `typescript`, `@types/react`, `@types/react-dom`, `@types/sockjs-client`, `eslint`, `prettier`, `vitest`

### 3.6. `backend/src/main/resources/application.yml`

Configuración base (perfil default):

```yaml
spring:
  application:
    name: habitpet
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### 3.7. `backend/src/main/resources/application-docker.yml`

Override para perfil Docker (la URL apunta al servicio Docker `postgres`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/habitpet
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

### 3.8. `.env.example` (raíz)

```env
# Postgres
POSTGRES_DB=habitpet
POSTGRES_USER=habitpet
POSTGRES_PASSWORD=habitpet

# Backend
SPRING_PROFILES_ACTIVE=docker
JWT_SECRET=dev-secret-change-in-prod
APP_SEED_ENABLED=true             # true = pobla la DB la primera vez (ver sección 5)

# Frontend
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

### 3.9. `frontend/vite.config.ts`

Configurado para escuchar en `0.0.0.0` (necesario en Docker) y con proxy al backend:

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': 'http://backend:8080',
      '/ws': { target: 'http://backend:8080', ws: true }
    }
  }
});
```

---

## 4. Arranque del Sistema con un Solo Comando

El sistema completo se levanta con **un único comando**. No se enciende un servicio por vez.

### 4.1. Comando directo de Docker Compose

```bash
docker compose up --build
```

Este comando:
1. Construye las imágenes de `frontend` y `backend` si no existen o cambiaron
2. Arranca `postgres` primero
3. Espera al healthcheck de Postgres antes de arrancar `backend`
4. Arranca `frontend` una vez que el backend esté disponible
5. Atachea los logs de los 3 servicios a la terminal

Para correr en segundo plano:
```bash
docker compose up --build -d
```

### 4.2. Atajos con `Makefile` (recomendado)

`Makefile` en la raíz del proyecto:

```makefile
.PHONY: up down logs clean reset seed status build

up:           ## Levanta todo el sistema
	docker compose up --build

up-detached:  ## Levanta todo en segundo plano
	docker compose up --build -d

down:         ## Detiene los contenedores (conserva los datos)
	docker compose down

clean:        ## Detiene contenedores Y borra el volumen de Postgres
	docker compose down -v

reset: clean up   ## Limpia todo y vuelve a levantar de cero

logs:         ## Muestra logs en vivo de los 3 servicios
	docker compose logs -f

status:       ## Estado de los contenedores
	docker compose ps

seed:         ## Fuerza re-poblado (borra DB y la rearma con seed data)
	docker compose down -v
	APP_SEED_ENABLED=true docker compose up --build

build:        ## Solo construye imágenes sin arrancar
	docker compose build
```

Uso:
```bash
make up         # Arrancar todo
make down       # Apagar sin perder datos
make reset      # Apagar, borrar volumen y arrancar limpio
make seed       # Re-generar datos de prueba
```

### 4.3. Alternativa con `start.sh`

Para quien no tenga `make` disponible:

```bash
#!/bin/bash
# start.sh — Arranca el sistema completo
set -e

if [ ! -f .env ]; then
  echo "Copiando .env.example a .env (editá si necesitás cambios)..."
  cp .env.example .env
fi

docker compose up --build
```

Hacerlo ejecutable: `chmod +x start.sh` y correrlo con `./start.sh`.

---

## 5. Poblado Inicial de la Base de Datos (Seeder)

### 5.1. Estrategia

Se usa un **`CommandLineRunner` de Spring Boot** que corre al arrancar el backend, con lógica idempotente: solo pobla si la DB está vacía.

**Ubicación**: `backend/src/main/java/com/habitpet/shared/seed/DatabaseSeeder.java`

**Activación**: solo si la variable `APP_SEED_ENABLED=true` (definida en `.env`) y solo si no hay usuarios previos en la DB.

### 5.2. Comportamiento

| Escenario | Comportamiento del seeder |
|---|---|
| Primera vez que se arranca (volumen vacío) | Detecta DB vacía → genera datos de prueba |
| Segundo arranque (sin borrar volumen) | Detecta usuarios existentes → no hace nada, log "seed skipped" |
| `make reset` o `make seed` | Borra volumen → próximo arranque pobla de nuevo |
| `APP_SEED_ENABLED=false` | No corre el seeder aunque la DB esté vacía |

### 5.3. Estructura del seeder

```java
// backend/src/main/java/com/habitpet/shared/seed/DatabaseSeeder.java

@Component
@Profile({"dev", "docker"})  // Nunca corre en perfil "prod"
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final CompletionRecordRepository recordRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Base de datos ya poblada, saltando seeder.");
            return;
        }
        log.info("Poblando base de datos con datos de prueba...");
        seedUsers();
        seedHabitsAndPets();
        seedCompletionRecords();
        log.info("Seeder completado.");
    }

    private void seedUsers() { /* ... */ }
    private void seedHabitsAndPets() { /* ... */ }
    private void seedCompletionRecords() { /* ... */ }
}
```

### 5.4. Datos de prueba a generar

| Entidad | Cantidad | Detalle |
|---|---|---|
| Users | 3 | `demo@habitpet.com`, `maria@habitpet.com`, `lucas@habitpet.com` — password "demo1234" |
| Habits (por usuario) | 3–5 | Mix de categorías (HEALTH, STUDY, SPORT, WELLNESS, NUTRITION), frecuencias variadas |
| CompletionRecords | últimos 14 días | Patrones distintos por usuario: uno con buen cumplimiento (mascota EXCELLENT), otro irregular (NEUTRAL), otro descuidado (POOR) |
| Pets | 1 por usuario | Estado y XP calculados según los registros sembrados |

Esto permite que al arrancar el sistema se pueda:
- Hacer login con `demo@habitpet.com` y ver una mascota ya con historial
- Probar el cálculo de bienestar sin tener que hacer check-ins manuales
- Demostrar el sistema completo en una defensa sin armar datos en vivo

### 5.5. Configuración en `application.yml`

```yaml
app:
  seed:
    enabled: ${APP_SEED_ENABLED:false}   # default false; se activa por env var
```

En el `docker-compose.yml` ya está seteado `APP_SEED_ENABLED=true` por defecto para que la primera vez se pueble.

---

## 6. Archivos de Código Mínimos (Solo Esqueleto)

Para que `docker compose up --build` arranque sin errores, se crean:

### Backend
- `HabitPetApplication.java` con la clase principal `@SpringBootApplication` vacía
- `DatabaseSeeder.java` con el esqueleto (puede dejar los métodos privados con `// TODO`)
- Ningún otro archivo `.java` con lógica de negocio

### Frontend
- `main.tsx` que monta `App` en `#root`
- `App.tsx` mínimo con un `<div>HabitPet</div>` o similar
- `index.html` con `<div id="root"></div>`

**No se crea** ningún controller, service, repository, entidad, componente React, store, etc. Esos archivos se crearán al implementar cada historia del backlog.

---

## 7. `CLAUDE.md` — Contexto para Futuros Agentes

```markdown
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
- docs/SCAFFOLDING_PLAN.md — Este documento

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
```

---

## 8. Orden de Implementación para el Agente

1. **Estructura de carpetas vacías** — Crear toda la jerarquía con archivos `.gitkeep` donde corresponda (frontend/, backend/, docker/, paquetes Java incluido `shared/seed/`).
2. **Archivos raíz** — `docker-compose.yml`, `docker-compose.dev.yml`, `.env.example`, `.gitignore`, `Makefile`, `start.sh`, `README.md`, `CLAUDE.md`.
3. **Backend scaffolding** — `pom.xml`, `mvnw` + wrapper, `HabitPetApplication.java`, `DatabaseSeeder.java` (esqueleto), archivos `application*.yml`, `Dockerfile`.
4. **Frontend scaffolding** — `package.json`, `vite.config.ts`, `tsconfig.json`, `tailwind.config.js`, `index.html`, `main.tsx`, `App.tsx` mínimo, `Dockerfile`.
5. **Validación de arranque** — Ejecutar `docker compose up --build` (o `make up`) y confirmar que:
   - El volumen `habitpet_postgres_data` se crea
   - Postgres pasa el healthcheck antes de que arranque el backend
   - Backend arranca, se conecta a Postgres y expone `/actuator/health` en `:8080`
   - Frontend sirve la página en `:5173`
   - Swagger UI accesible en `http://localhost:8080/swagger-ui.html`
6. **Validación de persistencia** —
   - `docker compose down` y luego `docker compose up` → los datos siguen ahí
   - `docker compose down -v` y luego `docker compose up` → el volumen se recrea desde cero
7. **Validación del seeder** (cuando ya existan las entidades en una fase posterior) — Primera vez: pobla. Segunda vez: skip.

---

## 9. Criterio de "Done" del Scaffolding

El scaffolding está completo cuando:

- [ ] Toda la estructura de carpetas definida en este documento existe
- [ ] **Un solo comando** (`docker compose up --build` o `make up`) levanta los 3 contenedores sin errores
- [ ] Backend responde 200 OK en `/actuator/health`
- [ ] Frontend muestra la página inicial en el navegador
- [ ] Backend conecta exitosamente a Postgres (verificable en logs)
- [ ] **El volumen `habitpet_postgres_data` existe** y persiste datos entre reinicios
- [ ] `docker compose down` + `up` conserva los datos
- [ ] `docker compose down -v` + `up` los borra y reinicia limpio
- [ ] La clase `DatabaseSeeder` existe como esqueleto, condicionada a `app.seed.enabled=true`
- [ ] `Makefile` con targets `up`, `down`, `clean`, `reset`, `seed`, `logs`, `status`
- [ ] `CLAUDE.md` está creado con el contexto del proyecto
- [ ] `README.md` en la raíz explica cómo levantar el proyecto

**Sin lógica de negocio implementada.** El seeder queda como esqueleto vacío con TODOs — su implementación real ocurrirá cuando existan las entidades JPA del dominio, en las fases siguientes del plan en `docs/PLANIFICACION.md`.
