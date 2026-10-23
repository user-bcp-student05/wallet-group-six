---
name: software-testing-design
description: Procedimiento para la etapa de análisis y diseño de pruebas. A partir del Issue, las specs aprobadas y el contrato de API, define casos de prueba funcionales, de performance y de seguridad que validan cada criterio de aceptación con una cobertura de diseño mínima del 95%, y los presenta en una tabla indicando cuáles son automatizables y en qué capa (backend o frontend) se validan. Genera o actualiza el archivo test-cases.md, que es la fuente de entrada del skill software-testing-automation. USE FOR - diseñar el plan y los casos de prueba antes de automatizar. DO NOT USE FOR - escribir el código de las pruebas (usar software-testing-automation), pruebas unitarias ni ejecutar pruebas.
---
# Análisis y diseño de pruebas (funcional, performance y seguridad)

Objetivo: transformar los criterios de aceptación en casos de prueba trazables, completos y priorizados, y decidir qué se automatiza y en qué capa. Este skill produce diseño, no código ni resultados de ejecución.

**Salida: crea o actualiza `test-cases.md`.** Ese archivo es la única fuente de entrada del skill `software-testing-automation`: lo que no esté en `test-cases.md` no se automatiza. Por eso su formato es estable y legible por máquina (ver "Archivo test-cases.md").

## Precondiciones

No diseñar casos si faltan:

```text
Issue con criterios de aceptación numerados (fuente de verdad del alcance)
specs aprobadas (openspec/changes/<cambio>/specs/**/spec.md) con escenarios WHEN/THEN
contrato de API (services/api/*.yaml) para los casos de backend
descripción de la interfaz o criterios de UI para los casos de frontend (si aplica)
```

Si falta alguna fuente, indícalo y marca los casos afectados como `NO VERIFICABLE` con la información necesaria. No inventar campos, códigos de estado, umbrales ni reglas de negocio.

## Procedimiento

1. **Inventario de criterios.** Enumera cada criterio de aceptación del Issue (CA-1, CA-2, ...) y cada escenario de las specs. Si un escenario no tiene criterio, regístralo igualmente; si un criterio no tiene escenario, márcalo como brecha de especificación.
2. **Análisis de cada criterio.** Identifica entradas, precondiciones, salidas observables, reglas, límites y errores posibles. Identifica en qué capa se observa (backend por API, frontend por UI, ambas).
3. **Técnicas de diseño.** Aplica según corresponda: partición de equivalencia, análisis de valores límite (mínimo, máximo, justo dentro y justo fuera), tablas de decisión para reglas combinadas, transición de estados para flujos con estados, y casos negativos por cada validación.
4. **Casos funcionales.** Por cada criterio, al menos un caso positivo y, cuando exista una regla de validación o error, al menos un negativo y los de borde. Incluye integración (varios componentes) y E2E (flujo completo) solo cuando el criterio lo exija.
5. **Casos de seguridad.** Evalúa y diseña lo aplicable (ver catálogo). Si una categoría no aplica, justifícalo.
6. **Casos de performance.** Evalúa si el criterio o cambio afecta latencia, throughput o recursos. Diseña los casos aplicables (ver catálogo) con carga, duración y métrica. Usa umbrales existentes del Issue, spec o baseline; si no existen, declara `umbral no definido`, propone qué medir y no inventes aprobación.
7. **Automatización y capa.** Para cada caso decide si es automatizable (`Sí`, `Parcial`, `No`), la capa donde se valida (`Backend`, `Frontend`, `Ambas`) y el nivel (`Componente`, `Integración`, `E2E`). Justifica los `No`.
8. **Matriz de trazabilidad y cobertura.** Calcula la cobertura de diseño (ver abajo). Si es menor al 95%, agrega casos o documenta la brecha y su causa antes de entregar.
9. **Escritura de `test-cases.md`.** Crea el archivo o, si ya existe, actualízalo según las reglas de actualización.
10. **Gate.** Verifica la lista de control final y entrega.

## Catálogo mínimo de casos

### Funcionales
- Camino feliz por criterio.
- Negativos: campos faltantes, tipos inválidos, formatos incorrectos, valores fuera de rango, reglas de negocio incumplidas.
- Límites: valor mínimo, máximo, justo dentro y justo fuera.
- Estados y flujos: transiciones válidas e inválidas, repetición de solicitudes, orden de pasos.
- Integración: contrato entre componentes y manejo de fallas de dependencias.
- E2E: flujo completo del usuario o consumidor.

