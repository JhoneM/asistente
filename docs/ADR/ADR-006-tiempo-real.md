# ADR-006: Comunicación en Tiempo Real

## Estado
Aceptada

## Contexto

Uno de los requerimientos core de HabitPet es que la mascota se actualice visualmente en tiempo real cuando el usuario completa un check-in, sin necesidad de recargar la página. Esto es fundamental para la experiencia de usuario: el usuario hace click en "completar hábito" y ve inmediatamente a su mascota reaccionar.

Las opciones técnicas para lograr este comportamiento son:
1. Polling: el cliente pregunta al servidor cada N segundos si hay cambios
2. Long Polling: el servidor mantiene la conexión abierta hasta que hay datos nuevos
3. Server-Sent Events (SSE): el servidor envía actualizaciones unidireccionales al cliente
4. WebSocket: conexión bidireccional persistente entre cliente y servidor

## Decisión

Se utiliza **Spring WebSocket con STOMP** en el backend, y **SockJS + cliente STOMP** en el frontend React.

| Componente  | Tecnología                              |
|-------------|-----------------------------------------|
| Backend     | Spring Boot `@EnableWebSocketMessageBroker` + STOMP |
| Frontend    | `@stomp/stompjs` + `sockjs-client`      |
| Protocolo   | STOMP sobre WebSocket, con fallback SockJS |
| Seguridad   | Token JWT en el handshake de SockJS     |

### Modelo de mensajes STOMP

```
Suscripciones del cliente:
  /user/queue/pet          → actualizaciones del estado de la mascota
  /user/queue/notification → alertas in-app

El servidor envía mensajes a:
  /user/{userId}/queue/pet          → state, xp, level, message
  /user/{userId}/queue/notification → type, message
```

### Configuración Spring Boot

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback automático si WebSocket no está disponible
    }
}
```

### Envío de eventos desde el backend

```java
@Service
@RequiredArgsConstructor
public class PetWebSocketGateway {
    private final SimpMessagingTemplate messagingTemplate;

