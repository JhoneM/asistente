# Principios SOLID y Patrones GRASP en HabitPet

Este documento explica cómo se aplican los principios SOLID y los patrones GRASP en el proyecto HabitPet, con ejemplos concretos del código del sistema. Cada principio se ilustra con un caso real del proyecto, no con ejemplos genéricos.

---

## Parte 1: Principios SOLID

### S — Single Responsibility Principle (Principio de Responsabilidad Única)

**Definición**: Una clase debe tener una única razón para cambiar.

**Problema que resuelve en HabitPet**: Sin este principio, `PetService` tendría que calcular el bienestar, gestionar XP y niveles, actualizar la base de datos, Y emitir eventos WebSocket. Cualquier cambio en el algoritmo de cálculo, en la estructura de la base de datos, o en el protocolo de WebSocket requeriría modificar la misma clase.

**Cómo se aplica**: La responsabilidad de calcular el bienestar de la mascota se extrae a una clase separada `WellnessCalculator`. Cada clase tiene una única razón de cambio:

```java
// MAL — PetService con múltiples responsabilidades (violación de SRP)
@Service
public class PetService_INCORRECTO {
    public void recalcularEstado(String userId) {
        List<CompletionRecord> records = recordRepository.findRecentByUserId(userId);
        List<Habit> habits = habitRepository.findActiveByUserId(userId);

        // Lógica de cálculo embebida — razón de cambio #1
        double totalWeight = 0;
        double weightedSum = 0;
        for (int i = 0; i < records.size(); i++) {
            double weight = Math.exp(-0.3 * i);
            weightedSum += weight;
            totalWeight += weight;
        }
        double score = (weightedSum / totalWeight) * 100;

        // Lógica de niveles embebida — razón de cambio #2
        int xpGained = 10 * habits.size();
        int newLevel = (xpGained / 100) + 1;

        // Persistencia directa — razón de cambio #3
        petRepository.updateState(userId, score, xpGained, newLevel);

        // Emisión de eventos — razón de cambio #4
        webSocketGateway.emit("pet:state-changed", userId, score, xpGained, newLevel);
    }
}

// BIEN — Separación de responsabilidades (aplicación de SRP)

// Razón de cambio: algoritmo de cálculo de bienestar
@Service
public class WeightedAverageWellnessCalculator implements WellnessCalculator {

    @Override
    public WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days) {
        if (records.isEmpty()) return new WellnessScore(0);

        double weightedSum = 0;
        double totalWeight = 0;

        for (int i = 0; i < days; i++) {
            double weight = Math.exp(-0.3 * i); // Días recientes tienen mayor peso
            long dayCompleted = records.stream().filter(r -> isFromDay(r, i)).count();
            long dayExpected = habits.stream().filter(h -> expectedThisDay(h, i)).count();
            double dayRate = dayExpected > 0 ? (double) dayCompleted / dayExpected : 0;

            weightedSum += dayRate * weight;
            totalWeight += weight;
        }

        return new WellnessScore((weightedSum / totalWeight) * 100);
    }
}

// Razón de cambio: lógica de XP y niveles
@Service
public class XpCalculator {
    private static final int[] XP_THRESHOLDS = {0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700};

    public int calculateXpForCheckIn(Habit habit) {
        // Hábitos más frecuentes dan menos XP por cumplimiento
        return 10 + (habit.getWeeklyFrequency() * 2);
    }

    public int calculateLevel(int totalXp) {
        int level = 0;
        for (int i = 0; i < XP_THRESHOLDS.length; i++) {
            if (totalXp >= XP_THRESHOLDS[i]) level = i;
        }
        return Math.min(level, 10);
    }
}

// Razón de cambio: orquestación (PetService ahora solo coordina)
@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;
    private final RecordRepository recordRepository;
    private final HabitRepository habitRepository;
    private final WellnessCalculator wellnessCalculator; // Inyectado
    private final XpCalculator xpCalculator;             // Inyectado
    private final WebSocketGateway webSocketGateway;

    public void recalcularEstado(String userId, Habit completedHabit) {
        List<CompletionRecord> records = recordRepository.getLastNDays(userId, 7);
        List<Habit> habits = habitRepository.findActiveByUserId(userId);

        WellnessScore wellnessScore = wellnessCalculator.calculate(records, habits, 7);
        int xpGained = xpCalculator.calculateXpForCheckIn(completedHabit);
        Pet pet = petRepository.findByUserId(userId);

        Pet updatedPet = pet.addXP(xpGained).updateState(wellnessScore.toState());
        petRepository.save(updatedPet);

        webSocketGateway.emitPetStateChanged(userId, updatedPet);
    }
}
```