### Seguridad (adaptar al alcance; referencia OWASP API Security Top 10)
- Autenticación: token ausente, inválido, expirado, con esquema incorrecto.
- Autorización: acceso a recursos de otro usuario (BOLA/IDOR), funciones sin permiso.
- Validación y inyección: SQL/NoSQL, XSS, caracteres de control, cuerpos sobredimensionados, campos no esperados (mass assignment).
- Exposición de datos: respuestas y mensajes de error sin datos sensibles, enmascarado de datos, ausencia de secretos y datos personales en logs.
- Integridad y repetición: reintentos, doble envío, manipulación de montos o destinos.
- Configuración: cabeceras de seguridad, métodos HTTP no permitidos, CORS, TLS en ambientes de prueba.
- Límite de uso: protección ante abuso (rate limiting) cuando el requisito exista.

### Performance (solo con carga proporcional al riesgo y ambiente aislado)
- Línea base: latencia p50/p95/p99 de un usuario sobre el endpoint crítico.
- Carga: volumen esperado sostenido durante una duración definida.
- Estrés: incremento hasta degradación para identificar el límite.
- Resistencia (soak): carga moderada prolongada para detectar fugas.
- Pico: aumento súbito de solicitudes.
- Concurrencia: solicitudes simultáneas sobre el mismo recurso (consistencia e idempotencia).
- Cada caso indica escenario, carga/volumen, duración, métrica y criterio de aceptación o `umbral no definido`.

## Cobertura de diseño (objetivo ≥ 95%)

La cobertura se mide sobre el **diseño**, no sobre el código ni sobre la ejecución:

```text
Cobertura de criterios  = criterios con ≥1 caso positivo y sus negativos/bordes aplicables / total de criterios
Cobertura de escenarios = escenarios de la spec con ≥1 caso / total de escenarios
Cobertura de condiciones = particiones y límites con caso / particiones y límites identificados
```

- Meta: cada métrica ≥ 95%. Lo restante debe estar listado como brecha con causa (por ejemplo, requisito sin umbral, escenario no observable) y responsable.
- Un criterio solo cuenta como cubierto si tiene casos para todas sus categorías aplicables.
- Nunca presentes la cobertura de diseño como cobertura de código ni como resultado de pruebas ejecutadas.

## Archivo test-cases.md

Ubicación: `openspec/changes/<cambio>/test-cases.md`, junto a las specs del cambio. Si el flujo del equipo define otra ruta, respétala y indícala en el encabezado del archivo.

### Estructura obligatoria

```markdown
# Casos de prueba - <cambio o Issue>

- Issue: <enlace o número>
- Specs: <rutas>
- Contrato: <ruta OpenAPI>
- Última actualización: <fecha>

## 1. Resumen
<alcance en una o dos frases>

## 2. Casos de prueba
<tabla de casos>

## 3. Matriz de trazabilidad
<criterio -> IDs de casos, cobertura por criterio>

## 4. Cobertura y brechas
<tres métricas y brechas con causa>

## 5. Resumen de automatización
<totales Sí/Parcial/No y distribución por capa y nivel>

## 6. Datos de prueba, supuestos y riesgos

## 7. Historial de cambios
<fecha, IDs agregados, modificados, obsoletos>
```

### Reglas de actualización

- Si el archivo no existe, créalo completo.
- Si existe, actualízalo: conserva los IDs existentes y nunca los renumeres ni los reutilices.
- Agrega casos nuevos con el siguiente ID libre; modifica los casos cuyo criterio, spec o contrato cambió.
- Un caso que ya no aplica se marca `OBSOLETO` con motivo; no se borra.
- No sobrescribas las columnas `Prueba automatizada` ni `Estado` de casos ya trabajados por el skill de automatización; solo vuelve a `NO EJECUTADO` el estado de un caso cuyo comportamiento esperado cambió, y regístralo en el historial.
- Recalcula las secciones 3 a 5 en cada actualización y registra el cambio en la sección 7.

### Contrato con el skill de automatización

El skill `software-testing-automation` automatiza únicamente las filas de la sección 2 que cumplan simultáneamente:

```text
Automatizable = Sí o Parcial
Capa = Backend (o Ambas, solo la parte de backend)
Nivel = Componente, Integración o E2E
Estado distinto de OBSOLETO
```

Para que eso sea posible, cada fila debe ser autosuficiente: precondiciones, datos, petición (método, ruta, headers relevantes y cuerpo) y resultado esperado (status, código de error y campos del cuerpo) deben estar escritos sin ambigüedad en `Descripción y resultado esperado`. Si un dato no puede derivarse del contrato o de la spec, el caso se marca `NO VERIFICABLE` y no se automatiza hasta resolverlo.

## Formato de salida: tabla de casos

La sección 2 de `test-cases.md` es siempre una tabla. Columnas obligatorias:

