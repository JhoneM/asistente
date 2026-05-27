# ADR-001: Stack de Tecnologías Frontend

## Estado
Aceptada

## Contexto

El proyecto HabitPet requiere una interfaz de usuario web que:
- Muestre una mascota con animaciones basadas en su estado
- Se actualice en tiempo real cuando cambia el estado de la mascota (sin recargar la página)
- Sea responsiva (funcione correctamente en desktop y mobile)
- Permita gestionar hábitos (CRUD) y registrar check-ins

El equipo tiene experiencia previa con JavaScript y algunos miembros han trabajado con Vue o React superficialmente. El contexto es un proyecto universitario de 10 semanas con un equipo de 4 personas.

## Decisión

Se utiliza **React 18 con TypeScript** como framework de interfaz de usuario, **TailwindCSS** para estilos, y **Zustand** para el estado global.

## Justificación

**React 18 + TypeScript**:
- React es el framework más adoptado en la industria (mayor disponibilidad de recursos educativos, documentación, ejemplos y ayuda de la comunidad)
- TypeScript provee tipado estático que detecta errores en tiempo de compilación, especialmente útil para el manejo del estado de la mascota y los DTOs de la API
- El modelo de componentes de React es ideal para encapsular la mascota como un componente independiente que reacciona a props
- React Hooks (`useState`, `useEffect`, `useRef`) simplifican el manejo de estado local de la mascota y la integración con WebSocket

**TailwindCSS**:
- Permite construir interfaces responsivas sin escribir CSS personalizado para cada componente
- El bundle final solo incluye las clases usadas (purging automático), lo que resulta en CSS de producción mínimo
- Las clases utilitarias se leen directamente en el JSX, eliminando el context-switching entre archivos CSS y componentes

**Zustand**:
- API mínima: el store se define en pocas líneas, sin boilerplate de Redux (actions, reducers, selectors separados)
- Soporta actualizaciones parciales del estado, lo que es útil cuando el WebSocket actualiza solo el estado de la mascota sin alterar la lista de hábitos
- Compatible con React DevTools para debugging

## Alternativas Consideradas

**Next.js**: Se descartó porque el SSR (Server-Side Rendering) agrega complejidad de despliegue que no aporta beneficios al MVP. La mascota actualizada en tiempo real requiere estado del cliente, lo que hace que SSR sea irrelevante para el componente principal. Una SPA simple es suficiente para el MVP.

**Vue.js**: Descartado a pesar de su curva de aprendizaje menor porque el equipo tiene más exposición a React y la cantidad de recursos educativos disponibles para React supera a los de Vue.

**Redux**: Descartado por el overhead de boilerplate. El estado del MVP (usuario autenticado + mascota + lista de hábitos) no justifica la complejidad de Redux.

**Angular**: Descartado por la curva de aprendizaje significativa (decoradores de módulos, inyección de dependencias del lado del cliente, RxJS obligatorio) que haría inviable el timeline del proyecto.

## Consecuencias

**Positivas**:
- El tipado estático de TypeScript reduce bugs en los DTOs y el manejo del estado de la mascota
- TailwindCSS acelera el desarrollo de la UI responsiva
- El ecosistema de React facilita encontrar soluciones a problemas comunes

**Negativas**:
- El bundle inicial de React + TailwindCSS (purged) es levemente mayor que el de Svelte, pero aceptable para el contexto (< 200 KB gzipped)
- Los miembros del equipo sin experiencia en React necesitan un período de adaptación (contemplado en la Fase 0 del proyecto)