**Razones de cambio separadas**: Si el algoritmo de cálculo cambia (ej: de ponderado a simple promedio), solo cambia `WeightedAverageWellnessCalculator`. Si cambia la escala de niveles, solo cambia `XpCalculator`. Si cambia el protocolo WebSocket, solo cambia `WebSocketGateway`.

---

### O — Open/Closed Principle (Principio Abierto/Cerrado)

**Definición**: Las entidades de software deben estar abiertas para extensión pero cerradas para modificación.

**Problema que resuelve en HabitPet**: El sistema de cálculo del bienestar de la mascota podría necesitar diferentes algoritmos en el futuro (ej: un modo estricto donde los días sin registros penalizan más, o un modo principiante que es más permisivo). Sin OCP, cada nuevo algoritmo requeriría modificar el código existente.

**Cómo se aplica**: La interfaz `WellnessCalculator` define el contrato. Nuevos algoritmos se agregan como nuevas clases sin modificar las existentes:

```java
// Contrato — cerrado para modificación
public interface WellnessCalculator {
    WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days);
}

// Implementación 1 — promedio ponderado exponencial (días recientes tienen más peso)
@Service
public class WeightedAverageStrategy implements WellnessCalculator {

    @Override
    public WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days) {
        double weightedSum = 0;
        double totalWeight = 0;
        for (int i = 0; i < days; i++) {
            double weight = Math.exp(-0.3 * i);
            double rate = getDayCompletionRate(records, habits, i);
            weightedSum += rate * weight;
            totalWeight += weight;
        }
        return new WellnessScore((weightedSum / totalWeight) * 100);
    }
}

// Extensión — nueva implementación sin modificar código existente (OCP)
@Service
public class StrictModeStrategy implements WellnessCalculator {

    @Override
    public WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days) {
        // Implementación estricta: un día sin registro cuenta como 0%, no como ausencia
        int totalExpected = habits.stream().mapToInt(Habit::getWeeklyFrequency).sum();
        int totalCompleted = records.size();
        double strictRate = (double) totalCompleted / (totalExpected * ((double) days / 7));
        return new WellnessScore(Math.min(strictRate * 100, 100));
    }
}

// Extensión para usuarios principiantes — sin modificar código existente
@Service
public class BeginnerModeStrategy implements WellnessCalculator {

    @Override
    public WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days) {
        // Modo principiante: si cumplió al menos 1 hábito por día, cuenta como 100%
        long activeDays = records.stream()
                .map(r -> r.getDate().toLocalDate())
                .distinct()
                .count();
        return new WellnessScore(((double) activeDays / days) * 100);
    }
}

// El PetService no cambia cuando se agregan nuevas estrategias
@Service
@RequiredArgsConstructor
public class PetService {
    private final WellnessCalculator wellnessCalculator; // Inyectado por tipo o @Qualifier
    // El resto del código no cambia
}
```

**Por qué importa**: Agregar un nuevo modo de cálculo (ej: "Modo Vacaciones" que pausa la degradación) no requiere modificar `PetService` ni `WeightedAverageStrategy`. Solo se agrega una nueva clase que implementa `WellnessCalculator`.

---

### L — Liskov Substitution Principle (Principio de Sustitución de Liskov)

