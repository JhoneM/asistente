# RFC: HabitPet — Sistema de Mascota Virtual Orientada a Hábitos

## Metadatos

| Campo     | Valor                                              |
|-----------|----------------------------------------------------|
| Título    | HabitPet: Sistema de Mascota Virtual de Hábitos   |
| Autores   | Equipo Proyecto Universitario — UADE               |
| Fecha     | 2026-05-07                                         |
| Versión   | 1.0.0                                              |
| Estado    | Propuesta                                          |

---

## 1. Abstract

HabitPet es una aplicación web que combina la mecánica de mascotas virtuales (estilo Tamagotchi/Pou) con sistemas de seguimiento de hábitos positivos. El usuario configura hábitos (estudiar, hacer ejercicio, beber agua, leer, etc.) y a medida que los cumple, su mascota virtual evoluciona: mejora su apariencia, sube de nivel y expresa estados de bienestar crecientes. Si los hábitos se descuidan, la mascota se deteriora visualmente. El objetivo es gamificar la adopción de hábitos saludables mediante un vínculo emocional con una entidad virtual persistente.

---

## 2. Motivación y Problema

### 2.1. Contexto

La adopción y mantenimiento de hábitos saludables es uno de los principales desafíos para jóvenes universitarios. Estudios sobre comportamiento muestran que el 80% de las personas que intentan incorporar un nuevo hábito lo abandonan dentro de las primeras dos semanas.

Las aplicaciones existentes de seguimiento de hábitos (Habitica, Streaks, Habitify, Bereal) presentan tasas de abandono elevadas porque:
- La motivación extrínseca basada en rachas y porcentajes se agota rápidamente
- No generan consecuencias emocionales reales por el incumplimiento
- La experiencia de usuario carece de elementos que generen apego

### 2.2. Problema Central

Las personas abandonan el seguimiento de hábitos cuando la recompensa es abstracta y el sistema no genera consecuencias emocionales por el incumplimiento. Una lista de tareas cumplidas no activa el mismo mecanismo de motivación que cuidar de una entidad que depende del usuario.

### 2.3. Hipótesis

Vincular el progreso de hábitos a una entidad virtual con la que el usuario desarrolla un vínculo emocional (mascota que se deteriora si es descuidada) aumentará la retención y la consistencia en el cumplimiento de hábitos.

---

## 3. Objetivos

### 3.1. Objetivos Funcionales

1. Permitir al usuario crear y configurar hábitos personalizados con frecuencia, categoría y descripción
2. Registrar el cumplimiento diario de tareas asociadas a cada hábito
3. Calcular el estado de la mascota en función del porcentaje de cumplimiento histórico ponderado
4. Mostrar la mascota con diferentes estados visuales (cinco niveles de bienestar)
5. Implementar un sistema de experiencia y niveles para la mascota (diez niveles en MVP)
6. Mostrar estadísticas de progreso del usuario (racha actual, porcentaje semanal, historial)
7. Notificar al usuario in-app cuando la mascota requiere atención
8. Soportar múltiples sesiones simultáneas con actualización en tiempo real vía WebSocket

### 3.2. Objetivos No Funcionales

1. Tiempo de respuesta menor a 200ms para operaciones CRUD en condiciones normales
2. Disponibilidad del 99% durante horario académico (8:00–22:00)
3. Soporte para al menos 50 usuarios concurrentes (contexto universitario)
4. Autenticación segura mediante JWT; objetivo final: access token corto (15 min) y refresh token (7 días)
5. Interfaz responsiva para desktop y dispositivos móviles
6. Cobertura de tests unitarios mayor al 70% en el backend
7. El código debe seguir principios SOLID, patrones GRASP y patrones de diseño GoF justificados

---

## 4. Alcance del MVP

### 4.1. Incluido en el MVP

- Registro y autenticación de usuarios (email + contraseña)
- CRUD de hábitos con categorías predefinidas (Salud, Estudio, Deporte, Bienestar, Alimentación)
- Registro de cumplimiento de tareas diarias (check-in diario por hábito)
- Mascota con 5 estados visuales representados mediante SVG + animaciones CSS
- Cálculo del estado de la mascota basado en promedio ponderado de los últimos 7 días
- Sistema de experiencia y niveles básico (10 niveles)
- Dashboard con estado actual y estadísticas semanales
- Notificaciones in-app básicas (estado crítico de mascota)
- Actualización de estado de mascota en tiempo real (WebSocket)

### 4.2. Fuera del MVP (Features Futuras)