    public void emitPetStateChanged(String userId, Pet pet) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/pet",
            new PetStateChangedMessage(pet.getState(), pet.getXp(), pet.getLevel())
        );
    }
}
```

### Autenticación del WebSocket

La autenticación se realiza mediante un interceptor que valida el token JWT incluido en los headers del handshake STOMP:

```java
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                String userId = jwtService.extractUserId(token.substring(7));
                accessor.setUser(() -> userId); // Asocia el userId al socket
            }
        }
        return message;
    }
}
```

## Justificación

**WebSocket sobre Polling**:
- El polling (preguntar cada 2-3 segundos si hay cambios) genera tráfico innecesario: el usuario puede no hacer check-ins en horas, pero el cliente seguiría enviando requests. Con 50 usuarios concurrentes y polling cada 2 segundos, eso son 25 requests/segundo sin datos útiles
- WebSocket elimina el tráfico innecesario: el servidor solo envía datos cuando hay cambios reales (tras un check-in)
- La latencia con polling puede ser hasta N segundos; con WebSocket el estado de la mascota llega en milisegundos tras el check-in

**WebSocket sobre SSE**:
- SSE es unidireccional (servidor → cliente únicamente). Aunque en el MVP solo el servidor inicia eventos, en versiones futuras el cliente podría necesitar enviar datos en tiempo real (ej: "el usuario está viendo la mascota", para estadísticas de engagement)
- WebSocket soporta bidireccionalidad sin cambio de protocolo ni tecnología

**STOMP sobre WebSocket nativo**:
- STOMP (Simple Text Oriented Messaging Protocol) agrega semántica de mensajería encima del protocolo WebSocket raw: define destinos (`/user/queue/pet`), suscripciones y mensajes con headers tipados
- Sin STOMP, se debería implementar manualmente el routing de mensajes a usuarios específicos. Con STOMP + Spring, `convertAndSendToUser(userId, ...)` maneja esto automáticamente
- STOMP es el estándar en el ecosistema Spring y tiene soporte de primera clase con `@EnableWebSocketMessageBroker`

**SockJS para fallback**:
- Algunos proxies corporativos o redes universitarias bloquean WebSocket
- SockJS detecta automáticamente si WebSocket está disponible y hace fallback a long polling transparentemente para el cliente
- Sin SockJS, usuarios detrás de proxies restrictivos no recibirían actualizaciones en tiempo real

**`convertAndSendToUser` y user-specific messages**:
- Spring STOMP con `setUserDestinationPrefix("/user")` permite enviar mensajes a un usuario específico mediante `convertAndSendToUser(userId, destination, payload)`
- Esto garantiza que el usuario A no reciba las actualizaciones de la mascota del usuario B, sin necesitar implementar manualmente un sistema de rooms o suscripciones por usuario

## Alternativas Consideradas

**Socket.io (Node.js)**:
- Es la librería de WebSocket más usada en el ecosistema Node.js/NestJS. Descartada porque el backend del proyecto usa Spring Boot (Java). Mezclar un servidor Node.js solo para WebSocket introduciría un servicio adicional innecesario en el MVP.

**Server-Sent Events (SSE)**:
- Evaluado como alternativa más simple (solo HTTP, sin protocolo adicional, soporte nativo en Spring Boot con `SseEmitter`). Descartado por la limitación unidireccional y porque los proxies HTTP suelen cerrar conexiones SSE de larga duración.

**GraphQL Subscriptions**:
- Descartado por agregar la complejidad de GraphQL al proyecto cuando el resto de la API usa REST. La inversión de aprender GraphQL Subscriptions no se justifica cuando STOMP resuelve el mismo problema con menos overhead.

**Polling con React Query (`refetchInterval`)**:
- Soportado nativamente por React Query. Descartado porque genera tráfico innecesario y la latencia de la actualización (hasta N segundos) impacta negativamente la experiencia de "feedback inmediato" de la mascota.

**Firebase Realtime Database / Supabase Realtime**:
- Servicios BaaS que proveen tiempo real sin implementar el backend. Descartados porque introducen una dependencia de un servicio externo de pago, agregan latencia de red adicional, y no están bajo el control del equipo del proyecto.

## Consideraciones para Escalar (Fuera del MVP)

En el MVP, solo hay una instancia del servidor Spring Boot. Si se escalan múltiples instancias, el estado de las suscripciones WebSocket está en memoria de cada instancia: un usuario conectado a la instancia A no recibiría mensajes enviados desde la instancia B.

La solución documentada (no implementada en el MVP) es usar un **message broker externo** como RabbitMQ o Redis con el full-featured broker de Spring:

```java
// Configuración futura para múltiples instancias
config.enableStompBrokerRelay("/queue", "/topic")
      .setRelayHost("rabbitmq-host")
      .setRelayPort(61613);
```

Esta decisión se toma conscientemente: el MVP opera con una instancia y la arquitectura está documentada para escalar cuando sea necesario.

## Consecuencias

**Positivas**:
- El usuario ve el cambio de estado de la mascota en menos de 500ms tras el check-in
- El tráfico de red es mínimo comparado con polling — el servidor solo envía cuando hay cambios
- SockJS maneja automáticamente reconexiones y fallbacks a long polling
- STOMP con `convertAndSendToUser` garantiza aislamiento entre usuarios (usuario A no recibe mensajes de usuario B)
- Integración nativa con Spring Security: el interceptor STOMP puede verificar el JWT en el handshake

**Negativas**:
- Las conexiones WebSocket persistentes consumen más memoria en el servidor que las conexiones HTTP stateless. Para 50 usuarios concurrentes el impacto es despreciable, pero debe considerarse al escalar
- En arquitectura multi-instancia futura, se requiere un message broker externo (RabbitMQ o Redis) — documentado pero no implementado en el MVP
- Los tests de integración de WebSocket/STOMP son más complejos que los de REST (requieren un cliente STOMP en el test). Se usa `StompSessionHandlerAdapter` de Spring para tests de integración WebSocket