**Definición**: Los objetos de una superclase deben poder ser reemplazados por objetos de sus subclases sin afectar la correctitud del programa.

**Problema que resuelve en HabitPet**: Si se tienen diferentes tipos de hábitos (hábitos de frecuencia diaria vs hábitos de meta única), deben poder usarse de forma intercambiable en el sistema de cálculo y en la UI.

**Cómo se aplica**: La clase base `Habit` define el comportamiento que todas las variantes deben cumplir. Las subclases no pueden cambiar el contrato:

```java
// Clase base — define el contrato
public abstract class Habit {
    protected final String id;
    protected final String name;
    protected final String userId;
    protected final int weeklyFrequency;

    protected Habit(String id, String name, String userId, int weeklyFrequency) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.weeklyFrequency = weeklyFrequency;
        validate();
    }

    // Contrato: todo Habit puede determinar si debe completarse un día dado
    public abstract boolean shouldCompleteOn(int dayOfWeek);

    // Contrato: todo Habit puede calcular el XP por completarse
    public abstract int getXpReward();

    // Contrato: todo Habit puede validarse a sí mismo
    protected void validate() {
        if (name == null || name.isBlank()) {
            throw new InvalidHabitException("El nombre del hábito es requerido");
        }
        if (weeklyFrequency < 1 || weeklyFrequency > 7) {
            throw new InvalidHabitException("La frecuencia debe estar entre 1 y 7 días por semana");
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUserId() { return userId; }
    public int getWeeklyFrequency() { return weeklyFrequency; }
}

// Hábito de frecuencia semanal — sustituible por Habit
public class FrequencyHabit extends Habit {
    private final List<Integer> preferredDays; // 0=Domingo, 1=Lunes, ...

    public FrequencyHabit(String id, String name, String userId,
                          int weeklyFrequency, List<Integer> preferredDays) {
        super(id, name, userId, weeklyFrequency);
        this.preferredDays = preferredDays;
    }

    @Override
    public boolean shouldCompleteOn(int dayOfWeek) {
        return preferredDays.contains(dayOfWeek);
    }

    @Override
    public int getXpReward() {
        // Hábitos menos frecuentes dan más XP por cumplimiento
        return 10 + (8 - weeklyFrequency) * 2;
    }
}

// Hábito de meta única (completar una vez, no recurrente) — también sustituible por Habit
public class OneTimeHabit extends Habit {
    private final LocalDate targetDate;

    public OneTimeHabit(String id, String name, String userId, LocalDate targetDate) {
        super(id, name, userId, 1);
        this.targetDate = targetDate;
    }

    @Override
    public boolean shouldCompleteOn(int dayOfWeek) {
        // Puede completarse cualquier día hasta la fecha objetivo
        return !LocalDate.now().isAfter(targetDate);
    }

    @Override
    public int getXpReward() {
        // Los hábitos de meta única dan más XP por ser desafíos puntuales
        return 50;
    }
}

// El WellnessCalculator trabaja con Habit (tipo base) — acepta cualquier subtipo
@Service
public class WeightedAverageStrategy implements WellnessCalculator {

    @Override
    public WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days) {
        // Funciona con FrequencyHabit, OneTimeHabit, o cualquier futura subclase de Habit
        // porque solo usa los métodos del contrato de Habit
        int today = LocalDate.now().getDayOfWeek().getValue() % 7;
        long expectedToday = habits.stream().filter(h -> h.shouldCompleteOn(today)).count();
        // ...
        return new WellnessScore(0);
    }
}
```

**Por qué importa**: El `WellnessCalculator` puede recibir una lista de `Habit` que mezcle `FrequencyHabit` y `OneTimeHabit` sin necesidad de saber cuáles son cuáles. Si se agrega un nuevo tipo de hábito (`ChallengeHabit`), funciona automáticamente en el cálculo.

---

### I — Interface Segregation Principle (Principio de Segregación de Interfaces)

**Definición**: Los clientes no deben depender de interfaces que no usan.

