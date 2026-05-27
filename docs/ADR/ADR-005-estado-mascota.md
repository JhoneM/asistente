# ADR-005: Motor de Cálculo del Estado de la Mascota

## Estado
Aceptada

## Contexto

El núcleo del sistema HabitPet es el algoritmo que determina el estado de la mascota (CRITICAL, POOR, NEUTRAL, GOOD, EXCELLENT) en función del cumplimiento de hábitos del usuario.

Las decisiones técnicas a tomar son:
1. ¿Qué variables considera el algoritmo?
2. ¿Qué período de tiempo se analiza?
3. ¿Cómo se pondera el cumplimiento reciente vs el histórico?
4. ¿Cuándo se ejecuta el recálculo?
5. ¿Cómo se hace extensible el algoritmo para futuras variantes?

Esta decisión impacta directamente en la experiencia del usuario: un algoritmo que cambia el estado muy rápido (un día sin check-in y la mascota pasa a CRITICAL) generará frustración. Uno que cambia muy lento hará que la mascota no se sienta reactiva.

## Decisión

Se implementa el motor de cálculo usando el **patrón Strategy** con una implementación default de **promedio ponderado exponencial** sobre los últimos 7 días. El estado se recalcula **síncronamente en cada check-in** y el resultado se persiste en la tabla `pets`.

### Algoritmo de Promedio Ponderado Exponencial

Para cada uno de los últimos 7 días:
1. Se calcula la tasa de cumplimiento del día: `completados / esperados`
2. Se aplica un peso decreciente exponencialmente: `peso(i) = e^(-0.3 * i)` donde `i=0` es hoy
3. El bienestar final es: `suma(tasa_i * peso_i) / suma(peso_i) * 100`

Umbrales de estado:
- 0–20% → CRITICAL
- 21–40% → POOR
- 41–60% → NEUTRAL
- 61–80% → GOOD
- 81–100% → EXCELLENT

Los días en que no hay hábitos esperados (ej: un hábito de frecuencia 3 días en un día que no corresponde) no cuentan ni positiva ni negativamente.

### Cuándo se recalcula

El estado se recalcula sincrónicamente después de cada check-in exitoso. El nuevo estado se persiste en `pets.state` para evitar recalcularlo en cada consulta al dashboard.

## Justificación

**Patrón Strategy para el algoritmo**:
- El algoritmo de cálculo es el punto de variación más evidente del sistema. Con Strategy, nuevas variantes del algoritmo (modo estricto, modo principiante, modo académico con calendario) se agregan como nuevas clases sin modificar el código existente (OCP)
- Facilita el testing: cada estrategia se puede testear de forma aislada con datos controlados
- El patrón Strategy es uno de los requeridos académicamente en el proyecto

**Ventana de 7 días**:
- 7 días es suficiente para capturar la tendencia reciente sin ser demasiado permisivo con el historial
- Una ventana más corta (ej: 3 días) haría el sistema demasiado reactivo: un fin de semana descuidado dañaría demasiado a la mascota
- Una ventana más larga (ej: 30 días) haría que la mascota tarde demasiado en recuperarse, reduciendo la motivación

**Ponderación exponencial vs promedio simple**:
- La ponderación exponencial da más peso a los días recientes: si el usuario cumplió bien toda la semana pero falló ayer, la mascota debería reflejar ese fallo más que un promedio simple
- El factor `e^(-0.3 * i)` fue elegido empíricamente: el día de hoy tiene peso 1.0, ayer 0.74, anteayer 0.55. El día de hace 7 días tiene peso 0.12, lo que significa que tiene poco impacto en el resultado final

**Recálculo síncrono en cada check-in**:
- Alternativa evaluada: calcular el estado en tiempo real al consultar el dashboard. Descartada porque añade latencia en cada carga del dashboard (que ocurre con más frecuencia que los check-ins)
- El recálculo síncrono mantiene el estado siempre actualizado, con el costo de añadir ~50ms al tiempo de respuesta del endpoint de check-in (aceptable)

## Alternativas Consideradas

**Promedio simple de los últimos 7 días**:
- Más sencillo de entender. Descartado porque trata igual el cumplimiento de hace 7 días que el de hoy, lo que no captura bien la tendencia reciente del usuario.

**Cálculo basado en racha (streak-based)**:
- La mascota mejoraría solo si el usuario mantiene una racha perfecta. Descartado porque es demasiado punitivo: romper la racha un día (por enfermedad, por ejemplo) resetearía todo el progreso.

**Cálculo asíncrono (job nocturno)**:
- Calcular el estado una vez por día en un job programado. Descartado porque la mascota no reaccionaría en tiempo real al check-in del usuario, eliminando el feedback inmediato que es el núcleo de la experiencia de usuario.

**Machine Learning para predecir el estado**:
- Descartado por complejidad. El dominio no tiene suficientes datos para entrenar un modelo en el MVP y la complejidad de implementación es excesiva para el contexto universitario.

## Consecuencias

**Positivas**:
- El patrón Strategy hace que el algoritmo sea extensible sin modificar el código existente
- La persistencia del estado calculado reduce la latencia del dashboard
- La ponderación exponencial crea una experiencia de usuario más intuitiva (los días recientes importan más)

**Negativas**:
- El algoritmo de promedio ponderado exponencial es menos intuitivo de explicar al usuario que un promedio simple. Mitigación: el algoritmo es un detalle de implementación; la UI muestra el resultado (el estado de la mascota) sin exponer la fórmula
- Si el usuario no tiene hábitos creados, el denominador del cálculo es 0. Caso especial: sin hábitos, el estado se mantiene en NEUTRAL (la mascota no mejora ni empeora si no hay nada que trackear)
