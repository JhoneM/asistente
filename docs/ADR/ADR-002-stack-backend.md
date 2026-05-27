# ADR-002: Stack de Tecnologías Backend

## Estado
Aceptada

## Contexto

El backend de HabitPet debe:
- Proveer una API REST para las operaciones CRUD (hábitos, registros, perfil)
- Manejar autenticación y autorización mediante JWT
- Emitir eventos en tiempo real cuando cambia el estado de la mascota (WebSocket/STOMP)
- Ejecutar lógica de negocio: cálculo de bienestar de la mascota, sistema de XP/niveles, estadísticas
- Ser evaluado académicamente por la aplicación de principios SOLID, patrones GRASP y patrones de diseño GoF

El equipo tiene familiaridad con Java desde las materias de programación de la carrera. El contexto es un proyecto universitario de 10 semanas.

## Decisión

Se utiliza **Spring Boot 3.x** con **Java 21** como framework backend. El stack completo del backend es:

| Componente         | Tecnología                          |
|--------------------|-------------------------------------|
| Framework          | Spring Boot 3.x                     |
| Lenguaje           | Java 21                             |
| API REST           | Spring Web MVC                      |
| Persistencia       | Spring Data JPA + Hibernate         |
| Migraciones        | Flyway                              |
| Autenticación      | Spring Security + JWT (JJWT)        |
| WebSocket          | Spring WebSocket (STOMP + SockJS)   |
| Validación         | Jakarta Bean Validation             |
| Testing            | JUnit 5 + Mockito + Spring Boot Test|
| Documentación API  | SpringDoc OpenAPI (Swagger)         |
| Build              | Maven                               |

## Justificación

**Spring Boot sobre otras opciones Java (Quarkus, Micronaut)**:
- Spring Boot es el framework Java más adoptado en la industria y en contextos académicos, con mayor disponibilidad de documentación y recursos educativos
- El sistema de **inyección de dependencias** (`@Autowired`, constructores con `@Service`) facilita la aplicación del principio **D (Dependency Inversion)** de SOLID de forma nativa y sin configuración adicional
- `@Component`, `@Service`, `@Repository`, `@Controller` mapean directamente a las cuatro capas de la arquitectura de capas, lo que hace que la estructura del proyecto sea legible y evaluable
- Spring Data JPA provee el **patrón Repository** listo para usar: una interfaz que extiende `JpaRepository<Pet, String>` genera automáticamente las implementaciones CRUD sin código adicional
- Spring Events (`ApplicationEventPublisher` + `@EventListener`) implementa el **patrón Observer** de forma idiomática para desacoplar módulos sin polling

**Java 21 específicamente**:
- Los **records** de Java son inmutables por defecto, lo que los hace ideales para Value Objects como `WellnessScore` (requisito del dominio)
- Los **sealed classes** permiten modelar el patrón State (CRITICAL, POOR, NEUTRAL, GOOD, EXCELLENT) con exhaustividad verificada por el compilador
- Java 21 con `var` y pattern matching reduce el boilerplate en comparación con versiones anteriores

**Spring Security + JWT**:
- Spring Security es el estándar de seguridad en el ecosistema Spring. No requiere librerías adicionales para configurar autenticación stateless
- La integración con `OncePerRequestFilter` permite implementar el filtro JWT sin acoplamiento al framework HTTP subyacente
- JJWT (Java JWT) es la librería más usada para generación y validación de tokens JWT en Java

**Spring WebSocket (STOMP)**:
- STOMP (Simple Text Oriented Messaging Protocol) sobre WebSocket simplifica el protocolo de mensajería: define conceptos de "destinos", "suscripciones" y "mensajes" en lugar de manejar frames WebSocket raw
- SockJS provee fallback automático a long polling para clientes que no soporten WebSocket nativo
- Spring Boot provee soporte nativo con `@EnableWebSocketMessageBroker` sin dependencias adicionales

**Spring Data JPA + Flyway**:
- Spring Data JPA permite definir repositorios como interfaces tipadas sin implementación: `PetRepository extends JpaRepository<Pet, String>`
- Flyway gestiona las migraciones de base de datos de forma declarativa (archivos SQL versionados), complementando a JPA para migraciones de esquema controladas
- Las anotaciones JPA (`@Entity`, `@Column`, `@UniqueConstraint`) definen el mapeo objeto-relacional en las entidades de dominio