- Mascotas con apariencia personalizable (accesorios, colores, skins)
- Múltiples mascotas por usuario
- Sistema social (compartir y comparar mascotas con amigos)
- Integración con wearables (Apple Health, Google Fit)
- Notificaciones push en dispositivos móviles
- Gamificación avanzada (logros, medallas, rankings globales)
- Sugerencias de hábitos basadas en IA generativa
- Exportación de datos de progreso

---

## 5. Propuesta Técnica

### 5.1. Stack Tecnológico

| Capa            | Tecnología                      | Justificación                                                                                    |
|-----------------|---------------------------------|--------------------------------------------------------------------------------------------------|
| Frontend        | React 18 + TypeScript           | Ecosistema maduro, tipado estático, modelo de componentes ideal para la mascota reactiva         |
| Estilos         | TailwindCSS                     | Velocidad de desarrollo en MVP, responsive por defecto, sin overhead de CSS en bundle            |
| Estado global   | Zustand                         | Más liviano que Redux, boilerplate mínimo, suficiente para el tamaño del MVP                     |
| Backend         | Spring Boot 3.x (Java 21)       | Inyección de dependencias nativa facilita SOLID/DI; monolito por capas con servicios del dominio; ecosistema maduro |
| Base de datos   | PostgreSQL                       | Modelo relacional con integridad referencial; consultas de agregación eficientes para estadísticas |
| ORM             | Spring Data JPA + Hibernate     | Integración nativa con Spring Boot; anotaciones JPA type-safe; migraciones con Flyway            |
| Tiempo real     | Spring WebSocket (STOMP/SockJS) | Soporte nativo en Spring Boot; STOMP simplifica el protocolo de mensajería sobre WebSocket       |
| Autenticación   | Spring Security + JWT (JJWT)    | Stateless, escalable horizontalmente; integración con el ecosistema Spring Security              |
| Build           | Maven                           | Estándar en el ecosistema Java universitario; gestión de dependencias declarativa                |
| Contenedores    | Docker + Docker Compose         | Reproducibilidad del entorno de desarrollo; onboarding simplificado para el equipo               |
| Testing         | JUnit 5 + Mockito + Spring Test | Estándar en el ecosistema Java; integración nativa con Spring Boot Test                         |

### 5.2. Arquitectura General

El backend sigue una **arquitectura de capas (Layered Architecture)**. La propuesta académica original planteaba módulos DDD por dominio; la implementación actual organiza el código por capas técnicas (`controllers`, `services`, `models`, `repositories`, `dtos`, `exceptions`, `configs`, `websockets`, `events`) y concentra la separación de dominio en nombres de entidades, servicios y casos de uso.

- Layered Architecture es el patrón más enseñado en contextos académicos y facilita la evaluación
- Los conceptos de dominio principales siguen explícitos en el código (Habit, Pet, CompletionRecord, WellnessScore, HabitService, RecordService, PetService)
- La evolución a paquetes por dominio sigue siendo posible si el proyecto crece, pero no debe asumirse como estructura actual
- La separación en capas soporta naturalmente los principios SOLID, especialmente SRP y DIP

**Capas del Backend:**

```
┌─────────────────────────────────────────────┐
│          Presentation Layer                  │
│     (REST Controllers + WebSocket Gateway)   │
├─────────────────────────────────────────────┤
│          Application Layer                   │
│              (Services)                      │
├─────────────────────────────────────────────┤
│          Domain Layer                        │
│   (Entities, Value Objects, Domain Rules)    │
├─────────────────────────────────────────────┤
│          Infrastructure Layer                │
│  (JPA Repositories, Spring Security, WS)     │
└─────────────────────────────────────────────┘
```

### 5.3. Módulos del Sistema

| Módulo        | Responsabilidad                                      | Estado actual |
|---------------|------------------------------------------------------|---------------|
| AuthModule    | Registro, login, generación y validación de JWT      | Implementado sin refresh token real |
| UserModule    | Gestión del perfil del usuario                       | Parcial: entidad User y usuario autenticado |
| HabitModule   | CRUD de hábitos, validaciones de dominio             | Implementado: crear, listar, editar y archivar |
| RecordModule  | Registro de cumplimiento diario, una vez por día     | Implementado |
| PetModule     | Estado de mascota, bienestar, XP y niveles           | Implementado con Factory simple y Decorator de XP |
| StatsModule   | Racha, porcentaje semanal, historial                 | Pendiente |
| NotificationModule | Notificaciones in-app, criterios de disparo     | Parcial: tabla/seed; falta API/UI |
| WebSocketModule | Gateway de tiempo real para eventos de mascota     | Implementado con topic público por usuario para demo |