**Problema que resuelve en HabitPet**: Si se define una única interfaz `HabitRepository` con todos los métodos posibles (CRUD + estadísticas + queries complejas), los servicios que solo necesitan leer hábitos tienen que depender de métodos de escritura que nunca usan.

**Cómo se aplica**: Las interfaces de repositorio se dividen según el uso:

```java
// MAL — Interfaz monolítica (violación de ISP)
public interface HabitRepository_INCORRECTO {
    Optional<Habit> findById(String id);
    List<Habit> findByUserId(String userId);
    List<Habit> findActiveByUserId(String userId);
    Habit save(Habit habit);
    void delete(String id);
    long countByCategory(String userId, HabitCategory category);
    List<Habit> findHabitsWithMostCompletions(String userId, int limit);
    List<Habit> findHabitsWithLeastCompletions(String userId, int limit);
}

// BIEN — Interfaces segregadas por propósito
public interface HabitReader {
    Optional<Habit> findById(String id);
    List<Habit> findActiveByUserId(String userId);
}

public interface HabitWriter {
    Habit save(Habit habit);
    void delete(String id);
}

public interface HabitStatsReader {
    List<Habit> findHabitsWithMostCompletions(String userId, int limit);
    List<Habit> findHabitsWithLeastCompletions(String userId, int limit);
    long countByCategory(String userId, HabitCategory category);
}

// PetService solo necesita leer hábitos — depende solo de HabitReader
@Service
@RequiredArgsConstructor
public class PetService {
    private final HabitReader habitReader;         // No depende de HabitWriter ni HabitStatsReader
    private final WellnessCalculator wellnessCalculator;
}

// HabitService necesita leer y escribir
@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitReader habitReader;
    private final HabitWriter habitWriter;
}

// StatsService necesita el lector especializado de estadísticas
@Service
@RequiredArgsConstructor
public class StatsService {
    private final HabitStatsReader habitStatsReader;
    private final RecordReader recordReader;
}

// La implementación concreta implementa las tres interfaces
@Repository
public class JpaHabitRepository implements HabitReader, HabitWriter, HabitStatsReader {
    // Implementa todos los métodos
}
```

**Por qué importa**: Cuando se cambia el método `findHabitsWithMostCompletions` (ej: por optimización), solo los servicios que usan `HabitStatsReader` necesitan revisarse. `PetService` no se ve afectado aunque el repositorio cambie.

---

### D — Dependency Inversion Principle (Principio de Inversión de Dependencias)

**Definición**: Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones.

**Problema que resuelve en HabitPet**: Si `PetService` (módulo de alto nivel, contiene la lógica de negocio) depende directamente de `JpaRepository` (módulo de bajo nivel, infraestructura), un cambio de ORM requeriría modificar la lógica de negocio.

**Cómo se aplica**: Spring Boot facilita DIP con su sistema de inyección de dependencias y el uso de `@Configuration` con `@Bean`:

```java
// Abstracción (en la capa de dominio/aplicación)
public interface PetRepository {
    Optional<Pet> findByUserId(String userId);
    Pet save(Pet pet);
}

// Implementación concreta (en la capa de infraestructura)
@Repository
@RequiredArgsConstructor
public class JpaPetRepository implements PetRepository {
    private final PetJpaRepository jpaRepository; // Spring Data JPA

    @Override
    public Optional<Pet> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId)
                .map(Pet::fromPersistence); // Mapeamos de entidad JPA a entidad de dominio
    }

    @Override
    public Pet save(Pet pet) {
        PetEntity saved = jpaRepository.save(pet.toPersistence());
        return Pet.fromPersistence(saved);
    }
}

// Configuración Spring Boot — conecta abstracción con implementación
@Configuration
public class PetConfig {

    // DIP: la configuración conecta la abstracción con la implementación concreta
    @Bean
    public WellnessCalculator wellnessCalculator() {
        return new WeightedAverageStrategy();
    }
}

// PetService (módulo de alto nivel) depende de la ABSTRACCIÓN PetRepository
@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;          // Abstracción, no JpaPetRepository
    private final WellnessCalculator wellnessCalculator; // Abstracción, no WeightedAverageStrategy

    public Pet recalcularEstado(String userId) {
        Pet pet = petRepository.findByUserId(userId)
                .orElseThrow(() -> new PetNotFoundException(userId)); // No sabe que es JPA
        // ...
        return pet;
    }
}
```

