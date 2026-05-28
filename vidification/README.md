# Vidification 🎮

MVP de un tracker de hábitos con **gamificación**: el usuario cuida a un **avatar** que mejora
cuando cumple sus hábitos y se deteriora cuando los abandona. Hecho en **Java 21 + Spring Boot**
con **Spring Data JPA + H2 (SQL)** y **6 patrones de diseño** vistos en clase.

---

## ▶️ Cómo correr

Desde esta carpeta (`vidification/`):

```bash
./mvnw spring-boot:run
```

No hace falta instalar Maven ni una base de datos: el wrapper `./mvnw` baja Maven y H2 corre
embebida en memoria.

Al arrancar:
- Deja **precargado** un usuario (`Jugador`, id 1) con **dos hábitos**: 🍎 *Comer saludable* (id 1) y 🏃 *Entrenar* (id 2).
- Abre un **menú interactivo** en la consola para que decidas en vivo (darle de comer, entrenar, terminar el día, ver hábitos, salir).
- Al mismo tiempo queda disponible la **API REST** y la **consola H2**.

> Importante: corré el comando en una terminal real (la de Windsurf o la del Mac) y escribí el número de la opción + Enter. El menú lee del teclado.

---

## 🐾 Cómo se ve el avatar (sin frontend)

El estado del avatar se observa de tres formas:

**1. Consola interactiva** (al correr la app) — avatar en ASCII + barra de vitalidad, y un menú para decidir:

```
   ( ^_^ )   🙂 NORMAL
   Vitalidad [██████░░░░] 60/100    Puntos: 0
   Tu avatar se siente bien. Mantené el ritmo.

¿Qué querés hacer?
  1) 🍎 Darle de comer  (completar 'Comer saludable')
  2) 🏃 Hacerlo entrenar (completar 'Entrenar')
  3) 🌙 Terminar el día  (lo no cumplido lo deteriora)
  4) 📋 Ver los hábitos
  0) 🚪 Salir
Opción: 1
✅ Completaste "Comer saludable"  (+10 pts, total 10)

   ( ^_^ )   🙂 NORMAL
   Vitalidad [██████░░░░] 64/100    Puntos: 10
```

**2. API REST** → `GET /api/usuarios/1/avatar` devuelve el estado en JSON (se abre en el navegador).

