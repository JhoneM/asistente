# Vidification — Estructura del backend

## Qué es
Una app que ayuda a desarrollar hábitos saludables. El usuario tiene un **avatar** que mejora
cuando cumple sus hábitos y se deteriora cuando los abandona. Hecho en **Java + Spring Boot**
con persistencia en una base **SQL**.

## Estado actual
**MVP funcional del backend — ~30-40 % del producto final.** Está implementada toda la
estructura, los seis patrones de diseño y el flujo principal del juego. Lo que falta
(personalización del avatar con puntos, logros, estadísticas, frontend y login) queda como
**trabajo futuro**.

## Cómo está organizada (capas)
Cada capa tiene una sola responsabilidad. La acción del usuario entra por la consola o por la
API, pasa por el Facade, atraviesa el dominio con sus patrones, y termina persistiendo en la
base SQL.

```
Consola interactiva   /   API REST
                ↓
       VidificationFacade  (orquesta)
                ↓
    Dominio + Patrones  (reglas del juego)
                ↓
     Repositorios JPA  →  Base H2 (SQL)
```

## Dónde está cada cosa

- **`model/`** — el dominio: Usuario, Habito, Avatar, Rutina, enums.
- **`model/state/`** — patrón **State** (estados del avatar).
- **`model/strategy/`** — patrón **Strategy** (cálculo de puntos).
- **`observer/`** — patrón **Observer** (reacciones al completar un hábito).
- **`factory/`** — patrón **Factory** (creación de hábitos).
- **`service/`** — patrón **Facade** (punto único de orquestación).
- **`controller/`** — API REST.
- **`repository/`** — acceso a la base (Spring Data JPA).
- **`dto/`** — objetos de entrada/salida.
- **`ConsolaInteractiva.java`** — menú para operar la app desde la terminal.

## Los 6 patrones de diseño implementados

| Patrón        | Categoría      | Para qué se usa                                              |
| ------------- | -------------- | ------------------------------------------------------------ |
| **State**     | Comportamiento | Estados del avatar (saludable, normal, decaído, crítico)     |
| **Observer**  | Comportamiento | Reacciones encadenadas al completar un hábito                |
| **Strategy**  | Comportamiento | Cómo se calcula el puntaje (fijo o con bonus por racha)      |
| **Factory**   | Creacional     | Construir hábitos ya configurados                            |
| **Composite** | Estructural    | Rutina que agrupa hábitos y sub-rutinas                      |
| **Facade**    | Estructural    | Servicio único que orquesta todo                             |

## Cómo funciona en vivo
Se levanta con un comando (`./mvnw spring-boot:run`). Al arrancar precarga un usuario con dos
hábitos (comer y entrenar) y abre un **menú interactivo** para decidir las acciones. Al mismo
tiempo queda disponible la **API REST** y la **consola de la base H2** para ver las tablas SQL.