**Por qué importa**: Si se cambia de PostgreSQL a MongoDB, solo se crea `MongoDbPetRepository implements PetRepository` y se actualiza la configuración. `PetService` no cambia porque depende de `PetRepository` (la abstracción), no de `JpaPetRepository`.

---

## Parte 2: Patrones GRASP

Los patrones GRASP (General Responsibility Assignment Software Patterns) son principios para asignar responsabilidades a clases y objetos.

---

### 1. Creator (Creador)

**Principio**: Asigna la responsabilidad de crear una instancia de A a la clase B si B agrega objetos de A, o si B contiene o registra objetos de A.

**Aplicación en HabitPet**: `HabitService` es responsable de crear instancias de `Habit` porque es quien los "registra" y "contiene" en el contexto de la aplicación. No se crean hábitos en el controller ni directamente desde el repository.

```java
@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitWriter habitWriter;

    // HabitService es el Creator de Habit porque gestiona su ciclo de vida completo
    public Habit createHabit(String userId, CreateHabitDto dto) {
        Habit habit = new FrequencyHabit( // HabitService crea la instancia
                UUID.randomUUID().toString(),
                dto.getName(),
                userId,
                dto.getWeeklyFrequency(),
                dto.getPreferredDays()
        );
        // Las reglas de negocio se validan en el constructor de Habit (validate())
        return habitWriter.save(habit);
    }
}
```

---

### 2. Information Expert (Experto en Información)

**Principio**: Asigna la responsabilidad a la clase que tiene la información necesaria para cumplirla.

**Aplicación en HabitPet**: La entidad `Pet` es la experta en saber si subió de nivel, porque tiene el XP actual y los umbrales de nivel. El `PetService` no debería contener esa lógica.

```java
public class Pet {
    private static final int[] XP_THRESHOLDS = {0, 100, 250, 450, 700, 1000, 1350, 1750, 2200, 2700, Integer.MAX_VALUE};

    private final String userId;
    private final int xp;
    private final PetState state;
    private final LocalDateTime lastUpdated;

    // Pet es la Information Expert sobre su propio nivel — tiene la info para decidir
    public int getCurrentLevel() {
        int level = 0;
        for (int i = 0; i < XP_THRESHOLDS.length - 1; i++) {
            if (xp >= XP_THRESHOLDS[i] && xp < XP_THRESHOLDS[i + 1]) {
                level = i;
                break;
            }
        }
        return level;
    }

    // Pet sabe si acaba de subir de nivel tras agregar XP
    public LevelUpResult addXP(int amount) {
        int oldLevel = getCurrentLevel();
        Pet newPet = new Pet(userId, xp + amount, state, lastUpdated);
        boolean leveledUp = newPet.getCurrentLevel() > oldLevel;
        return new LevelUpResult(newPet, leveledUp);
    }

    // Pet sabe cómo transformar un WellnessScore en su nuevo estado
    public Pet updateState(PetState newState) {
        return new Pet(userId, xp, newState, LocalDateTime.now());
    }
}
```

---

### 3. Low Coupling (Bajo Acoplamiento)

**Principio**: Minimizar las dependencias entre clases para reducir el impacto de los cambios.

**Aplicación en HabitPet**: Los módulos se comunican a través de eventos de dominio, no mediante referencias directas. El `RecordService` no importa directamente `PetService`; en cambio, publica un evento de dominio.

