# Casos de prueba - HU-02 Confirmación de pagos de bajo monto

- Issue: [#2 HU-02 - Confirmación de pagos de bajo monto](https://github.com/user-bcp-student05/wallet-group-six/issues/2)
- Cambio OpenSpec: `openspec/changes/hu-02-low-amount-payment-confirmation`
- Specs: `specs/payment-confirmation/spec.md`, `specs/payment-status-resolution/spec.md`, `specs/payment-history-and-measurement/spec.md`
- Contrato: `services/api/ux-contact-transfer-v1.yaml`
- Servicio analizado: `services/back-end/channel-contact-transfer-v1` (servicio mock)
- Última actualización: 2026-10-23

## 1. Resumen

Diseño de 96 casos funcionales, de seguridad y de performance para los 8 criterios de aceptación del Issue #2 y los 39 escenarios de las specs, más los casos de contrato de los 4 endpoints existentes (`/affiliation-wallet`, `/contact-transfers/financial-entities/search`, `/contact-transfers/authorization/verify`, `/contact-transfers/execute`).

Hoy el contrato y el servicio solo cubren la verificación y ejecución de la transferencia. Estados, límites por proveedor, idempotencia, historial, eventos, MAU y abandono no existen en el contrato; sus casos están diseñados contra las specs y marcados `NO VERIFICABLE` hasta que exista contrato.

## 2. Casos de prueba

Convenciones: **H** = headers válidos `sessionId: session-123`, `autentication: mock-auth-header`, `Authorization: Bearer mock-token` (valores ficticios del mock). **B64(x)** = x codificado en Base64. Todas las peticiones son `POST` con `Content-Type: application/json`. Valores válidos de referencia: `phone = B64("+51999888777")`, `documentNumber = B64("ABCD1234")`, `email = B64("demo@example.com")`. Disparadores del mock (dependen de la implementación actual): `documentNumber`/`phone` = `RkFJTF80MjI=` (422) y `RkFJTF81MDA=` (500); `amount` 422.00 (422) y 500.00 (500); `description` que contiene `fail422` o `fail500` (insensible a mayúsculas).

| ID | Tipo | Criterio / Escenario | Descripción y resultado esperado | Prioridad | Automatizable | Capa | Nivel | Herramienta sugerida | Prueba automatizada | Estado |
|---|---|---|---|---|---|---|---|---|---|---|
| TC-001 | Funcional (positivo) | CA-1 / payment-confirmation: Revisión previa | `/contact-transfers/authorization/verify`, H, `{"amount":25.50}` -> 200; `origin.name="M**** P****"`, `destination.name="J**** R****"`, `destination.financialEntity={code:"01",entity:"Yape"}` | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc001_verifyShowsOriginAndDestinationBeforeConfirming` | APROBADO |
| TC-002 | Funcional (UI) | CA-1 / payment-confirmation: Revisión previa | En la pantalla de confirmación se muestran la identidad del destino y el monto del intento antes del botón de confirmar. Frontend aún no implementado | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-003 | Funcional (E2E) | CA-1 / payment-confirmation: Revisión previa | Flujo con H y el mismo `sessionId`: `search` (phone válido) -> 200 con 2 entidades; `verify` `{"amount":25.50}` -> 200; `execute` `{"description":"Pago de almuerzo"}` -> 200 con `operationCode` y `transactionDateTime` | Alta | Sí | Backend | E2E | REST Assured | `ContactTransferFlowE2ETest#tc003_searchVerifyAndExecuteFlow` | APROBADO |
| TC-004 | Funcional (integridad) | CA-1 / payment-confirmation: Revisión previa | Cambiar el monto o el destino entre `verify` y `execute` exige nueva revisión; el intento ejecutado coincide con el mostrado. `execute` no recibe monto ni destino y el contrato no define el vínculo con `verify`. Falta: contrato del vínculo verify-execute | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-005 | Seguridad (exposición de datos) | CA-1 | Respuesta de `verify` 200 con H: nombres enmascarados con asteriscos (patrón `^\S\*+ \S\*+$`) y sin documento, teléfono ni correo en el cuerpo | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc005_verifyMasksNamesAndHidesPersonalData` | APROBADO |
| TC-006 | Funcional (negativo) | CA-1 | `verify`, H, `{}` (sin `amount`) -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc006_verifyWithoutAmountReturns400` | APROBADO |
| TC-007 | Funcional (límite) | CA-2 / payment-confirmation: Importe dentro del menor límite | `verify`, H, `amount=0.01` (mínimo del contrato) -> 200 | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc007_verifyAcceptsMinimumAmount` | APROBADO |
| TC-008 | Funcional (límite) | CA-2 / payment-confirmation: Importe dentro del menor límite | `verify`, H, `amount=99999999.99` (máximo del contrato) -> 200 | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc008_verifyAcceptsMaximumAmount` | APROBADO |
| TC-009 | Funcional (negativo) | CA-2 / payment-confirmation: Importe cero o negativo | `verify`, H, `amount=0` y `amount=0.00` -> 400, `code="HB0305"` (envío bloqueado) | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc009_verifyBlocksZeroAmount` | APROBADO |
| TC-010 | Funcional (negativo) | CA-2 / payment-confirmation: Importe cero o negativo | `verify`, H, `amount=-1` -> 400, `code="HB0305"` | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc010_verifyBlocksNegativeAmount` | APROBADO |
| TC-011 | Funcional (límite) | CA-2 / payment-confirmation: Importe que excede algún límite | `verify`, H, `amount=100000000.00` (justo sobre el máximo del contrato) -> 400, `code="HB0305"` | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc011_verifyBlocksAmountAboveContractMaximum` | APROBADO |
| TC-012 | Funcional (negativo) | CA-2 | `verify`, H, `amount=1.234` y `amount=0.001` (más de 2 decimales) -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc012_verifyBlocksMoreThanTwoDecimals` | APROBADO |
| TC-013 | Funcional (negativo) | CA-2 | `verify`, H, `{"amount":"abc"}` -> 400 (el contrato no define el cuerpo de este error) | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc013_verifyRejectsNonNumericAmount` | APROBADO |
| TC-014 | Funcional (negativo) | CA-2 / payment-confirmation: Importe que excede algún límite | Importe mayor al menor límite aplicable (regulatorio, contractual, tope MVP) del proveedor/canal -> envío bloqueado. Falta: valores de límites y contrato de error | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-015 | Funcional (positivo) | CA-2 / payment-confirmation: Importe dentro del menor límite | Importe mayor que cero e igual al límite efectivo del proveedor/canal -> permitido. Falta: valores de límites | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-016 | Funcional (decisión) | CA-2 / payment-confirmation: Límite menor que el máximo regulatorio | Con límite contractual o tope MVP inferior al regulatorio se aplica el menor. Falta: configuración de límites | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-017 | Funcional (negativo) | CA-2 / payment-confirmation: Falta un límite o su configuración | Ruta de proveedor/canal sin algún límite o sin configuración -> no disponible para pagar. Falta: contrato de disponibilidad de rutas | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-018 | Funcional (manual) | CA-2 / payment-confirmation: Checklist disponible | Checklist con resultado por elemento de límites, integridad de destino/monto y prevención de duplicados | Media | No (evidencia documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-019 | Seguridad (autenticación) | CA-2 | Los 4 endpoints con cuerpo válido, headers `sessionId` y `autentication` y sin `Authorization` -> 401, `code="HB0302"` | Alta | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc019_missingAuthorizationReturns401` | APROBADO |
| TC-020 | Seguridad (autenticación) | CA-2 | Los 4 endpoints con `Authorization` = `Basic abc123`, `Bearer` sin token o vacío -> 401, `code="HB0302"` | Alta | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc020_invalidAuthorizationSchemeReturns401` | APROBADO |
| TC-021 | Seguridad (headers) | CA-2 | Los 4 endpoints sin header `autentication` (resto válido) -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc021_missingAutenticationReturns400` | APROBADO |
| TC-022 | Seguridad (validación) | CA-2 | `sessionId` = `bad id!`, `con espacios`, `a/b` en los 4 endpoints -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc022_invalidSessionIdReturns400` | APROBADO |
| TC-023 | Funcional (límite) | CA-2 | `verify` válido con `sessionId` de 128 caracteres `[A-Za-z0-9]` -> 200; con 129 caracteres -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc023_verifySessionIdLengthBoundary` | APROBADO |
| TC-024 | Seguridad (exposición de datos) | CA-2 | Cuerpos de errores 400, 401, 422 y 500 de todos los endpoints contienen solo `code` y `description` genéricos, sin trazas de pila ni valores enviados (documento, teléfono, correo, token) | Alta | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc024_errorBodiesDoNotLeakData` | APROBADO |
| TC-025 | Seguridad (inyección) | CA-2 | `execute`, H, `description` = `'; DROP TABLE x;--` y `<script>alert(1)</script>` -> 200; el cuerpo contiene solo `operationCode` y `transactionDateTime`, sin reflejar el texto | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc025_executeDoesNotReflectInjectionPayloads` | APROBADO |
| TC-026 | Seguridad (validación) | CA-2 | `execute`, H, `description` con carácter de control (`"hola\u0001mundo"`) -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc026_executeRejectsControlCharacters` | APROBADO |
| TC-027 | Seguridad (mass assignment) | CA-2 | `verify`, H, `{"amount":25.50,"extra":"x"}`; el contrato define `additionalProperties:false` -> 400, `code="HB0305"`. Posible defecto: la implementación podría ignorar el campo | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc027_verifyRejectsUnexpectedField` | FALLIDO: esperado 400, obtenido 200 (el servicio ignora campos no declarados) |
| TC-028 | Seguridad (configuración) | CA-2 | `GET` y `PUT` sobre `/contact-transfers/execute` con H -> 405 | Baja | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc028_executeRejectsUnsupportedMethods` | APROBADO |
| TC-029 | Seguridad (configuración) | CA-2 | `verify`, H, `Content-Type: text/plain` con cuerpo `amount=25.50` -> 415 | Baja | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc029_unsupportedContentTypeReturns415` | APROBADO |
| TC-030 | Seguridad (autorización) | CA-2 | Acceso con `sessionId` perteneciente a otro pagador (BOLA) -> rechazado. Falta: modelo de pertenencia sesión-pagador en el contrato | Alta | Sí | Backend | Componente | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-031 | Seguridad (límite de uso) | CA-2 | Ráfaga de solicitudes por sesión supera el umbral -> 429. Falta: requisito de rate limiting y umbral | Baja | Parcial | Backend | Componente | k6 / REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-032 | Funcional (estado) | CA-3 / payment-status-resolution: Resultado desconocido | Intento con resultado desconocido -> estado visible "En proceso". Falta: contrato de estado del pago | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-033 | Funcional (negativo) | CA-3 / payment-confirmation: Segundo pago con resultado desconocido | Con el intento de un `checkout_id` en "En proceso", un segundo pago de ese checkout no se inicia. Falta: `checkout_id` en el contrato | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-034 | Funcional (UI) | CA-3 / payment-status-resolution: Resultado desconocido | La pantalla muestra "En proceso" y deshabilita iniciar otro pago del mismo checkout | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-035 | Funcional (estado) | CA-4 / payment-status-resolution: Timeout de comunicación | Timeout simulado del proveedor -> el intento sigue "En proceso" y no se ofrece nuevo intento. Falta: adaptador de proveedor simulado y contrato de estado | Alta | Sí | Backend | Integración | REST Assured + WireMock | _(pendiente)_ | NO VERIFICABLE |
| TC-036 | Funcional (estado) | CA-4 / payment-status-resolution: Respuesta ambigua | Respuesta del proveedor sin señal definitiva -> el intento sigue "En proceso" | Alta | Sí | Backend | Integración | REST Assured + WireMock | _(pendiente)_ | NO VERIFICABLE |
| TC-037 | Funcional (estado) | CA-4 / payment-status-resolution: Ventana agotada con estado desconocido | Al terminar la ventana máxima de consulta con estado desconocido -> derivado a conciliación, reintentos bloqueados, no se muestra como fallido. Falta: ventana por proveedor | Alta | Sí | Backend | Integración | REST Assured + WireMock | _(pendiente)_ | NO VERIFICABLE |
| TC-038 | Funcional (manual) | CA-4 / payment-status-resolution: Proveedor sin documentación | Un proveedor sin estados, señales definitivas y ventana documentados no tiene su ruta habilitada (checklist de lanzamiento) | Media | No (revisión documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-039 | Funcional (idempotencia) | CA-4 / payment-status-resolution: Notificación repetida | La misma notificación de resultado recibida dos veces produce un solo efecto. Falta: contrato de notificaciones | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-040 | Funcional (estado) | CA-4 / payment-status-resolution: Notificación tardía | Confirmación definitiva de un intento "En proceso" -> se registra el terminal y queda auditado | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-041 | Funcional (estado) | CA-4 / payment-status-resolution: Notificación contradictoria | Notificación que contradice un terminal vigente -> el estado visible no cambia hasta confirmación autoritativa, registro auditable y conciliación | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-042 | Funcional (estado) | CA-4 / payment-status-resolution: Resultado terminal único | Tras un terminal, un segundo resultado terminal distinto no sustituye al vigente | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-043 | Funcional (estado) | CA-4 / payment-status-resolution: Expiración confirmada | El proveedor confirma que el intento venció sin completar el pago -> "Expirado" | Alta | Sí | Backend | Integración | REST Assured + WireMock | _(pendiente)_ | NO VERIFICABLE |
| TC-044 | Funcional (positivo) | CA-5 / payment-status-resolution: Éxito confirmado | `execute`, H, `{"description":"Pago de almuerzo"}` -> 200; `operationCode` cumple `^[A-Za-z0-9_-]{1,64}$`; `transactionDateTime` en formato RFC 3339 | Alta | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc044_executeReturnsOperationCodeAndTransactionDateTime` | APROBADO |
| TC-045 | Funcional (límite) | CA-5 | `execute`, H, `description` de 140 caracteres -> 200 | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc045_executeAcceptsMaxLengthDescription` | APROBADO |
| TC-046 | Funcional (límite) | CA-5 | `execute`, H, `description` de 141 caracteres -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc046_executeRejectsTooLongDescription` | APROBADO |
| TC-047 | Funcional (negativo) | CA-5 | `execute`, H, `description` vacía, solo espacios o ausente -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc047_executeRejectsBlankOrMissingDescription` | APROBADO |
| TC-048 | Seguridad (exposición de datos) | CA-5 / payment-history-and-measurement: Referencia tras éxito | `operationCode` de `execute` 200 no contiene teléfono, documento ni nombres usados en el flujo | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc048_operationCodeDoesNotContainPersonalData` | APROBADO |
| TC-049 | Funcional (positivo) | CA-5 / payment-history-and-measurement: Pago en historial | Tras un éxito, el historial incluye destino enmascarado, monto y moneda, fecha/hora y referencia. Falta: endpoint de historial y moneda en el contrato | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-050 | Funcional (negativo) | CA-5 / payment-history-and-measurement: Pagos no exitosos | Pagos "En proceso", "No se pudo realizar" o "Expirado" no aparecen como exitosos en el historial. Falta: endpoint de historial | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-051 | Funcional (UI) | CA-5 / payment-history-and-measurement: Referencia tras éxito | Tras un éxito, la pantalla muestra la referencia devuelta por el servicio | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-052 | Seguridad (exposición de datos) | CA-5 / payment-history-and-measurement: Referencia tras éxito | La respuesta 200 de `execute` contiene solo `operationCode` y `transactionDateTime`; la referencia del proveedor no se expone | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc052_executeResponseExposesOnlyContractFields` | APROBADO |
| TC-053 | Funcional (negativo) | CA-6 / payment-status-resolution: Fallo confirmado sin cargo | `execute`, H, `description` = `fail422`, `FAIL422`, `Pago FaIl422` -> 422, `code="HB0700"` | Alta | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc053_executeRejectedTransferReturns422` | APROBADO |
| TC-054 | Funcional (negativo) | CA-6 / payment-status-resolution: Fallo confirmado sin cargo | `verify`, H, `amount=422.00` -> 422, `code="HB0700"` | Alta | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc054_verifyRejectedTransferReturns422` | APROBADO |
| TC-055 | Funcional (negativo) | CA-6 | `execute`, H, `description="fail500"` -> 500, `code="H9999"` | Media | Sí | Backend | Componente | REST Assured | `ExecuteTransferComponentTest#tc055_executeInternalErrorReturns500` | APROBADO |
| TC-056 | Funcional (negativo) | CA-6 | `verify`, H, `amount=500.00` -> 500, `code="H9999"` | Media | Sí | Backend | Componente | REST Assured | `VerifyTransferComponentTest#tc056_verifyInternalErrorReturns500` | APROBADO |
| TC-057 | Funcional (idempotencia) | CA-6 / payment-status-resolution: Solicitud técnica repetida | Repetir la misma solicitud técnica de un intento conserva la clave de idempotencia y no genera un cargo adicional. Falta: clave de idempotencia en el contrato | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-058 | Funcional (negativo) | CA-6 / payment-status-resolution: Solicitud técnica repetida | Misma clave con contenido distinto -> rechazada. Falta: clave de idempotencia en el contrato | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-059 | Funcional (concurrencia) | CA-6 / payment-status-resolution: Solicitud técnica repetida | Dos confirmaciones concurrentes del mismo checkout -> un único cargo y un único intento activo | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-060 | Funcional (estado) | CA-6 / payment-status-resolution: Reintento tras fallo o expiración sin cargo | Tras no-cargo confirmado se ofrece volver o nuevo intento, que exige nueva autorización del pagador y nueva clave | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-061 | Funcional (negativo) | CA-6 / payment-status-resolution: Reintento sin confirmación de no-cargo | Con el intento "En proceso" o en conciliación no se permite un nuevo intento | Alta | Sí | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-062 | Funcional (UI) | CA-6 / payment-status-resolution: Reintento tras fallo o expiración sin cargo | Tras "No se pudo realizar" o "Expirado" la pantalla ofrece volver o nuevo intento; durante "En proceso" no | Alta | Sí | Frontend | E2E | Playwright / Cypress (por definir) | _(pendiente)_ | NO VERIFICABLE |
| TC-063 | Funcional (evento) | CA-7 / payment-history-and-measurement: Evento payer_payment_succeeded (éxito) | Cada éxito confirmado emite un solo `payer_payment_succeeded`. Falta: canal de eventos observable | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-064 | Funcional (evento) | CA-7 / payment-history-and-measurement: Evento payer_payment_succeeded (confirmación repetida) | La misma confirmación de éxito recibida de nuevo no emite un segundo evento | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-065 | Funcional (evento) | CA-7 / payment-history-and-measurement: Evento payer_payment_succeeded (pago no exitoso) | Pago pendiente, fallido o expirado no emite `payer_payment_succeeded` | Alta | Parcial (vía consulta del canal de eventos) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-066 | Funcional (reporte) | CA-7 / payment-history-and-measurement: MAU - varias transferencias | Una wallet con varias transferencias elegibles exitosas en el mes cuenta una sola vez en MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-067 | Funcional (reporte) | CA-7 / payment-history-and-measurement: MAU - solo activación | Wallet activada sin pagos exitosos en el mes no cuenta en MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-068 | Funcional (reporte) | CA-7 / payment-history-and-measurement: MAU - reversión total | Wallet cuya única transferencia exitosa se anula o revierte totalmente antes del cierre del mes no cuenta | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-069 | Funcional (reporte) | CA-7 / payment-history-and-measurement: MAU - reversión parcial | Transferencia revertida solo parcialmente: la wallet sigue contando en MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-070 | Funcional (reporte) | CA-7 / payment-history-and-measurement: MAU - pruebas o no exitosos | Wallet cuyos únicos pagos son de prueba, pendientes, fallidos o expirados no cuenta en MAU | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-071 | Funcional (reporte) | CA-8 / payment-history-and-measurement: Abandono - éxito en 24 h | Checkout con éxito confirmado dentro de las 24 horas de iniciado no cuenta como abandonado (23 h 59 min) | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-072 | Funcional (reporte) | CA-8 / payment-history-and-measurement: Abandono - sin éxito | Checkout sin éxito tras 24 horas y no pendiente cuenta como abandonado | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-073 | Funcional (reporte) | CA-8 / payment-history-and-measurement: Abandono - pendiente | Checkout con resultado pendiente se reporta aparte de los abandonados | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-074 | Funcional (reporte) | CA-8 / payment-history-and-measurement: Abandono - múltiples intentos | Un `checkout_id` con varios intentos cuenta una sola vez en numerador y denominador | Alta | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-075 | Funcional (límite) | CA-8 | Cero checkouts iniciados: la tasa no produce una división inválida y se informa como no calculable | Media | Parcial (vía consulta del reporte) | Backend | Integración | REST Assured | _(pendiente)_ | NO VERIFICABLE |
| TC-076 | Funcional (manual) | CA-4 / payment-status-resolution: Pruebas disponibles | Resultados de prueba para éxito, fallo confirmado, expiración confirmada, timeout, notificación tardía y reintento | Media | No (evidencia documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-077 | Funcional (manual) | CA-7 / payment-history-and-measurement: Evidencia disponible | Muestra no sensible de referencia, historial y evento, y reportes de abandono y MAU con la definición aprobada por Producto | Media | No (evidencia documental) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-078 | Funcional (manual) | CA-8 | Monitoreo de la tasa de abandono contra la meta de 30% a 8% en el trimestre posterior al lanzamiento | Baja | No (seguimiento de negocio) | Ambas | E2E | Manual | N/A | NO EJECUTADO |
| TC-079 | Funcional (positivo) | Contrato: affiliation-wallet | `/affiliation-wallet`, H, documentType `DNI`, documentNumber, phone y email válidos -> 204 sin contenido | Alta | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc079_affiliationSucceedsWithNoContent` | APROBADO |
| TC-080 | Funcional (positivo) | Contrato: affiliation-wallet | Igual que TC-079 con documentType `DNI`, `CE` y `PASSPORT` -> 204 en cada uno | Media | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc080_affiliationAcceptsEveryDocumentType` | APROBADO |
| TC-081 | Funcional (negativo) | Contrato: affiliation-wallet | documentType `RUC` -> 400; documentType ausente -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc081_affiliationRejectsUnknownDocumentType`, `#tc081_affiliationRejectsMissingDocumentType` | APROBADO |
| TC-082 | Funcional (negativo) | Contrato: affiliation-wallet | documentNumber, phone o email vacío, ausente o no Base64 (`abc`, `no es base64!`) -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc082_affiliationRejectsInvalidDocumentNumber`, `#tc082_affiliationRejectsInvalidPhone`, `#tc082_affiliationRejectsInvalidEmail` | APROBADO |
| TC-083 | Funcional (negativo) | Contrato: affiliation-wallet | Sin el campo `password` (requerido por el contrato) -> 400, `code="HB0305"`. Posible defecto: el modelo del servicio no incluye `password` | Media | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc083_affiliationRequiresPassword` | FALLIDO: esperado 400, obtenido 204 (el servicio no exige `password`) |
| TC-084 | Funcional (negativo) | Contrato: affiliation-wallet | documentNumber = `RkFJTF80MjI=` -> 422, `code="H9999"` | Alta | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc084_affiliationRejectedForDocumentReturns422` | APROBADO |
| TC-085 | Funcional (negativo) | Contrato: affiliation-wallet | documentNumber = `RkFJTF81MDA=` -> 500, `code="H9999"` | Media | Sí | Backend | Componente | REST Assured | `AffiliationWalletComponentTest#tc085_affiliationInternalErrorReturns500` | APROBADO |
| TC-086 | Funcional (positivo) | Contrato: financial-entities/search | `/contact-transfers/financial-entities/search`, H, phone válido -> 200, lista de 2 elementos `{code:"01",entity:"Yape"}` y `{code:"02",entity:"Plin"}` | Alta | Sí | Backend | Componente | REST Assured | `SearchFinancialEntitiesComponentTest#tc086_searchReturnsWalletsForPhone` | APROBADO |
| TC-087 | Funcional (negativo) | Contrato: financial-entities/search | phone vacío, ausente o no Base64 -> 400, `code="HB0305"` | Media | Sí | Backend | Componente | REST Assured | `SearchFinancialEntitiesComponentTest#tc087_searchWithInvalidPhoneReturns400`, `#tc087_searchWithoutPhoneReturns400` | APROBADO |
| TC-088 | Funcional (negativo) | Contrato: financial-entities/search | phone = `RkFJTF81MDA=` -> 500, `code="H9999"` | Media | Sí | Backend | Componente | REST Assured | `SearchFinancialEntitiesComponentTest#tc088_searchInternalErrorReturns500` | APROBADO |
| TC-089 | Funcional (negativo) | Contrato: todos | Los 4 endpoints con H y cuerpo ausente -> 400 | Media | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc089_missingBodyReturns400` | APROBADO |
| TC-090 | Funcional (negativo) | Contrato: todos | Los 4 endpoints con H y JSON malformado (`{"amount":`) -> 400 | Media | Sí | Backend | Componente | REST Assured | `CommonRequestRulesComponentTest#tc090_malformedJsonReturns400` | APROBADO |
| TC-091 | Performance (línea base) | Transversal / CA-1 | `verify` con H, 1 usuario, 200 solicitudes secuenciales: medir latencia p50, p95 y p99 y tasa de error. Umbral no definido | Media | Parcial (herramienta de carga por definir) | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-092 | Performance (carga) | Transversal / CA-5 | `execute` con H bajo carga sostenida; volumen y duración a definir por Producto; medir p95, throughput y errores. Umbral no definido | Media | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-093 | Performance (concurrencia) | Transversal / CA-3 | 50 solicitudes simultáneas a `verify` con `sessionId` distintos (cantidad propuesta): todas 200 con los datos esperados, sin mezcla entre sesiones | Media | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-094 | Performance (estrés) | Transversal | Incremento escalonado de carga sobre `verify` hasta degradación; identificar el punto de saturación y el comportamiento al superarlo (sin caída del servicio). Umbral no definido | Baja | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-095 | Performance (resistencia) | Transversal | Carga moderada y constante durante un intervalo prolongado a definir: latencia estable y uso de memoria sin crecimiento sostenido. Umbral no definido | Baja | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |
| TC-096 | Performance (pico) | Transversal | Aumento súbito de solicitudes sobre `execute`: sin errores 5xx inesperados y recuperación de la latencia al cesar el pico. Umbral no definido | Baja | Parcial | Backend | Integración | k6 / JMeter (por definir) | _(pendiente)_ | NO EJECUTADO |

## 3. Matriz de trazabilidad

### Criterios de aceptación -> casos

| Criterio | Casos | Casos verificables hoy | Cobertura de diseño |
|---|---|---|---|
| CA-1 Destino y monto antes de confirmar | TC-001 a TC-006 | TC-001, 002, 003, 005, 006 | Completa; TC-004 NO VERIFICABLE |
| CA-2 Límites y ruta no habilitada | TC-007 a TC-031 | TC-007 a 013, 018 a 029 | Completa; TC-014 a 017, 030, 031 NO VERIFICABLE |
| CA-3 Resultado desconocido, no pagar dos veces | TC-032 a TC-034 | Ninguno | Completa; todos NO VERIFICABLE |
| CA-4 Resultado terminal único, timeout y correcciones | TC-035 a TC-043, TC-076 | TC-038, 076 (manuales) | Completa; resto NO VERIFICABLE |
| CA-5 Éxito, referencia e historial | TC-044 a TC-052 | TC-044 a 048, 051, 052 | Completa; TC-049 y 050 NO VERIFICABLE |
| CA-6 Fallo/expiración sin cargo y reintentos | TC-053 a TC-062 | TC-053 a 056 | Completa; TC-057 a 062 NO VERIFICABLE |
| CA-7 Evento y MAU | TC-063 a TC-070, TC-077 | TC-077 (manual) | Completa; resto NO VERIFICABLE |
| CA-8 Abandono | TC-071 a TC-075, TC-078 | TC-078 (manual) | Completa; resto NO VERIFICABLE |
| Transversal (contrato, seguridad, performance) | TC-018 a TC-031, TC-079 a TC-096 | TC-079 a 096 | Soporte a CA-1 a CA-8 |

### Escenarios de las specs -> casos

| Capacidad | Escenario -> casos |
|---|---|
| payment-confirmation | Revisión previa -> TC-001, 002; Importe dentro del menor límite -> TC-007, 008, 015; Importe que excede algún límite -> TC-011, 014; Importe cero o negativo -> TC-009, 010; Límite menor que el máximo regulatorio -> TC-016; Falta un límite o su configuración -> TC-017; Segundo pago con resultado desconocido -> TC-033; Checklist disponible -> TC-018 |
| payment-status-resolution | Resultado desconocido -> TC-032, 034; Éxito confirmado -> TC-044; Fallo confirmado sin cargo -> TC-053, 054; Expiración confirmada -> TC-043; Timeout -> TC-035; Respuesta ambigua -> TC-036; Ventana agotada -> TC-037; Proveedor sin documentación -> TC-038; Notificación repetida -> TC-039; Notificación tardía -> TC-040; Notificación contradictoria -> TC-041, 042; Solicitud técnica repetida -> TC-057, 058, 059; Reintento tras no-cargo -> TC-060, 062; Reintento sin confirmación -> TC-061; Pruebas disponibles -> TC-076 |
| payment-history-and-measurement | Referencia tras éxito -> TC-048, 051, 052; Pago en historial -> TC-049; Pagos no exitosos -> TC-050; Evento: éxito, confirmación repetida, pago no exitoso -> TC-063, 064, 065; MAU: varias transferencias, solo activación, reversión total, reversión parcial, pruebas o no exitosos -> TC-066 a 070; Abandono: éxito en 24 h, sin éxito, pendiente, múltiples intentos -> TC-071 a 074; Evidencia disponible -> TC-077 |

## 4. Cobertura y brechas

| Métrica | Diseñada | Verificable con el contrato actual |
|---|---|---|
| Criterios de aceptación con casos de todas sus categorías aplicables | 8 de 8 (100%) | 4 de 8 (50%): CA-1, CA-2, CA-5 y CA-6, de forma parcial |
| Escenarios de las specs con al menos un caso | 39 de 39 (100%) | 5 de 39 (13%): Revisión previa, Importe cero o negativo, Éxito confirmado, Fallo confirmado sin cargo y Referencia tras éxito |
| Condiciones del contrato (particiones y límites) con caso | 30 de 30 (100%) | 30 de 30 (100%) |

La cobertura de diseño supera el 95% en las tres métricas. Es cobertura de diseño, no de código ni de ejecución. Brechas que impiden verificar el resto hoy:

1. El contrato no incluye estados de pago, `checkout_id`, clave de idempotencia, notificaciones del proveedor, historial, moneda, límites por proveedor/canal ni disponibilidad de rutas (afecta TC-004, 014 a 017, 030, 032 a 037, 039 a 043, 049, 050, 057 a 061).
2. No hay canal de eventos ni reportes consultables para `payer_payment_succeeded`, MAU y abandono (TC-063 a 075). Responsable: Producto / Backend.
3. No existe frontend (`front-end/` vacío): TC-002, 034, 051 y 062 dependen de su implementación y de la herramienta de UI.
4. No hay umbrales ni volúmenes de performance definidos (TC-091 a 096). Responsable: Producto.
5. No hay requisito de rate limiting ni modelo de pertenencia sesión-pagador (TC-030, 031).

## 5. Resumen de automatización

| Automatizable | Casos |
|---|---|
| Sí | 71 |
| Parcial | 20 |
| No (evidencia o seguimiento manual) | 5 |
| Total | 96 |

| Capa | Casos |
|---|---|
| Backend | 87 |
| Frontend | 4 (TC-002, 034, 051, 062) |
| Ambas | 5 (TC-018, 038, 076, 077, 078) |

| Nivel | Casos |
|---|---|
| Componente | 50 |
| Integración | 36 |
| E2E | 10 |

Listos para `software-testing-automation` hoy (Sí, Backend, Estado distinto de NO VERIFICABLE): 44 casos funcionales y de seguridad (TC-001, 003, 005 a 013, 019 a 029, 044 a 048, 052 a 056, 079 a 090). Los 6 de performance (TC-091 a 096) requieren definir herramienta y umbrales. Bloqueados por falta de información: 23 casos Backend con Estado NO VERIFICABLE más 2 de Frontend (TC-034, 062).

## 6. Datos de prueba, supuestos y riesgos

**Datos:** sintéticos (ver convenciones de la sección 2). No usar datos ni credenciales reales.

**Supuestos:**
- Las specs de HU-02 describen un pago multirriel con estados, límites e idempotencia que el servicio mock `channel-contact-transfer-v1` aún no implementa; los casos NO VERIFICABLE se desbloquean al ampliar el contrato.
- Los disparadores de 422 y 500 son del servicio mock; con una implementación real cambiarán los datos de entrada, no los resultados esperados del contrato.
- No hay requisitos de cabeceras de seguridad, CORS ni TLS en el contrato; no se diseñaron casos para ellos.

**Riesgos y posibles defectos a confirmar al ejecutar:**
- TC-083: el contrato exige `password` pero el modelo no lo incluye; la afiliación sin `password` podría devolver 204.
- TC-027: el contrato declara `additionalProperties: false` pero la implementación podría ignorar campos extra.
- Discrepancias de documentación del contrato: tabla de errores con `H99999`/`H0700` frente a `H9999`/`HB0700` del código y los esquemas. Los casos usan los valores del código; conviene corregir el contrato.
- `execute` no recibe monto ni destino: la integridad entre `verify` y `execute` (TC-004) depende de la sesión y no está especificada.
- Existen 64 pruebas funcionales de componente ya escritas en `src/test/java/.../v1/functional/component` (no referencian IDs `TC-xxx`); el skill de automatización puede mapearlas a TC-001, 005 a 013, 019 a 029, 044 a 048, 052 a 056 y 079 a 090, y completar la columna `Prueba automatizada`.

## 7. Historial de cambios

| Fecha | Cambio |
|---|---|
| 2026-10-23 | Creación inicial: TC-001 a TC-096 (8 criterios, 39 escenarios, contrato de 4 endpoints, seguridad y performance). |