**3. Consola H2** (las tablas SQL) → http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:vidification` · Usuario: `sa` · Password: *(vacío)*

---

## 🌐 Endpoints REST

| Método | Ruta                              | Qué hace                                       |
| ------ | --------------------------------- | ---------------------------------------------- |
| GET    | `/api/usuarios/{id}/avatar`       | Estado del avatar (vitalidad, estado, puntos)  |
| GET    | `/api/usuarios/{id}/habitos`      | Lista los hábitos del usuario                  |
| POST   | `/api/habitos/{id}/completar`     | Completa un hábito (lo "alimenta")             |
| POST   | `/api/usuarios/{id}/pasar-dia`    | Cierra el día: penaliza lo no cumplido         |
| POST   | `/api/usuarios`                   | Crea un usuario `{ "nombre": "Ana" }` *(extra)* |
| POST   | `/api/usuarios/{id}/habitos`      | Agrega un hábito nuevo *(extra, fuera del MVP)* |

Ejemplo de cuerpo para crear un hábito (endpoint extra):

```json
{ "nombre": "Leer", "categoria": "ESTUDIO", "dificultad": "MEDIA" }
```

`categoria`: ALIMENTACION · DESCANSO · EJERCICIO · ESTUDIO · ORGANIZACION
`dificultad`: FACIL · MEDIA · DIFICIL

---

## ⚙️ Cómo funciona (mecánica)

- El avatar tiene **vitalidad** (0–100); arranca en 60.
- **Completar un hábito** sube la vitalidad (`dificultad × 4`) y suma **puntos**.
- **Estados según la vitalidad:** ≥80 SALUDABLE 😄 · ≥50 NORMAL 🙂 · ≥25 DECAÍDO 😣 · <25 CRÍTICO 🤢.
- **Pasar el día** penaliza (−6 de vitalidad) por cada hábito que quedó sin cumplir.
- **Puntaje:** *Comer* usa puntaje fijo; *Entrenar* suma un **bonus por racha** (premia la constancia).

---

## 🧩 Patrones de diseño

| Patrón | Categoría | Dónde está | Para qué |
| ------ | --------- | ---------- | -------- |
| **State** | Comportamiento | `model/state/` + `model/Avatar` | El avatar delega su comportamiento al estado actual y **transiciona** al cambiar la vitalidad. |
| **Observer** | Comportamiento | `observer/` | Al completar un hábito, varios observadores reaccionan (suman puntos, actualizan el avatar, loguean). |
| **Strategy** | Comportamiento | `model/strategy/` | Distintas formas de calcular el puntaje (fijo vs. bonus por racha). |
| **Factory** | Creacional | `factory/HabitoFactory` | Centraliza la creación de hábitos ya configurados con su estrategia. |
| **Composite** | Estructural | `model/Rutina` + `model/Habito` + `model/ComponenteHabito` | Una rutina agrupa hábitos (y sub-rutinas) y se tratan de forma uniforme. |
| **Facade** | Estructural | `service/VidificationFacade` | Interfaz simple que orquesta repos, factory, estrategias, avatar y observadores. |

> **Singleton (bonus):** en Spring los `@Service`/`@Component`/`@Repository` ya son singletons del contenedor.

### Flujo de "completar un hábito"

```
Controller / Demo → VidificationFacade.completarHabito()        [Facade]
   ├─ Habito.puntajeTotal()  → EstrategiaPuntaje                 [Strategy]
   ├─ SujetoHabitos.notificar(EventoHabito)                      [Observer]
   │     ├─ ObservadorRecompensas → suma puntos
   │     ├─ ObservadorAvatar      → Avatar.recompensar() → cambia EstadoAvatar  [State]
   │     └─ ObservadorLog         → log por consola
   └─ guarda en H2 (JPA)
```

---

## ✅ SOLID (para la defensa)

- **SRP:** cada clase, una responsabilidad (los repos persisten, la Factory crea, las estrategias calculan, el Facade orquesta).
- **OCP:** agregar una estrategia, un estado o un observador nuevo **no obliga a modificar** lo existente, solo a extender.
- **LSP:** cualquier `EstadoAvatar` / `EstrategiaPuntaje` / `ObservadorHabito` es sustituible por otra implementación.
- **ISP:** interfaces chicas y específicas.
- **DIP:** se depende de **abstracciones** (interfaces); Spring inyecta las dependencias.

---

## 🎯 Alcance (MVP)

**Incluido:** usuario + avatar con estados · 2 hábitos precargados (Comer y Entrenar) que se completan ·
listar hábitos · pasar el día / deterioro · acumular puntos · historial en SQL · base de datos · API REST · demo por consola.

**Fuera del MVP (trabajo futuro):** agregar hábitos a mano, personalizar el avatar con puntos, logros,
estadísticas, interfaz gráfica y login.

---

## 🗂️ Estructura

```
src/main/java/com/uade/vidification/
├── model/            # Dominio: Usuario, Habito, RegistroHabito, Avatar, Rutina, ComponenteHabito, enums
│   ├── state/        # Patrón State
│   └── strategy/     # Patrón Strategy
├── observer/         # Patrón Observer
├── factory/          # Patrón Factory
├── service/          # Patrón Facade (VidificationFacade)
├── controller/       # API REST + manejo de errores
├── repository/       # Spring Data JPA (H2)
├── dto/              # Objetos de transferencia (requests/responses)
└── ConsolaInteractiva.java   # Precarga datos + menú interactivo por consola al arrancar
```
