# ADR-004: Estilo de Arquitectura — Layered Architecture con módulos DDD

## Estado
Aceptada

## Contexto

El sistema HabitPet necesita una estructura de código que:
1. Facilite la evaluación académica de los principios SOLID y patrones de diseño
2. Permita que el equipo trabaje en módulos independientes sin conflictos constantes de merge
3. Sea lo suficientemente simple para implementarse en 10 semanas
4. Escale razonablemente si el proyecto continúa después de la materia

Las principales decisiones de estructura son: ¿cómo se organiza el código del backend? ¿Por capa técnica o por dominio? ¿Monolito o microservicios?

## Decisión

Se adopta **Layered Architecture (Arquitectura de Capas)** en el backend, con módulos organizados por dominio siguiendo principios de **Domain-Driven Design (DDD)**. Se descarta explícitamente la arquitectura de microservicios para el MVP.

La estructura de directorios refleja esta decisión:

```
src/
├── modules/
│   ├── auth/                 # Módulo de dominio: autenticación
│   │   ├── controllers/      # Capa de presentación
│   │   ├── services/         # Capa de aplicación
│   │   ├── repositories/     # Capa de infraestructura
│   │   └── domain/           # Entidades y value objects
│   ├── habit/                # Módulo de dominio: hábitos
│   ├── record/               # Módulo de dominio: registros de cumplimiento
│   ├── pet/                  # Módulo de dominio: mascota
│   └── stats/                # Módulo de dominio: estadísticas
├── shared/                   # Código compartido (excepciones, decoradores, DTOs comunes)
└── infrastructure/           # Infraestructura transversal (Prisma, WebSocket, JWT)
```

## Justificación

**Layered Architecture**:
- La separación en capas (Presentation → Application → Domain → Infrastructure) es el patrón más enseñado en contextos académicos, lo que facilita la evaluación por parte del docente
- Cada capa tiene una única razón de cambio (alineado con SRP de SOLID): si cambia el framework HTTP, solo cambia la capa de presentación; si cambia el ORM, solo cambia la infraestructura
- Las reglas de dependencia entre capas (las capas superiores dependen de las inferiores, nunca al revés) son verificables mediante herramientas de análisis estático
- Facilita los tests: la lógica de negocio en la capa de aplicación puede testearse sin HTTP ni base de datos

**Módulos por dominio (DDD)**:
- Organizar por dominio (`habit/`, `pet/`) en lugar de por capa técnica (`controllers/`, `services/`) facilita encontrar todo el código relacionado a un concepto en un solo lugar
- Mejora el trabajo en equipo: un desarrollador puede trabajar en `habit/` sin conflictos con otro trabajando en `pet/`
- Alinea el código con el lenguaje del negocio (Ubiquitous Language de DDD): si un stakeholder pregunta por "los hábitos", el desarrollador sabe exactamente dónde mirar

**Monolito modular**:
- Los microservicios introducen complejidad operacional (service discovery, comunicación entre servicios, trazabilidad distribuida) que no está justificada para el tamaño del dominio ni para el timeline universitario
- Un monolito modular bien diseñado puede descomponerse en microservicios si el proyecto escala, sin reescribir la lógica de negocio (las interfaces entre módulos ya están definidas)
- Para el contexto del MVP, el overhead de operar múltiples servicios independientes (cada uno con su propia BD, CI/CD, monitoring) es prohibitivo

## Alternativas Consideradas

**Organización por capa técnica (todos los controllers juntos, todos los services juntos)**:
- Descartada porque al crecer el proyecto, el directorio `services/` contendrá `AuthService`, `HabitService`, `PetService`, etc., sin relación aparente entre ellos. Es más difícil entender el sistema de un vistazo.

**Arquitectura hexagonal (Ports & Adapters)**:
- Considerada como alternativa más sofisticada. Descartada porque agrega un nivel de abstracción (ports/adapters) que dificulta la legibilidad sin aportar beneficios materiales en este tamaño de dominio. La arquitectura de capas logra la misma separación con menos complejidad.

**Clean Architecture**:
- Similar a la hexagonal. Descartada por las mismas razones: las reglas de dependencia adicionales y los círculos concéntricos de Clean Architecture agregan complejidad conceptual que el equipo universitario no necesita en este contexto.

**Microservicios**:
- Explícitamente descartados. El dominio de HabitPet no tiene la complejidad ni el tráfico que justifique microservicios. Citar Conway's Law: si el equipo es de 4 personas, una arquitectura de 4+ servicios independientes genera más fricción que valor.

## Consecuencias

**Positivas**:
- La estructura es predecible: cualquier miembro del equipo puede encontrar el código de un feature sabiendo su módulo de dominio
- Las capas permiten aplicar y demostrar los principios SOLID de forma explícita
- La separación de capas facilita los tests unitarios de la lógica de negocio

**Negativas**:
- Si el proyecto crece y múltiples módulos deben colaborar frecuentemente, el monolito puede generar un grafo de dependencias complejo. Solución futura: introducir un bus de eventos para desacoplar módulos (ya aplicado en el diseño con EventEmitter2)
- La arquitectura de capas puede llevar a servicios de aplicación "anémicos" que solo delegan sin lógica, o a entidades de dominio "hinchadas" con demasiada lógica. Se mitiga con code reviews enfocados en la correcta asignación de responsabilidades
