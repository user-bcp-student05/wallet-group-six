# Casos de prueba - HU-01 Registro y activación del MVP de wallet

- Issue: [#1 HU-01 - Registro y activación del MVP de wallet](https://github.com/user-bcp-student05/wallet-group-six/issues/1) (baseline: `issues/HU-01-Onboarding.md`)
- Cambio OpenSpec: `openspec/changes/hu-01-wallet-onboarding`
- Specs: `specs/wallet-onboarding/spec.md`, `specs/onboarding-measurement/spec.md`
- Contrato: no existe contrato de API para el onboarding. `services/api/ux-contact-transfer-v1.yaml` solo expone `/affiliation-wallet`, sin pasos, estado KYC ni `wallet_active`
- Última actualización: 2026-10-23

## 1. Resumen

Diseño de 66 casos funcionales, de seguridad y de performance para los 7 criterios de aceptación del Issue #1 y los 32 escenarios de las specs `wallet-onboarding` (13) y `onboarding-measurement` (19).

El repositorio no tiene frontend, ni contrato de API, ni implementación del onboarding, y la política KYC aprobada es un insumo externo. Por eso los casos están diseñados contra las specs y el Issue, y quedan `NO VERIFICABLE` hasta que existan contrato, política KYC y canal de eventos y reportes. Solo los casos documentales y de performance están en `NO EJECUTADO`.

## 2. Casos de prueba

Convenciones: las operaciones de consulta, avance, cierre y activación del onboarding, y los reportes, **no tienen contrato**; en cada caso se describen como "operación de ..." y se concretarán (método, ruta, headers, cuerpo) cuando exista el contrato. **Política de prueba** = política KYC sintética con pasos obligatorios `P1..Pn` y verificaciones requeridas, definida en el entorno de pruebas (nunca documentos reales). **Usuario U** = usuario sintético con identificador seudónimo. Todas las horas son UTC salvo que se indique `America/Lima`.

| ID | Tipo | Criterio / Escenario | Descripción y resultado esperado | Prioridad | Automatizable | Capa | Nivel | Herramienta sugerida | Prueba automatizada | Estado |
|---|---|---|---|---|---|---|---|---|---|---|
| TC-001 | Funcional (UI) | CA-1 / wallet-onboarding: Recorrido guiado | U inicia el onboarding con la política de prueba: la pantalla muestra la secuencia completa de pasos requeridos y distingue el paso actual. Falta: política KYC y frontend | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-002 | Funcional (positivo) | CA-1 / wallet-onboarding: Recorrido guiado | Operación de consulta del estado del onboarding de U devuelve lista de pasos requeridos, paso actual, estado y acciones permitidas. Falta: contrato de la operación | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-003 | Funcional (UI) | CA-1 / wallet-onboarding: Recorrido guiado | En estado de corrección y en estado de revisión pendiente, la pantalla sigue mostrando la secuencia y el paso actual. Falta: política KYC y frontend | Media | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-004 | Funcional (negativo) | CA-2 / wallet-onboarding: Dato obligatorio ausente | Operación de avance de un paso sin un dato obligatorio -> el avance se bloquea; la respuesta indica qué falta y cómo completarlo según la política. Falta: contrato y mensajes de la política | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-005 | Funcional (negativo) | CA-2 / wallet-onboarding: Dato inválido corregible | Avance con dato inválido que la política permite corregir -> bloqueado con la corrección requerida; tras corregirlo el avance se permite. Falta: contrato y reglas de validación | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-006 | Funcional (negativo) | CA-2 / wallet-onboarding: Bloqueo y corrección | Avance con dato inválido que la política no permite corregir -> bloqueado y no se ofrece una acción de corrección que la política no permite. Falta: política KYC | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-007 | Funcional (UI) | CA-2 / wallet-onboarding: Dato obligatorio ausente | La pantalla bloquea "Continuar", indica el dato faltante o inválido y cómo corregirlo, y solo avanza al satisfacer la validación | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-008 | Funcional (negativo) | CA-2 | `/affiliation-wallet` con H, documentNumber, phone o email vacío o no Base64 -> 400, `code="HB0305"`. Falta: confirmar que `/affiliation-wallet` es el paso de captura del onboarding y su vínculo con pasos y KYC | Media | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-009 | Funcional (negativo) | CA-2 | `/affiliation-wallet` con H y documentNumber `RkFJTF80MjI=` -> 422, `code="H9999"`; la wallet no queda activa. Falta: vínculo con `wallet_active` | Media | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-010 | Seguridad (validación) | CA-2 | Campos de captura con caracteres de control, `'; DROP TABLE x;--` y `<script>alert(1)</script>` -> rechazados o tratados como texto, sin ejecución ni reflejo en la respuesta. Falta: contrato | Media | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-011 | Seguridad (exposición de datos) | CA-2 | Mensajes de error y de corrección sin documentos, números de documento, teléfono ni correo del usuario, ni trazas de pila. Falta: contrato | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-012 | Funcional (positivo) | CA-3 / wallet-onboarding: Política disponible | Con política vigente aprobada y configurada, los documentos y verificaciones exigidos coinciden exactamente con la política, sin requisitos ajenos. Falta: política KYC aprobada | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-013 | Funcional (negativo) | CA-3 / wallet-onboarding: Política no disponible | Sin política aprobada o sin su configuración, la verificación de preparación de lanzamiento falla y no se habilita ninguna activación que eluda KYC. Falta: mecanismo de configuración | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-014 | Funcional (manual) | CA-3 / wallet-onboarding: Política no disponible | Checklist de lanzamiento: la política aprobada por Cumplimiento está identificada, vigente y configurada; un fixture de prueba no se acepta como aprobación real | Alta | No (revisión documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-015 | Funcional (concurrencia) | CA-3 | Si la versión de política cambia durante la evaluación, el sistema reevalúa y no activa con una lectura obsoleta (design.md, decisión 1). Falta: contrato y mecanismo de versionado | Media | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-016 | Funcional (positivo) | CA-3 / wallet-onboarding: Flujo completo y aprobado | Pasos obligatorios completos, datos válidos y verificaciones KYC aprobadas -> `wallet_active` persistido y punto de entrada a pagos de bajo monto habilitado | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-017 | Funcional (negativo) | CA-3 / wallet-onboarding: Paso obligatorio pendiente | Un paso obligatorio incompleto con las demás verificaciones aprobadas -> sin activación y sin acceso a pagos | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-018 | Funcional (negativo) | CA-3 / wallet-onboarding: Caso pendiente de revisión | Datos enviados con revisión KYC pendiente -> estado pendiente, siguiente paso permitido informado, sin activación ni acceso a pagos | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-019 | Funcional (negativo) | CA-3 / wallet-onboarding: Verificación no aprobada | Captura completa pero una verificación requerida sin aprobación -> la wallet sigue sin activar y la captura no se presenta como aprobación KYC | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-020 | Funcional (negativo) | CA-3 / wallet-onboarding: Activación aún no persistida | Condiciones KYC satisfechas pero fallo del commit de `wallet_active` -> el onboarding no se contabiliza como finalizado ni se presenta como activado | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-021 | Funcional (UI) | CA-3 / wallet-onboarding: Flujo completo y aprobado | El acceso a pagos solo aparece tras confirmarse la activación persistida; antes no se muestra ni hay activación optimista | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-022 | Funcional (E2E) | CA-3 / wallet-onboarding: Flujo completo y aprobado | Flujo completo de U: iniciar, completar pasos, aprobación KYC, activación y acceso a pagos | Alta | Sí | Backend | E2E | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-023 | Seguridad (integridad) | CA-3 | Un cliente que envía `wallet_active=true` o una aprobación KYC propia no activa la wallet; la aprobación solo procede de la fuente de cumplimiento (design.md, decisión 1) | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-024 | Seguridad (autorización) | CA-3 | Un usuario distinto de U intenta consultar o avanzar el onboarding de U -> rechazado (BOLA) | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-025 | Seguridad (autenticación) | CA-3 | Operaciones del onboarding sin token, con token inválido o expirado -> 401 | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-026 | Seguridad (exposición de datos) | CA-3 | La respuesta de consulta de estado no incluye documentos ni resultados KYC detallados de U | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-027 | Funcional (concurrencia) | CA-3 | Dos activaciones concurrentes de U -> un único estado activo y una sola ocurrencia de `onboarding_activated` | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-028 | Funcional (idempotencia) | CA-3 | Respuesta perdida tras el commit de la activación y reintento con la misma clave -> mismo resultado, sin duplicados | Media | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-029 | Funcional (negativo) | CA-3 / wallet-onboarding: Registro sin límites por proveedor | Las respuestas del onboarding no incluyen límites numéricos por proveedor | Media | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-030 | Funcional (UI) | CA-3 / wallet-onboarding: Registro sin límites por proveedor | Al recorrer el onboarding hasta la activación no se muestran límites numéricos por proveedor ni se presentan como garantizados | Media | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-031 | Funcional (positivo) | CA-5 / wallet-onboarding: Pausa y reanudación | U selecciona "Comenzar", pausa y reanuda -> se conserva el timestamp original y se devuelven el paso actual y las acciones permitidas | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-032 | Funcional (UI) | CA-5 / wallet-onboarding: Pausa y reanudación | Al reanudar, la pantalla muestra el paso actual y las acciones permitidas, sin tratarlo como nuevo inicio | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-033 | Funcional (idempotencia) | CA-5 | Dos "Comenzar" de U, secuenciales o concurrentes -> un único inicio con timestamp y atribución del primero | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-034 | Funcional (evento) | CA-4 / onboarding-measurement: Inicio y reanudación | Primer "Comenzar", pausa y reanudación -> un solo `onboarding_started` con el timestamp del primer inicio | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-035 | Funcional (evento) | CA-4 / onboarding-measurement: Pasos, activación y cierre | Dos pasos completados, `wallet_active` persistido y un cierre -> 2 `onboarding_step_completed`, 1 `onboarding_activated` y 1 `onboarding_closed`, cada uno con su ocurrencia | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-036 | Funcional (idempotencia) | CA-4 / onboarding-measurement: Duplicación de una misma ocurrencia | La misma ocurrencia de inicio, paso, activación o cierre procesada más de una vez -> un solo evento en la muestra analítica | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-037 | Funcional (evento) | CA-4 / onboarding-measurement: Cierre sin activación | Cierre sin `wallet_active` -> `onboarding_closed` sin `onboarding_activated` ni finalización atribuida | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-038 | Seguridad (privacidad) | CA-4 / onboarding-measurement: Inspección de eventos | Muestra de los 4 tipos de evento: cada uno con timestamp UTC e identificador seudónimo, sin documentos ni datos sensibles | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-039 | Seguridad (exposición de datos) | CA-4 | Logs y eventos no contienen cuerpos de petición, documentos ni mensajes de error libres | Alta | Parcial (inspección de logs de prueba) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-040 | Funcional (evento) | CA-4 | Caída entre el commit y la publicación -> el evento se publica después sin perderse, y sin duplicarse al reintentar (design.md, decisión 2) | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-041 | Funcional (evento) | CA-4 | Dos pasos distintos y dos cierres distintos generan 2 + 2 eventos; no se fusionan como duplicados | Media | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-042 | Funcional (cálculo) | CA-5 / onboarding-measurement: Pausa incluida en la duración | Inicio 08:00, pausa 08:02, reanudación 08:10 y `wallet_active` 08:12 del mismo día -> duración primaria 12 minutos | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-043 | Funcional (cálculo) | CA-5 / onboarding-measurement: Tiempo de revisión incluido | Onboarding pendiente de revisión entre el inicio y la activación -> la duración incluye todo ese intervalo | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-044 | Funcional (cálculo) | CA-5 / onboarding-measurement: Cálculo con pendientes e incompletos | Tres finalizados de 2, 4 y 9 minutos, un pendiente de KYC y un incompleto -> mediana 4 minutos; pendiente e incompleto se reportan por separado sin duración de finalización | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-045 | Funcional (límite) | CA-5 | Cero onboardings finalizados en el periodo -> la mediana se informa como no calculable, no como 0 | Media | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-046 | Funcional (reporte) | CA-6 / onboarding-measurement: Reporte mensual posterior al lanzamiento | Reporte de la cohorte MVP en Perú en el trimestre posterior al lanzamiento: muestra el seguimiento de la mediana contra la meta de 8 a 3 minutos | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-047 | Funcional (reporte) | CA-6 / onboarding-measurement: Meta aún no alcanzada | Mediciones que no alcanzan la meta -> el reporte muestra el valor observado y su comparación sin alterar fórmulas, excluir pausas ni contar pendientes como finalizados | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-048 | Funcional (reporte) | CA-7 / onboarding-measurement: Reporte mensual posterior al lanzamiento | Reporte de los meses 1-3: inicios, activaciones, MAU y CAC, con metas MAU 1,000-2,000 y CAC PEN 10 | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-049 | Funcional (cálculo) | CA-7 / onboarding-measurement: Activación y múltiples pagos | En un mes, una wallet se activa sin transacciones y otra confirma 3 transacciones elegibles exitosas -> las dos aportan un total de 1 al MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-050 | Funcional (cálculo) | CA-7 / onboarding-measurement: Separación de meses calendario | Wallet con actividad elegible solo en el mes anterior y ninguna en el mes reportado -> no aporta al MAU del mes | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-051 | Funcional (cálculo) | CA-7 / onboarding-measurement: Actividad excluida | Tres wallets con solo pagos de prueba, solo anulados y solo totalmente revertidos antes del cierre -> ninguna aporta al MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-052 | Funcional (cálculo) | CA-7 / onboarding-measurement: Reversión parcial | Transacción elegible exitosa revertida solo parcialmente -> la wallet sigue contando una vez en el MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-053 | Funcional (límite) | CA-7 | Transacciones a las 23:59:59 `America/Lima` del último día del mes y a las 00:00:00 del día siguiente se asignan a meses distintos aunque en UTC caigan en el mismo día (design.md, decisión 4) | Media | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-054 | Funcional (cálculo) | CA-7 / onboarding-measurement: Primer contacto conservado | Primer contacto A al iniciar, contacto B posterior y activación a los 20 días -> la activación se atribuye a A | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-055 | Funcional (límite) | CA-7 / onboarding-measurement: Activación fuera de ventana | Activación a los 31 días del inicio atribuido -> no integra el denominador atribuible de CAC | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-056 | Funcional (límite) | CA-7 | Activación a exactamente 30 días del inicio atribuido -> sí integra el denominador atribuible (ventana de 30 días inclusive, design.md decisión 4) | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-057 | Funcional (cálculo) | CA-7 / onboarding-measurement: Cálculo con costos elegibles | PEN 100 medios pagados, PEN 20 agencias o afiliados, PEN 10 referidos, PEN 50 producto, PEN 40 gastos generales y 13 activados atribuibles -> gasto elegible PEN 130 y CAC PEN 10; PEN 90 excluidos | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-058 | Funcional (cálculo) | CA-7 / onboarding-measurement: Datos sin atribución | Con los datos de TC-057 más PEN 30 de gasto y 2 usuarios activados sin atribución -> CAC atribuible sigue en PEN 10 y los PEN 30 y 2 usuarios aparecen separados, sin reasignarse | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-059 | Funcional (límite) | CA-7 | Cero nuevos usuarios activados atribuibles -> CAC no calculable con sus componentes visibles, no PEN 0 (design.md, decisión 4) | Media | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-060 | Funcional (cálculo) | CA-7 | Gasto elegible PEN 100.00 y 3 activados atribuibles -> CAC 33.333... sin redondeo antes del cociente; el redondeo de presentación no está definido | Media | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-061 | Funcional (manual) | CA-3 / wallet-onboarding: Revisión de aceptación | Resultados de pruebas para flujo completo, datos inválidos, KYC pendiente y aprobado, y reanudación | Media | No (evidencia documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-062 | Funcional (manual) | CA-3 / wallet-onboarding: Revisión de aceptación | Checklist de cumplimiento con la versión de la política KYC aplicada, sin documentos reales de usuarios | Alta | No (evidencia documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-063 | Funcional (manual) | CA-4 / onboarding-measurement: Revisión de evidencia de medición | Muestra no sensible de eventos, timestamps y atribución, y reporte de onboarding, MAU y CAC que permite reproducir duraciones, mediana, MAU y CAC | Media | No (evidencia documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-064 | Performance (línea base) | Transversal / CA-1 | Operación de consulta del estado del onboarding, 1 usuario, 200 solicitudes: medir latencia p50, p95 y p99 y tasa de error. Umbral no definido | Media | Parcial (falta contrato y herramienta) | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-065 | Performance (concurrencia) | Transversal / CA-4 | 100 inicios concurrentes de usuarios distintos y 2 inicios simultáneos del mismo usuario (cantidades propuestas): un inicio por usuario, sin errores 5xx ni eventos duplicados. Umbral no definido | Media | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-066 | Performance (carga) | Transversal / CA-7 | Generación del reporte mensual con volumen hasta 2,000 wallets activas por mes (límite superior del objetivo de MAU): medir tiempo de generación y consumo de recursos. Umbral no definido | Baja | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |

## 3. Matriz de trazabilidad

### Criterios de aceptación -> casos

| Criterio | Casos | Casos verificables hoy | Cobertura de diseño |
|---|---|---|---|
| CA-1 Secuencia y paso actual | TC-001 a TC-003 | Ninguno | Completa; todos NO VERIFICABLE |
| CA-2 Bloqueo y corrección conforme a KYC | TC-004 a TC-011 | Ninguno | Completa; todos NO VERIFICABLE |
| CA-3 Activación condicionada y acceso a pagos | TC-012 a TC-030, TC-061, TC-062 | TC-014, 061, 062 (manuales) | Completa; resto NO VERIFICABLE |
| CA-4 Eventos sin datos sensibles | TC-034 a TC-041, TC-063 | TC-063 (manual) | Completa; resto NO VERIFICABLE |
| CA-5 Duración y reanudación | TC-031 a TC-033, TC-042 a TC-045 | Ninguno | Completa; todos NO VERIFICABLE |
| CA-6 Mediana contra meta de 8 a 3 minutos | TC-046, TC-047 | Ninguno | Completa; todos NO VERIFICABLE |
| CA-7 Reporte mensual: inicios, activaciones, MAU y CAC | TC-048 a TC-060 | Ninguno | Completa; todos NO VERIFICABLE |
| Transversal (performance) | TC-064 a TC-066 | TC-064 a 066 (sin herramienta ni umbral) | Soporte a CA-1, CA-4 y CA-7 |

### Escenarios de las specs -> casos

| Capacidad | Escenario -> casos |
|---|---|
| wallet-onboarding | Política disponible -> TC-012; Política no disponible -> TC-013, 014; Recorrido guiado -> TC-001, 002, 003; Dato obligatorio ausente -> TC-004, 007; Dato inválido corregible -> TC-005, 006, 007, 008, 009; Caso pendiente de revisión -> TC-018; Verificación no aprobada -> TC-019; Flujo completo y aprobado -> TC-016, 021, 022; Paso obligatorio pendiente -> TC-017; Activación aún no persistida -> TC-020; Pausa y reanudación -> TC-031, 032, 033; Registro sin límites por proveedor -> TC-029, 030; Revisión de aceptación -> TC-061, 062 |
| onboarding-measurement | Inicio y reanudación -> TC-034; Pasos, activación y cierre -> TC-035; Duplicación de una misma ocurrencia -> TC-036; Cierre sin activación -> TC-037; Inspección de eventos -> TC-038, 039; Pausa incluida en la duración -> TC-042; Tiempo de revisión incluido -> TC-043; Cálculo con pendientes e incompletos -> TC-044, 045; Reporte mensual posterior al lanzamiento -> TC-046, 048; Meta aún no alcanzada -> TC-047; Activación y múltiples pagos -> TC-049; Separación de meses calendario -> TC-050, 053; Actividad excluida -> TC-051; Reversión parcial -> TC-052; Primer contacto conservado -> TC-054; Activación fuera de ventana -> TC-055, 056; Cálculo con costos elegibles -> TC-057, 060; Datos sin atribución -> TC-058, 059; Revisión de evidencia de medición -> TC-063 |

## 4. Cobertura y brechas

| Métrica | Diseñada | Verificable hoy |
|---|---|---|
| Criterios de aceptación con casos de todas sus categorías aplicables | 7 de 7 (100%) | 0 de 7 (0%) automatizables; evidencia documental manual en CA-3 y CA-4 |
| Escenarios de las specs con al menos un caso | 32 de 32 (100%) | 3 de 32 (9%), todos documentales: Política no disponible (checklist), Revisión de aceptación y Revisión de evidencia de medición |
| Condiciones (particiones, límites y ejemplos numéricos) con caso | 37 de 37 (100%) | 0 de 37 |

La cobertura de diseño supera el 95% en las tres métricas. Es cobertura de diseño, no de código ni de ejecución. Brechas que bloquean la verificación (responsable entre paréntesis):

1. No existe contrato de API del onboarding: consulta de estado, avance de pasos, cierre, activación, `wallet_active` ni errores (Backend / Arquitectura). Afecta TC-002, 004 a 006, 010, 011, 016 a 020, 022 a 029, 031, 033 a 041.
2. La política KYC aprobada por Cumplimiento y sus pasos, documentos y mensajes de corrección no están disponibles (Cumplimiento). Afecta TC-001, 003 a 007, 012 a 015, 017 a 019.
3. No hay frontend (`front-end/` vacío) ni herramienta de UI definida (Frontend). Afecta TC-001, 003, 007, 021, 030, 032.
4. No hay canal de eventos ni reportes consultables para duraciones, mediana, MAU y CAC (Backend / Analítica). Afecta TC-034 a 060.
5. La captura de transacciones elegibles, pruebas, anulaciones y reversiones que alimentan MAU depende de HU-02, que tampoco las expone todavía. Afecta TC-049 a 052.
6. Sin datos de atribución ni de gasto de adquisición (Marketing / Finanzas). Afecta TC-054 a 060.
7. Sin umbrales ni volúmenes de performance definidos, salvo el límite de 2,000 MAU del Issue (Producto). Afecta TC-064 a 066.
8. `/affiliation-wallet` no está declarado como paso del onboarding; TC-008 y TC-009 requieren confirmar ese vínculo.

## 5. Resumen de automatización

| Automatizable | Casos |
|---|---|
| Sí | 32 |
| Parcial | 30 |
| No (evidencia documental) | 4 |
| Total | 66 |

| Capa | Casos |
|---|---|
| Backend | 56 |
| Frontend | 6 (TC-001, 003, 007, 021, 030, 032) |
| Ambas | 4 (TC-014, 061, 062, 063) |

| Nivel | Casos |
|---|---|
| Componente | 13 |
| Integración | 42 |
| E2E | 11 |

Listos para `software-testing-automation` hoy (Sí o Parcial, Backend, Estado distinto de NO VERIFICABLE): 0 casos funcionales. Los 3 de performance (TC-064 a 066) requieren definir herramienta, contrato y umbrales. Bloqueados por falta de información: 53 casos Backend y 6 de Frontend con Estado NO VERIFICABLE. El primer paso para desbloquearlos es definir el contrato de la API del onboarding.

## 6. Datos de prueba, supuestos y riesgos

**Datos:** sintéticos. La política de prueba (pasos `P1..Pn` y verificaciones) debe definirse como fixture en el entorno de pruebas; ningún documento real. Valores numéricos de referencia tomados de las specs: duraciones 12 minutos y medianas de 2, 4 y 9 minutos; gastos PEN 100, 20, 10, 50, 40 y 30; 13 y 2 usuarios activados; activaciones a 20, 30 y 31 días.

**Supuestos:**
- Las operaciones del onboarding y sus reportes se describen sin ruta ni cuerpo porque no hay contrato; los casos se concretarán al definirlo.
- Los casos de design.md (TC-015, 023, 028, 040, 053, 056, 059, 060) provienen del diseño aprobado, no de las specs; si el diseño cambia, deben revisarse.
- No se diseñaron casos de rate limiting, cabeceras de seguridad, CORS ni TLS por no existir requisito en el Issue ni en las specs. Los casos de estrés y de resistencia no se diseñaron por falta de requisito de carga.

**Riesgos:**
- Si `/affiliation-wallet` es el paso de captura, el contrato no define cómo se relaciona con los pasos ni con `wallet_active` (TC-008, TC-009). Además, el contrato exige `password` y el modelo del servicio no lo incluye (ver test-cases de HU-02, TC-083).
- MAU depende de actividad de pagos de HU-02, cuyo contrato aún no expone pruebas, anulaciones ni reversiones.
- La atribución de primer contacto y el gasto de adquisición provienen de fuentes externas sin definir.

## 7. Historial de cambios

| Fecha | Cambio |
|---|---|
| 2026-10-23 | Creación inicial: TC-001 a TC-066 (7 criterios, 32 escenarios, seguridad y performance). |