```java
// Alto acoplamiento (MAL)
@Service
@RequiredArgsConstructor
public class RecordService_INCORRECTO {
    private final RecordRepository recordRepository;
    private final PetService petService;             // Acoplamiento directo
    private final StatsService statsService;          // Acoplamiento directo
    private final NotificationService notifService;   // Acoplamiento directo

    public CompletionRecord checkIn(String userId, String habitId) {
        CompletionRecord record = recordRepository.save(new CompletionRecord(userId, habitId));
        petService.recalcularEstado(userId);          // Dependencia directa
        statsService.invalidateCache(userId);          // Dependencia directa
        notifService.checkAndNotify(userId);           // Dependencia directa
        return record;
    }
}

// Bajo acoplamiento (BIEN) — ApplicationEventPublisher desacopla los módulos
@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final ApplicationEventPublisher eventPublisher; // Solo depende del publisher, no de los consumidores

    public CompletionRecord checkIn(String userId, String habitId) {
        CompletionRecord record = recordRepository.save(new CompletionRecord(userId, habitId));
        // Publicamos el evento — RecordService no sabe quién lo escucha
        eventPublisher.publishEvent(new RecordCompletedEvent(this, userId, habitId, record.getDate()));
        return record;
    }
}

// PetService escucha el evento — sin acoplamiento a RecordService
@Service
@RequiredArgsConstructor
public class PetService {

    @EventListener
    public void onRecordCompleted(RecordCompletedEvent event) {
        recalcularEstado(event.getUserId());
    }
}

// Clase del evento de dominio
public class RecordCompletedEvent extends ApplicationEvent {
    private final String userId;
    private final String habitId;
    private final LocalDate date;

    public RecordCompletedEvent(Object source, String userId, String habitId, LocalDate date) {
        super(source);
        this.userId = userId;
        this.habitId = habitId;
        this.date = date;
    }

    public String getUserId() { return userId; }
    public String getHabitId() { return habitId; }
    public LocalDate getDate() { return date; }
}
```

---

### 4. High Cohesion (Alta Cohesión)

**Principio**: Mantener las responsabilidades de una clase relacionadas y enfocadas.

**Aplicación en HabitPet**: El paquete `pet` agrupa todo lo relacionado con la mascota (estado, XP, niveles, cálculo de bienestar). No mezcla responsabilidades de hábitos ni de autenticación. En Spring Boot, la cohesión se refleja en la organización de paquetes y en la configuración de beans:

```java
// PetConfig — alta cohesión: todo lo del Pet está aquí
@Configuration
public class PetConfig {

    // Solo registra beans relacionados con la mascota
    @Bean
    public WellnessCalculator wellnessCalculator() {
        return new WeightedAverageStrategy();
    }

    @Bean
    public XpCalculator xpCalculator() {
        return new XpCalculator();
    }
}

// Estructura de paquetes que refleja Alta Cohesión:
// com.habitpet.pet/
//   ├── Pet.java                         (entidad de dominio)
//   ├── PetState.java                    (enum de estados)
//   ├── PetService.java                  (orquestador)
//   ├── PetController.java               (entrada HTTP)
//   ├── PetConfig.java                   (configuración de beans)
//   ├── WellnessCalculator.java          (interfaz)
//   ├── WeightedAverageStrategy.java     (implementación)
//   └── repository/
//       ├── PetRepository.java           (abstracción)
//       └── JpaPetRepository.java        (implementación JPA)
```

---

### 5. Controller (Controlador)

**Principio**: Asignar la responsabilidad de manejar eventos del sistema a una clase que represente el sistema global o un caso de uso.

**Aplicación en HabitPet**: Los controllers de Spring son los "Controllers" GRASP: reciben los eventos del sistema (requests HTTP) y los delegan a los services apropiados. No contienen lógica de negocio.

```java
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController { // Controller GRASP — maneja el evento "completar hábito"
    private final RecordService recordService;

    @PostMapping("/checkin")
    public ResponseEntity<RecordResponseDto> checkIn(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid CreateCheckInDto dto) {
        // El controller solo valida y delega — no contiene lógica de negocio
        CompletionRecord record = recordService.checkIn(userId, dto.getHabitId(), dto.getDate());
        return ResponseEntity.ok(RecordResponseDto.fromDomain(record));
    }
}
```