## Alternativas Consideradas

**Node.js (NestJS)**:
- Evaluado inicialmente como alternativa. Descartado porque el equipo tiene mayor experiencia con Java desde las materias de la carrera universitaria, lo que reduce la curva de aprendizaje y el riesgo de entrega
- La inyección de dependencias y la estructura modular de NestJS son equivalentes a Spring Boot en concepto, por lo que el cambio no impacta el diseño arquitectónico

**Python (Django / FastAPI)**:
- Descartado por el cambio de lenguaje entre frontend (TypeScript/JavaScript) y backend (Python), que duplica el overhead cognitivo del equipo
- Django tiene un ORM propio que, aunque potente, difiere significativamente del estándar JPA/Hibernate que el equipo ya conoce

**Quarkus**:
- Alternativa Java moderna con arranque ultrarrápido y menor footprint de memoria. Descartado porque la documentación y los recursos educativos son menos abundantes que Spring Boot, lo que aumenta la fricción en el aprendizaje durante el desarrollo del MVP

**Micronaut**:
- Similar a Quarkus. Descartado por las mismas razones de ecosistema y recursos educativos

**Spring Boot 2.x vs 3.x**:
- Se eligió la versión 3.x (la más reciente) porque soporta Java 21, usa Jakarta EE 10 (paquetes `jakarta.*` en lugar de `javax.*`) y tiene soporte de largo plazo (LTS). La versión 2.x entró en fin de soporte en 2024.

## Consecuencias

**Positivas**:
- Las anotaciones de Spring (`@Service`, `@Repository`, `@Controller`) hacen explícita la capa a la que pertenece cada clase — directamente evaluable
- Spring Data JPA implementa el patrón Repository con interfaces tipadas, cumpliendo DIP sin código de infraestructura adicional
- Spring Events desacopla los módulos siguiendo el patrón Observer, con `@EventListener` como punto de extensión
- Java 21 records son ideales para Value Objects inmutables del dominio
- El ecosistema de Spring tiene cobertura de testing robusta (Spring Boot Test, `@DataJpaTest`, `@WebMvcTest`, Testcontainers)

**Negativas**:
- El tiempo de arranque de Spring Boot es mayor que alternativas como Quarkus o NestJS (no relevante en producción para una API, solo en desarrollo)
- El setup inicial de Spring Security para JWT requiere más configuración que frameworks con autenticación out-of-the-box (mitigado usando `spring-security-jwt` o JJWT con una clase `JwtFilter` bien documentada)
- La verbosidad de Java en comparación con TypeScript puede ralentizar el desarrollo inicial. Mitigación: Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`) reduce significativamente el boilerplate

## Estructura de Paquetes (Organización por Dominio)

```
com.habitpet/
├── auth/
│   ├── controller/    AuthController.java
│   ├── service/       AuthService.java
│   ├── domain/        User.java, RefreshToken.java
│   ├── repository/    UserRepository.java
│   └── security/      JwtFilter.java, JwtService.java
├── habit/
│   ├── controller/    HabitController.java
│   ├── service/       HabitService.java
│   ├── domain/        Habit.java, HabitCategory.java
│   └── repository/    HabitRepository.java
├── record/
│   ├── controller/    RecordController.java
│   ├── service/       RecordService.java
│   ├── domain/        CompletionRecord.java
│   ├── event/         RecordCompletedEvent.java
│   └── repository/    RecordRepository.java
├── pet/
│   ├── controller/    PetController.java
│   ├── service/       PetService.java
│   ├── domain/        Pet.java, PetState.java, WellnessScore.java
│   ├── strategy/      WellnessCalculator.java, WeightedAverageStrategy.java
│   └── repository/    PetRepository.java
├── stats/
│   ├── controller/    StatsController.java
│   └── service/       StatsService.java
├── notification/
│   ├── service/       NotificationService.java
│   └── domain/        Notification.java
└── websocket/
    └── PetWebSocketGateway.java
```