| ID | Tipo | Criterio / Escenario | Descripción y resultado esperado | Prioridad | Automatizable | Capa | Nivel | Herramienta sugerida | Prueba automatizada | Estado |
|---|---|---|---|---|---|---|---|---|---|---|
| TC-001 | Funcional (positivo) | CA-1 / payment-confirmation: Revisión previa | Datos y acción; resultado esperado observable | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO EJECUTADO |
| TC-014 | Funcional (UI) | CA-3 | Pasos en pantalla; resultado esperado visible | Media | Sí | Frontend | E2E | Playwright / Cypress | _(pendiente)_ | NO EJECUTADO |
| TC-025 | Seguridad | CA-2 | Token expirado -> 401; sin datos sensibles en el cuerpo | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO EJECUTADO |
| TC-031 | Performance | CA-5 | Carga: N usuarios, duración, p95; umbral no definido | Media | Parcial | Backend | Integración | k6 / JMeter | _(pendiente)_ | NO EJECUTADO |
| TC-040 | Funcional (manual) | CA-7 | Verificación operativa o contable | Baja | No (justificar) | Ambas | E2E | Manual | N/A | NO EJECUTADO |

Reglas de la tabla:
- `Tipo`: Funcional (positivo, negativo, límite, integración, E2E), Seguridad o Performance.
- `Automatizable`: `Sí`, `Parcial` (indicar qué parte) o `No` (justificar).
- `Capa`: `Backend` (se valida por API), `Frontend` (se valida en la interfaz), `Ambas`.
- `Nivel`: Componente, Integración o E2E. Las pruebas unitarias no forman parte de este diseño: las cubre otro skill.
- `Estado`: `NO EJECUTADO` hasta observar la ejecución; solo entonces `APROBADO` o `FALLIDO` con evidencia. `OBSOLETO` para casos que ya no aplican.
- `Prueba automatizada`: la completa el skill de automatización con `Clase#método`; este skill la deja como `_(pendiente)_` o `N/A`.
- Herramientas: usa las ya aprobadas en el proyecto (REST Assured + Quarkus para API; para UI, la herramienta definida por el equipo). Si no hay herramienta definida para una capa, indícalo como pendiente.

Además de la tabla, `test-cases.md` incluye (secciones 3 a 6):

1. **Matriz de trazabilidad** criterio -> casos (IDs), con cobertura por criterio.
2. **Resumen de cobertura** con las tres métricas y las brechas.
3. **Resumen de automatización**: total de casos, `Sí`/`Parcial`/`No` y distribución por capa y nivel.
4. **Supuestos, datos de prueba y riesgos.**

## Entrega

El único entregable es `test-cases.md` (crear o actualizar), con la estructura de la sección "Archivo test-cases.md". Al finalizar, informa en el chat un resumen breve: casos agregados, modificados y obsoletos, cobertura y brechas.

## Datos y ambientes

- Datos sintéticos; nunca datos reales ni credenciales reales.
- Pruebas de performance y de seguridad solo en ambientes aislados o de pruebas, nunca en producción.
- Las pruebas de seguridad no incluyen explotación real, DoS ni herramientas ofensivas sin autorización; se limitan a verificar controles.

## Reglas

- no inventar criterios, campos, códigos ni umbrales;
- no omitir criterios: cada uno aparece en la matriz con casos o con brecha justificada;
- no afirmar cobertura de código ni resultados de ejecución;
- no marcar `Automatizable = Sí` en un caso que no se pueda observar por la capa indicada;
- no mezclar pruebas unitarias en este diseño;
- no escribir ni modificar código de pruebas o de producción en esta etapa; solo se escribe `test-cases.md`;
- no ampliar el alcance más allá del Issue.

## Lista de control (gate)

- cada criterio de aceptación tiene casos de todas sus categorías aplicables;
- cobertura de criterios, escenarios y condiciones ≥ 95%, o brechas documentadas con causa;
- existen casos funcionales, de seguridad y de performance, o la ausencia de una categoría está justificada;
- la tabla indica para cada caso si es automatizable y en qué capa (backend o frontend) y nivel;
- los umbrales de performance provienen de una fuente o se declaran `umbral no definido`;
- todos los estados son `NO EJECUTADO` (o `OBSOLETO` con motivo);
- `test-cases.md` existe con las 7 secciones, y los IDs existentes se conservaron en una actualización;
- cada caso marcado `Sí`/`Parcial` en Backend es autosuficiente para que el skill de automatización lo implemente sin suposiciones;
- la matriz de trazabilidad y el resumen de automatización están incluidos.

## Relación con otros skills

- `software-testing-automation`: lee `test-cases.md` y automatiza los casos funcionales de API marcados como automatizables en `Backend` (componente, integración, E2E); completa la columna `Prueba automatizada` y el `Estado` con resultados reales.
- Pruebas unitarias: otro skill; no se diseñan aquí.
- Performance, seguridad y UI: requieren herramienta y ambiente definidos; si no existen, los casos quedan diseñados con la herramienta pendiente.