---

### 6. Polymorphism (Polimorfismo)

**Principio**: Cuando un comportamiento varía según el tipo, asignar la responsabilidad al tipo que varía usando polimorfismo.

**Aplicación en HabitPet**: Los diferentes estados de la mascota (CRITICAL, POOR, NEUTRAL, GOOD, EXCELLENT) tienen comportamientos distintos (animaciones, mensajes, penalización de XP). En lugar de `if-else` basados en el estado, cada estado define su propio comportamiento.

```java
// Sin polimorfismo (MAL) — if-else basado en tipo
public String getStateMessage(PetState state) {
    if (state == PetState.CRITICAL) return "¡Tu mascota necesita ayuda urgente!";
    if (state == PetState.POOR)     return "Tu mascota no está bien...";
    if (state == PetState.NEUTRAL)  return "Tu mascota está bien.";
    if (state == PetState.GOOD)     return "¡Tu mascota está contenta!";
    if (state == PetState.EXCELLENT) return "¡Tu mascota está en su mejor momento!";
    throw new IllegalArgumentException("Estado desconocido");
}

// Con polimorfismo (BIEN) — cada estado define su comportamiento
public abstract class PetStateHandler {
    public abstract String getMessage();
    public abstract String getAnimationClass();
    public abstract boolean shouldAlert();
    public abstract double getXpMultiplier();
}

public class CriticalState extends PetStateHandler {
    @Override public String getMessage()         { return "¡Tu mascota necesita ayuda urgente!"; }
    @Override public String getAnimationClass()  { return "animate-pulse-red"; }
    @Override public boolean shouldAlert()       { return true; }
    @Override public double getXpMultiplier()    { return 0.5; } // XP reducido en estado crítico
}

public class ExcellentState extends PetStateHandler {
    @Override public String getMessage()         { return "¡Tu mascota está en su mejor momento!"; }
    @Override public String getAnimationClass()  { return "animate-bounce-gold"; }
    @Override public boolean shouldAlert()       { return false; }
    @Override public double getXpMultiplier()    { return 1.5; } // Bonus de XP en estado excelente
}

// El código cliente no necesita if-else — trabaja con la abstracción
public class PetStateFactory {
    private static final Map<PetState, PetStateHandler> HANDLERS = Map.of(
            PetState.CRITICAL,  new CriticalState(),
            PetState.POOR,      new PoorState(),
            PetState.NEUTRAL,   new NeutralState(),
            PetState.GOOD,      new GoodState(),
            PetState.EXCELLENT, new ExcellentState()
    );

    public static PetStateHandler create(PetState state) {
        return HANDLERS.get(state);
    }
}
```

---

### 7. Pure Fabrication (Fabricación Pura)

**Principio**: Crear una clase que no representa un concepto del dominio pero que mejora el bajo acoplamiento y la alta cohesión.

**Aplicación en HabitPet**: `WellnessCalculator` no existe en el dominio del problema (no es un hábito, ni una mascota, ni un usuario), pero se crea como una clase de servicio para encapsular el algoritmo de cálculo de bienestar.

```java
// WeightedAverageStrategy — Fabricación Pura: no es un concepto del dominio
// pero encapsula una responsabilidad que ninguna clase de dominio debería tener
@Service
public class WeightedAverageStrategy implements WellnessCalculator {

    @Override
    public WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days) {
        // Algoritmo de promedio ponderado exponencial
        // Esta lógica no pertenece a Pet (no es su responsabilidad calcular su propio bienestar)
        // ni a CompletionRecord ni a Habit
        // Es una Fabricación Pura que mejora la cohesión del sistema
        if (records.isEmpty()) return new WellnessScore(0);

        LocalDate today = LocalDate.now();

        double weightedSum = 0;
        double totalWeight = 0;

        for (int i = 0; i < days; i++) {
            final int offset = i;
            LocalDate targetDay = today.minusDays(offset);
            double weight = Math.exp(-0.3 * offset);

            int dayOfWeek = targetDay.getDayOfWeek().getValue() % 7;
            long dayRecords = records.stream()
                    .filter(r -> r.getDate().equals(targetDay))
                    .count();
            long dayHabits = habits.stream()
                    .filter(h -> h.shouldCompleteOn(dayOfWeek))
                    .count();

            double rate = dayHabits > 0 ? (double) dayRecords / dayHabits : 0;
            weightedSum += rate * weight;
            totalWeight += weight;
        }

        return new WellnessScore((weightedSum / totalWeight) * 100);
    }
}
```

