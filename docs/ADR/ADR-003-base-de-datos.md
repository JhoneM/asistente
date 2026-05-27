# ADR-003: Base de Datos y ORM

## Estado
Aceptada

## Contexto

El sistema HabitPet necesita persistir:
- Usuarios con sus credenciales y preferencias (timezone)
- Hábitos configurados por el usuario, con atributos estructurados (categoría, frecuencia)
- Registros de cumplimiento diario, indexados por usuario y fecha para consultas eficientes
- Estado de la mascota (estado actual, XP, nivel) por usuario
- Notificaciones in-app

Las consultas más críticas del sistema son:
1. "Dame todos los registros de cumplimiento del usuario X en los últimos 7 días" (para el cálculo de bienestar)
2. "Dame los hábitos activos del usuario X" (para el cálculo de bienestar y la UI)
3. "¿Ya hay un registro del hábito Y para el usuario X el día de hoy?" (para validar duplicados)

## Decisión

Se utiliza **PostgreSQL** como base de datos relacional y **Prisma** como ORM.

## Justificación

**PostgreSQL**:

- El dominio de HabitPet es **inherentemente relacional**: usuarios tienen hábitos, hábitos generan registros, registros pertenecen a usuarios. Estas relaciones se expresan naturalmente con tablas y foreign keys, con integridad referencial garantizada por el motor de la base de datos
- Las consultas de agregación para estadísticas (GROUP BY fecha, COUNT de registros por hábito) son más expresivas y eficientes en SQL que con pipelines de aggregation de MongoDB
- La **restricción de unicidad** `UNIQUE(habitId, date)` en `completion_records` garantiza a nivel de base de datos que no puede haber dos check-ins del mismo hábito en el mismo día, sin depender de la lógica de la aplicación
- PostgreSQL soporta el tipo `Date` nativo para almacenar fechas sin hora, lo cual es crítico para el modelo de "check-in diario"
- PostgreSQL es gratuito, open source y el ORM seleccionado (Prisma) tiene soporte de primera clase para él

**Prisma**:

- Prisma genera queries **type-safe** a partir del schema: si se accede a un campo que no existe en el modelo, TypeScript lo detecta en tiempo de compilación
- El schema de Prisma (`schema.prisma`) es la **fuente única de verdad** para el modelo de datos: las migraciones se generan automáticamente a partir de cambios en el schema, reduciendo el riesgo de inconsistencias entre la BD y el código
- El Prisma Client es intuitivo para operaciones complejas como upsert (create or update) que se usa frecuentemente en `PetRepository`
- Las relaciones se navegan de forma tipada: `prisma.habit.findMany({ where: { userId }, include: { completionRecords: true } })`

## Alternativas Consideradas

**MongoDB (con Mongoose)**:
- Descartado porque el dominio es relacional y forzar una estructura de documentos introduciría complejidad artificial. Las consultas de estadísticas (promedio de cumplimiento por día, racha de días consecutivos) son significativamente más complejas con el aggregation pipeline de MongoDB que con SQL.
- La ausencia de restricciones de unicidad a nivel de BD (MongoDB no soporta UNIQUE constraints en arrays o documentos anidados de la misma forma que SQL) haría que la validación de check-ins duplicados dependa exclusivamente de la lógica de la aplicación, introduciendo riesgo de race conditions.

**SQLite**:
- Evaluado positivamente por la simplicidad de setup (sin servidor). Descartado porque SQLite no soporta conexiones concurrentes de escritura de forma eficiente, lo que sería un problema cuando múltiples usuarios realizan check-ins simultáneamente. Aceptable para desarrollo local, no para producción.

**Hibernate puro (sin Spring Data JPA)**:
- Alternativa válida. Descartado porque Spring Data JPA agrega una capa de abstracción encima de Hibernate que elimina el boilerplate de las operaciones CRUD básicas (implementaciones automáticas a partir de interfaces). Hibernate puro requeriría implementar manualmente cada método de repositorio, aumentando el tiempo de desarrollo sin aportar flexibilidad adicional en el MVP.

**Sequelize**:
- Descartado por el menor soporte de TypeScript comparado con Prisma. Sequelize fue diseñado para JavaScript y el soporte de TypeScript es un addon, lo que resulta en tipos menos precisos.

## Consecuencias

**Positivas**:
- La integridad referencial y las restricciones de unicidad se garantizan a nivel de BD, independientemente de errores en la lógica de la aplicación
- El schema de Prisma como fuente única de verdad reduce inconsistencias entre el modelo de datos y el código
- Las migraciones automáticas de Prisma simplifican la evolución del schema durante el desarrollo

**Negativas**:
- PostgreSQL requiere un servidor de base de datos (Docker o servicio en la nube), a diferencia de SQLite que es embebido. Esto agrega un paso al setup del entorno de desarrollo (mitigado con Docker Compose)
- Prisma no soporta queries SQL complejas con window functions directamente desde el cliente generado — se usaría `prisma.$queryRaw` para queries avanzadas de estadísticas si fuera necesario (no es el caso en el MVP)