---

## 6. Historias de Usuario

| ID    | Como...                   | Quiero...                                                     | Para...                                                        | Prioridad |
|-------|---------------------------|---------------------------------------------------------------|----------------------------------------------------------------|-----------|
| US-01 | Usuario nuevo             | Registrarme con email y contraseña                            | Tener acceso a mi propia mascota y hábitos                    | Must      |
| US-02 | Usuario autenticado       | Iniciar sesión con mis credenciales                           | Acceder a mi perfil y mascota                                  | Must      |
| US-03 | Usuario autenticado       | Crear un hábito con nombre, categoría y frecuencia semanal    | Empezar a trackear un comportamiento positivo                  | Must      |
| US-04 | Usuario con hábitos       | Marcar como completada una tarea de un hábito en el día       | Registrar mi progreso diario                                   | Must      |
| US-05 | Usuario con tareas        | Ver mi mascota cambiar de estado visual en tiempo real        | Recibir feedback emocional inmediato de mi progreso            | Must      |
| US-06 | Usuario con mascota       | Ver el nivel y la experiencia actual de mi mascota            | Entender su estado y cuánto falta para el próximo nivel        | Must      |
| US-07 | Usuario con historial     | Ver mis estadísticas semanales por hábito                     | Evaluar mi consistencia e identificar áreas de mejora          | Should    |
| US-08 | Usuario descuidado        | Recibir una notificación in-app cuando la mascota está mal    | Saber que necesita atención antes de que empeore               | Should    |
| US-09 | Usuario comprometido      | Editar o eliminar un hábito existente                         | Adaptar mis objetivos cuando cambian mis circunstancias        | Must      |
| US-10 | Usuario con racha         | Ver mi racha actual de días consecutivos                       | Motivarme a no romper la cadena                               | Should    |

---

## 7. Criterios de Aceptación del MVP

**CA-01 (Autenticación)**: El usuario puede registrarse, iniciar sesión y su sesión persiste mediante refresh token. El access token expira en 15 minutos. Las contraseñas se almacenan con bcrypt (mínimo 10 rounds).

> Estado actual: registro y login backend/frontend con JWT stateless están implementados; al registrarse se crea una mascota inicial con `PetFactory`. Refresh token, expiración de 15 minutos y cookie HttpOnly siguen pendientes. En la configuración actual el JWT expira en 24 horas y el frontend lo guarda en `localStorage` por simplicidad de demo.

**CA-02 (Hábitos)**: El usuario puede crear hasta 10 hábitos en el MVP. Cada hábito tiene: nombre (obligatorio, máx. 100 chars), categoría (enum: HEALTH, STUDY, SPORT, WELLNESS, NUTRITION), frecuencia semanal (1-7 días) y descripción (opcional).

**CA-03 (Registro de cumplimiento)**: El usuario puede marcar o desmarcar el cumplimiento de un hábito exactamente una vez por día calendario. El sistema registra timestamp con timezone del usuario.

**CA-04 (Estado de mascota)**: El estado de la mascota se recalcula tras cada registro de cumplimiento. Estados: CRITICAL (0–20%), POOR (21–40%), NEUTRAL (41–60%), GOOD (61–80%), EXCELLENT (81–100%). El cálculo considera los últimos 7 días con peso exponencial (día más reciente tiene mayor peso).

**CA-05 (Tiempo real)**: El cambio de estado de la mascota se refleja en el frontend en menos de 500ms desde el registro del cumplimiento, mediante WebSocket, sin necesidad de recargar la página.

**CA-06 (Estadísticas)**: El dashboard muestra: racha actual en días consecutivos, porcentaje de cumplimiento de la semana actual, y barras por hábito con el % de la semana.

**CA-07 (Niveles)**: La mascota acumula experiencia (XP) por cada hábito completado. La cantidad de XP por cumplimiento varía según la frecuencia comprometida del hábito. Los niveles son: 1–10, con umbrales de XP progresivos.

---

## 8. Riesgos y Mitigaciones

| Riesgo                                          | Probabilidad | Impacto | Mitigación                                                              |
|-------------------------------------------------|-------------|---------|-------------------------------------------------------------------------|
| Complejidad del algoritmo de estado de mascota  | Media       | Alto    | Definir el algoritmo con el patrón Strategy antes de implementar        |
| WebSockets con múltiples instancias en producción | Baja      | Alto    | Arquitectura documentada con Redis adapter para futura escala           |
| Scope creep del equipo durante el desarrollo    | Alta        | Medio   | Backlog MoSCoW estricto, definition of done por historia                |
| Diseño visual de la mascota sin diseñador       | Media       | Medio   | Usar SVG simple con CSS animations en MVP; iteración visual en v2       |
| Curva de aprendizaje de Spring Boot + JPA y patrones SOLID | Alta   | Medio   | Spike técnico en Semana 1, pair programming, code review con checklist  |
| Disponibilidad del equipo en período de parciales | Alta      | Alto    | Planificación con buffer del 20% en estimaciones por sprint             |

