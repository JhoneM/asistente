# Backend — HabitPet

API REST con Spring Boot 3 + Java 21.

## Estructura

```
src/main/java/com/habitpet/
├── shared/          Cross-cutting (config, exception, seed)
├── auth/            Autenticación y autorización
├── habit/           Gestión de hábitos
├── record/          Check-ins de cumplimiento
├── pet/             Estado y bienestar de la mascota
├── stats/           Estadísticas del usuario
├── notification/    Notificaciones in-app
└── websocket/       WebSocket STOMP
```

## Build y arranque

```bash
# Construir
mvn clean package

# Arrancar en local (si Postgres está corriendo)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# En Docker (desde raíz del proyecto)
docker compose up --build backend
```

## Docs

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- Health check: `GET /actuator/health`
