# Informe TPO - HabitPet

## Datos del proyecto

**Materia:** Trabajo Practico Obligatorio Proceso desarrollo de software 
**Institucion:** UADE  
**Proyecto:** HabitPet  
**Equipo:** Completar integrantes  
**Fecha:** Junio 2026  

---

## 1. Resumen Ejecutivo

HabitPet es una aplicacion web inspirada en la logica de mascotas virtuales tipo Tamagotchi. Su objetivo es acompanar al usuario en la construccion de habitos saludables mediante una mascota cuyo estado depende del cumplimiento diario de esos habitos.

La idea central del proyecto es transformar el seguimiento de habitos en una experiencia mas emocional y visual. En lugar de mostrar solamente porcentajes o listas de tareas, HabitPet vincula el progreso del usuario con el bienestar de una mascota virtual. Si el usuario cumple sus habitos, la mascota mejora, gana experiencia y sube de nivel. Si descuida sus habitos, el estado de la mascota empeora.

El sistema fue desarrollado como una aplicacion web full stack con React en el frontend, Spring Boot en el backend y PostgreSQL como base de datos.

---

## 2. Problema y Motivacion

Muchas aplicaciones de seguimiento de habitos se basan en rachas, porcentajes o listas de tareas. Si bien estos mecanismos son utiles, muchas veces no generan suficiente compromiso a largo plazo. El usuario puede abandonar el seguimiento porque no percibe una consecuencia emocional clara.

HabitPet propone una alternativa: asociar el progreso del usuario con una mascota virtual persistente. La mascota funciona como una representacion visual del cuidado personal. De esta manera, el cumplimiento de habitos deja de ser solo un dato numerico y pasa a tener una consecuencia visible dentro de la aplicacion.

---

## 3. Objetivos

### Objetivo general

Desarrollar una aplicacion web que permita registrar habitos saludables y reflejar el progreso del usuario en el estado de una mascota virtual.

### Objetivos especificos

- Permitir el registro e inicio de sesion de usuarios.
- Permitir crear, editar, listar y archivar habitos.
- Registrar el cumplimiento diario de cada habito.
- Evitar registros duplicados para el mismo habito en el mismo dia.
- Calcular el bienestar de la mascota en base al cumplimiento reciente.
- Actualizar el estado de la mascota en tiempo real.
- Aplicar patrones de diseno de forma justificada y sin sobredisenar el sistema.

---

## 4. Alcance Implementado

El alcance actual cubre el flujo principal del MVP:

1. Registro de usuario.
2. Creacion automatica de mascota inicial.
3. Inicio de sesion con JWT.
4. Visualizacion de dashboard.
5. Gestion basica de habitos.
6. Check-in diario de habitos.
7. Persistencia del registro en PostgreSQL.
8. Recalculo del bienestar de la mascota.
9. Suma de XP y niveles.
10. Actualizacion en tiempo real mediante WebSocket/STOMP.

### Funcionalidades pendientes o simplificadas

- Refresh token con cookie HttpOnly.
- Estadisticas avanzadas de rachas e historial.
- Notificaciones in-app completas.
- Autenticacion avanzada del canal WebSocket.
- Mascota visual avanzada con sprites o animaciones complejas.

Estas funcionalidades quedan fuera del alcance principal para evitar complejidad excesiva en el MVP.

---

## 5. Stack Tecnologico

| Capa | Tecnologia |
|---|---|
| Frontend | React 18, TypeScript, Vite |
| Estado frontend | Zustand |
| Estilos | TailwindCSS |
| Backend | Spring Boot 3, Java 21 |
| Persistencia | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security + JWT |
| Tiempo real | Spring WebSocket + STOMP + SockJS |
| Migraciones | Flyway |
| Build | Maven, npm, Docker Compose |
| Testing | JUnit 5, Mockito, AssertJ |

---

## 6. Arquitectura

El backend sigue una arquitectura por capas:

- **Controllers:** exponen endpoints REST.
- **Services:** contienen la logica de negocio.
- **Models:** representan entidades JPA y value objects.
- **Repositories:** abstraen el acceso a datos mediante Spring Data JPA.
- **DTOs:** definen requests y responses.
- **WebSocket Gateway:** envia actualizaciones al frontend.

El frontend es una SPA construida con React. La pantalla principal muestra la mascota, los habitos activos y las acciones disponibles para registrar cumplimiento o gestionar habitos.

### Flujo principal de check-in

1. El usuario presiona "Hecho" sobre un habito.
2. El frontend envia `POST /api/records`.
3. `RecordService` valida que el habito pertenezca al usuario y que no exista un registro previo para ese dia.
4. Se guarda un `CompletionRecord` en PostgreSQL.
5. Se publica un `CheckInCompletedEvent`.
6. `WellnessService` escucha el evento y recalcula el bienestar.
7. `PetService` actualiza estado, XP y nivel de la mascota.
8. `PetWebSocketGateway` envia el nuevo estado al frontend.
9. La interfaz se actualiza sin recargar la pagina.

---

## 7. Modelo de Dominio

Entidades principales:

- **User:** representa al usuario registrado.
- **Habit:** habito configurable por el usuario.
- **CompletionRecord:** registro diario de cumplimiento.
- **Pet:** mascota asociada al usuario.
- **Notification:** entidad preparada para futuras notificaciones.