---

## 9. Alternativas Consideradas

### 9.1. Mobile Nativa vs Web

Se eligió **web** para el MVP porque:
- Un solo codebase cubre desktop y mobile (responsive)
- Sin necesidad de configurar App Store / Google Play para la demo universitaria
- El equipo tiene mayor experiencia con tecnologías web

Desventaja aceptada: sin notificaciones push nativas. Se mitiga con notificaciones in-app.

### 9.2. Monolito vs Microservicios

Se eligió **monolito modular** porque:
- Los microservicios introducen complejidad operacional (service discovery, comunicación entre servicios, tracing distribuido) que no está justificada para el tamaño del dominio
- Un monolito modular bien estructurado puede descomponerse en microservicios si escala, sin reescribir la lógica de negocio

> Estado actual: el repositorio implementa un monolito por capas. La separación en paquetes por dominio (`com.habitpet.habit`, `com.habitpet.pet`, `com.habitpet.record`) queda como evolución posible, no como estructura presente.

### 9.3. MongoDB vs PostgreSQL

Se eligió **PostgreSQL** porque:
- El dominio es relacional: usuarios tienen hábitos, hábitos tienen registros de cumplimiento. Estas relaciones se expresan naturalmente con tablas y foreign keys
- Las consultas de agregación para estadísticas (GROUP BY, window functions) son más eficientes en PostgreSQL que en MongoDB
- Integridad referencial garantizada por la base de datos, no solo por la aplicación

### 9.4. Redux vs Zustand

Se eligió **Zustand** porque:
- El estado del frontend es relativamente simple: mascota actual, lista de hábitos, usuario autenticado
- Redux introduce boilerplate significativo (actions, reducers, selectors) que no se justifica en este tamaño
- Zustand permite el mismo patrón de store sin la complejidad adicional

### 9.5. Stack Backend: Java / Spring Boot

Se eligió **Spring Boot 3.x con Java 21** porque:
- La inyección de dependencias del contenedor IoC de Spring facilita la aplicación del principio DIP (Dependency Inversion) de SOLID de forma nativa
- La organización actual por capas permite una evolución posterior a paquetes por dominio si el alcance crece
- Spring Data JPA implementa el patrón Repository sin boilerplate, con interfaces tipadas
- Java 21 ofrece records inmutables (ideales para Value Objects como `WellnessScore`) y mejoras en pattern matching
- Integración nativa con SpringDoc OpenAPI para documentación automática de la API REST

---

## 10. Métricas de Éxito

### Técnicas
- Tiempo de carga inicial menor a 2 segundos en conexión normal (3G)
- Tiempo de respuesta de API menor a 200ms en el percentil 95
- Cobertura de tests unitarios mayor al 70% en el backend
- 0 vulnerabilidades críticas de seguridad reportadas en revisión de código

### De Producto
- Al menos 5 usuarios activos usando la app durante 2 semanas consecutivas
- 60% de los usuarios que crean hábitos los registran al menos 3 días por semana durante el primer mes
- La mascota cambia de estado en el 100% de los casos tras un registro de cumplimiento

### Académicas
- Documentación de arquitectura completa (este RFC + ARQUITECTURA.md + ADRs)
- Aplicación de al menos 6 patrones de diseño justificados con ejemplos de código real del proyecto
- Demostración funcional del MVP en defensa del proyecto

---

## 11. Glosario

| Término           | Definición                                                                         |
|-------------------|------------------------------------------------------------------------------------|
| Hábito            | Comportamiento positivo que el usuario desea adoptar, con frecuencia configurable  |
| Registro          | Acto de marcar un hábito como cumplido para un día específico                      |
| Estado de mascota | Nivel de bienestar visual de la mascota, función del cumplimiento histórico        |
| XP                | Puntos de experiencia acumulados por cumplimiento de hábitos                       |
| Racha             | Número de días consecutivos con al menos un hábito cumplido                        |
| Check-in          | Acción de registrar el cumplimiento diario de un hábito                            |
| Bienestar         | Métrica interna calculada como promedio ponderado del cumplimiento de los últimos 7 días |
