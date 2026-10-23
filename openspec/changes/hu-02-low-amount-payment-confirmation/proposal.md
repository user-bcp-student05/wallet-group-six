# Proposal

## Why

El [Issue #2: HU-02 - Confirmacion de pagos de bajo monto](https://github.com/user-bcp-student05/wallet-group-six/issues/2), consultado mediante github-official el 2026-10-23, compromete que el pagador revise destino y monto antes de confirmar una transferencia de bajo monto y conozca su estado sin riesgo de pagar dos veces. Formalizar este contrato permite verificar la reduccion del abandono de checkout de 30% a 8% en el trimestre posterior al lanzamiento y medir MAU (1,000 a 2,000 en los meses 1-3) como actividad de pago, no como activacion.

## What Changes

### Alcance

- Mostrar identidad del destino y monto del intento antes de confirmar.
- Definir "bajo monto" como importe mayor que cero y menor o igual al menor limite aplicable (regulatorio, contractual del proveedor, tope MVP por proveedor/canal); sin configuracion completa la ruta no se habilita.
- Especificar los estados visibles "En proceso", "Pago realizado", "No se pudo realizar" y "Expirado", el tratamiento de timeout y respuestas ambiguas, la ventana de consulta por proveedor y la derivacion a conciliacion.
- Especificar idempotencia, bloqueo de pagos duplicados por checkout, reintento solo tras no-cargo confirmado, y manejo de notificaciones repetidas, tardias o contradictorias con auditoria.
- Especificar referencia visible, historial de pagos exitosos y evento `payer_payment_succeeded`.
- Especificar reportes de MAU y abandono con sus definiciones, exclusiones y ventanas.
- Exigir evidencia verificable: pruebas por escenario, checklist, muestras no sensibles y reportes.

### Comportamiento que se preserva

El baseline es [HU-02-Pago-contacto.md](../../../issues/HU-02-Pago-contacto.md). El cuerpo del Issue #2 coincide con el baseline y no tiene comentarios al consultarlo: no hay cambios de contrato ni excepciones que justificar. Se preservan:

- La ausencia de un importe global fijo; aplica el menor limite vigente y una ruta sin configuracion permanece deshabilitada.
- "En proceso" como estado no terminal; un timeout nunca es fallo ni expiracion; la expiracion exige confirmacion del proveedor.
- Un unico resultado terminal vigente por intento; correcciones solo con confirmacion autoritativa, registro auditable y conciliacion.
- Misma clave de idempotencia al repetir una solicitud tecnica; nuevo intento solo tras no-cargo definitivo, con nueva autorizacion y nueva clave.
- Referencia visible interna, estable y no sensible; referencia del proveedor solo asociada internamente.
- Exactamente un `payer_payment_succeeded` por exito confirmado.
- MAU: wallets unicas con al menos una transferencia elegible exitosa en el mes calendario; excluye pruebas y pagos anulados o totalmente revertidos antes del cierre; la reversion parcial no elimina la actividad.
- Abandono por `checkout_id` unico con ventana de 24 horas; pendientes reportados aparte.

### Fuera de alcance

- Definir los valores concretos de limites regulatorios, contractuales o tope MVP, ni los contratos, estados o ventanas de cada proveedor (son configuracion y documentacion obligatoria de lanzamiento).
- Onboarding, KYC y activacion de wallet (HU-01).
- Elegir arquitectura, APIs, almacenamiento, proveedores, librerias, formato de claves de idempotencia o instrumentacion tecnica.
- Procesos operativos internos de conciliacion mas alla de la derivacion y el bloqueo del reintento.
- Otros paises, monedas distintas de las configuradas, o programas de crecimiento.
- Crear diseno tecnico, `tasks.md` o modificar codigo funcional en esta etapa.

## Capabilities

### New Capabilities

- `payment-confirmation`: revision de destino y monto, limite de bajo monto, confirmacion y prevencion de pago duplicado por checkout.
- `payment-status-resolution`: estados visibles, resultado terminal unico, timeout, ventana de consulta, conciliacion, notificaciones, reintento e idempotencia.
- `payment-history-and-measurement`: referencia, historial, evento `payer_payment_succeeded`, MAU y abandono.

### Modified Capabilities

Ninguna. `wallet-onboarding` y `onboarding-measurement` (HU-01) no cambian; HU-02 solo consume wallets ya activas.

## Impact

El contrato afecta la experiencia de pago, el control de limites por proveedor/canal, el estado y reintento de pagos, el historial, la analitica y los reportes de Producto, Operaciones y Conciliacion. La documentacion contractual por proveedor y la configuracion de limites son prerrequisitos externos de lanzamiento. Esta entrega solo agrega documentacion del cambio bajo OpenSpec; no altera APIs, dependencias ni codigo. Las metas de negocio se monitorean, no se presentan como resultados ya alcanzados.