Value object:

- **WellnessScore:** representa el puntaje de bienestar de la mascota entre 0 y 100.

Enumeraciones relevantes:

- **HabitCategory:** HEALTH, STUDY, SPORT, WELLNESS, NUTRITION.
- **PetState:** CRITICAL, POOR, NEUTRAL, GOOD, EXCELLENT.

---

## 8. Persistencia del Registro

El sistema guarda cada check-in en la tabla `completion_records`.

Cada registro contiene:

- ID unico.
- Habito asociado.
- Usuario asociado.
- Fecha del cumplimiento.
- Timestamp de creacion.

Ademas, existe una restriccion unica sobre `(habit_id, date)`, lo que impide que el mismo habito se registre mas de una vez en el mismo dia.

Esto significa que el registro no queda solo en memoria ni solo en el frontend: queda persistido en PostgreSQL.

---

## 9. Patrones de Diseno Aplicados

### Strategy

Se aplica en el calculo de bienestar de la mascota.

Clases principales:

- `WellnessCalculator`
- `ExponentialWeightedWellnessCalculator`

Justificacion: permite cambiar el algoritmo de calculo sin modificar los servicios que lo utilizan.

### Observer

Se aplica en el flujo posterior al check-in.

Clases principales:

- `CheckInCompletedEvent`
- `ApplicationEventPublisher`
- `WellnessService`

Justificacion: `RecordService` no necesita conocer todos los servicios que reaccionan al registro de un habito. Publica un evento y otros componentes responden.

### Facade

Se aplica en `WellnessService`, que centraliza la orquestacion entre habitos, registros, calculo de bienestar, mascota y WebSocket.

Justificacion: simplifica el flujo y evita que un controller o service puntual coordine demasiadas dependencias.

### Repository

Se aplica mediante Spring Data JPA.

Ejemplos:

- `UserRepository`
- `HabitRepository`
- `CompletionRecordRepository`
- `PetRepository`

Justificacion: separa la logica de negocio del acceso concreto a base de datos.

### Factory simple

Se aplica con `PetFactory`.

Justificacion: al registrar un usuario se crea una mascota inicial. La Factory centraliza los valores por defecto de esa mascota y evita que `AuthService` tenga detalles de construccion.

### Decorator

Se aplica en el calculo de XP.

Clases principales:

- `XpRewardCalculator`
- `BaseXpRewardCalculator`
- `FrequencyBonusXpDecorator`

Justificacion: se parte de una XP base por check-in y se agrega un bonus segun la frecuencia semanal del habito. El bonus se agrega como una capa sobre el calculo base sin modificarlo.

---

## 10. Decisiones de Diseno

### Monolito modular por capas

Se eligio un backend monolitico por capas porque el alcance del proyecto no justifica microservicios. Esta decision facilita el desarrollo, la prueba y la explicacion academica del sistema.

### PostgreSQL

Se eligio PostgreSQL porque el dominio es relacional: usuarios tienen habitos, habitos tienen registros, y cada usuario tiene una mascota.

### WebSocket con STOMP

Se eligio Spring WebSocket + STOMP para que la mascota pueda actualizarse en tiempo real luego de cada check-in.

### JWT stateless

Se implemento autenticacion JWT simple para mantener el backend sin estado de sesion. El refresh token queda documentado como mejora futura.

---

## 11. Pruebas

El backend cuenta con tests unitarios sobre servicios y calculos principales.

Cobertura funcional testeada:

- Registro e inicio de sesion.
- Validaciones de habitos.
- Registro de check-ins.
- Evitacion de check-ins duplicados.
- Calculo de bienestar.
- Factory de mascota inicial.
- Decorator de XP.

Actualmente el proyecto contiene 46 tests unitarios.

---

## 12. Como Ejecutar el Proyecto

Requisito principal:

- Docker Desktop con WSL 2 en Windows.

Comando de arranque:

```bash
docker compose up --build
```

URLs:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

Usuarios de prueba:

| Email | Password |
|---|---|
| `demo@habitpet.com` | `demo1234` |
| `maria@habitpet.com` | `demo1234` |
| `lucas@habitpet.com` | `demo1234` |

---

## 13. Demo Sugerida

1. Ingresar con `demo@habitpet.com`.
2. Mostrar el estado inicial de Chispa.
3. Crear un habito nuevo.
4. Editar el habito.
5. Registrar un check-in.
6. Mostrar que se actualiza XP/bienestar.
7. Mostrar que el sistema no permite repetir el mismo check-in del dia.
8. Archivar el habito.
9. Crear un usuario nuevo y mostrar que se genera una mascota automaticamente.

---

## 14. Conclusiones

HabitPet cumple el objetivo principal del MVP: permite gestionar habitos y visualizar su impacto sobre una mascota virtual. El proyecto integra backend, frontend, persistencia, autenticacion, tiempo real y patrones de diseno aplicados sobre necesidades reales del dominio.

La implementacion evita sobredisenar el sistema. Los patrones se aplican donde aportan claridad o extensibilidad concreta: Strategy para bienestar, Observer para reacciones al check-in, Facade para orquestacion, Repository para persistencia, Factory para creacion inicial de mascota y Decorator para bonus de XP.

Como trabajo futuro, se propone completar estadisticas, notificaciones, refresh token y una representacion visual mas elaborada de la mascota.