---

### 8. Indirection (Indirección)

**Principio**: Asignar responsabilidad a un objeto intermediario para mediar entre componentes, reduciendo el acoplamiento directo.

**Aplicación en HabitPet**: El `ApplicationEventPublisher` de Spring actúa como intermediario entre `RecordService` (quien genera el evento) y `PetService`/`NotificationService`/`StatsService` (quienes reaccionan). Ninguno de ellos se conoce directamente.

```java
// ApplicationEventPublisher como intermediario (Indirection)
@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final ApplicationEventPublisher eventPublisher; // El intermediario

    public CompletionRecord checkIn(String userId, String habitId) {
        CompletionRecord record = recordRepository.save(new CompletionRecord(userId, habitId));
        // RecordService no conoce a PetService, StatsService ni NotificationService
        // Solo conoce al intermediario (ApplicationEventPublisher)
        eventPublisher.publishEvent(new RecordCompletedEvent(this, userId, habitId, record.getDate()));
        return record;
    }
}

// Cada listener reacciona de forma independiente — sin conocer a los demás
@Service
@RequiredArgsConstructor
public class NotificationService {

    @EventListener
    public void onRecordCompleted(RecordCompletedEvent event) {
        checkAndNotify(event.getUserId());
    }
}

@Service
@RequiredArgsConstructor
public class StatsService {

    @EventListener
    public void onRecordCompleted(RecordCompletedEvent event) {
        invalidateCache(event.getUserId());
    }
}
```

---

### 9. Protected Variations (Variaciones Protegidas)

**Principio**: Identificar puntos de variación inestables y crear una interfaz estable alrededor de ellos.

**Aplicación en HabitPet**: El punto de variación es "cómo se calcula el bienestar de la mascota". La interfaz `WellnessCalculator` protege al resto del sistema de este cambio. Si mañana el algoritmo cambia, el punto de variación está aislado detrás de la interfaz.

Otro punto de variación protegido: la persistencia de datos. La interfaz `PetRepository` protege la lógica de negocio de los detalles de JPA/PostgreSQL. Si se cambia la base de datos, el punto de variación (la implementación del repositorio) está aislado.

```java
// Punto de variación protegido #1: algoritmo de bienestar
// La interfaz es estable — las implementaciones pueden cambiar libremente
public interface WellnessCalculator {
    WellnessScore calculate(List<CompletionRecord> records, List<Habit> habits, int days);
}

// Punto de variación protegido #2: persistencia
// PetService no sabe si estamos usando JPA, MongoDB o un repositorio en memoria
public interface PetRepository {
    Optional<Pet> findByUserId(String userId);
    Pet save(Pet pet);
}

// La configuración es el único lugar que conoce la implementación concreta
@Configuration
public class PetConfig {

    @Bean
    public WellnessCalculator wellnessCalculator() {
        // Si el algoritmo cambia, solo se modifica esta línea
        return new WeightedAverageStrategy();
        // return new StrictModeStrategy(); // Cambio de estrategia sin tocar PetService
    }

    @Bean
    public PetRepository petRepository(PetJpaRepository jpaRepository) {
        // Si la DB cambia, solo se modifica esta línea
        return new JpaPetRepository(jpaRepository);
        // return new MongoDbPetRepository(mongoTemplate); // Cambio de DB sin tocar PetService
    }
}
```
