# HU-02 - Confirmación de pagos de bajo monto

## Veredicto

**Definición PO cerrada para el MVP.** Los parámetros contractuales por proveedor son configuración obligatoria de lanzamiento; una ruta sin esos parámetros no se habilita.

## Historia de usuario

Como pagador,
quiero revisar el destino y el monto antes de confirmar una transferencia de bajo monto y conocer su estado,
para saber si se realizó sin correr el riesgo de pagar dos veces.

## Contexto

- Épica: [MVP de wallet en Perú](../epic/peru-wallet-mvp.sp.md).
- El MVP es multirriel y depende de partners con contratos y habilitación operativa.
- Los límites pueden variar por proveedor y ser inferiores al máximo regulatorio.
- La meta de negocio es reducir el abandono de checkout de 30% a 8% durante el trimestre posterior al lanzamiento y mantener MAU entre 1,000 y 2,000 en los meses 1-3.
- MAU mide actividad de pago; activar la wallet por sí sola no cuenta como actividad.

## Decisiones de producto

- “Bajo monto” significa un importe mayor que cero y menor o igual al menor de estos límites aplicables: límite regulatorio, contractual del proveedor y tope MVP configurado para el proveedor/canal. Si falta cualquiera de los límites aplicables o su configuración, esa ruta de pago no se habilita.
- Los estados visibles son “En proceso”, “Pago realizado”, “No se pudo realizar” y “Expirado”. “En proceso” no es terminal. Expiración significa confirmación del proveedor de que el intento venció sin completar el pago; un timeout de comunicación nunca se interpreta como fallo o expiración.
- Cada proveedor debe tener documentados antes del lanzamiento sus estados, señales de resultado definitivo y ventana máxima de consulta. Si el estado sigue siendo desconocido al terminar esa ventana, el intento permanece bloqueado para reintentos y se deriva a conciliación operativa; no se presume que el pago falló.
- Repetir una solicitud técnica conserva la misma clave de idempotencia. Solo después de una confirmación definitiva de no-cargo se permite iniciar un nuevo intento, con nueva autorización del pagador y una nueva clave.
- Una notificación repetida del mismo resultado se procesa una sola vez. Una notificación contradictoria no cambia silenciosamente un resultado terminal: requiere confirmación autoritativa del proveedor, registro auditable y conciliación antes de corregir el estado visible.
- La referencia visible es un identificador interno estable y no sensible. El historial de pagos exitosos muestra destino enmascarado, monto y moneda, fecha/hora y referencia; la referencia del proveedor se conserva asociada internamente.
- Se emite el evento `payer_payment_succeeded` una sola vez al confirmar el éxito. MAU cuenta wallets únicas con al menos una transferencia elegible confirmada como exitosa en el mes calendario; excluye pruebas y operaciones anuladas o totalmente revertidas antes del cierre del mes. Una reversión parcial no elimina la actividad.
- El abandono se calcula por `checkout_id` único: checkouts iniciados que no alcanzan éxito confirmado dentro de las 24 horas / checkouts iniciados. Los pendientes se reportan aparte. La meta para el trimestre posterior al lanzamiento es reducir esta tasa de 30% a 8%.

## Criterios de aceptación

1. Antes de confirmar, se muestra la identidad del destino y el monto correspondientes al intento.
2. Si el importe excede cualquiera de los límites aplicables configurados para proveedor/canal, el envío se bloquea; si falta configuración, la ruta no está disponible.
3. Mientras el resultado sea desconocido, se muestra “En proceso” y no se permite iniciar otro pago para el mismo checkout.
4. Un intento tiene un único resultado terminal vigente. Timeout y respuesta ambigua no se presentan como fallo o expiración; una corrección requiere confirmación autoritativa y queda auditada.
5. En éxito confirmado, se muestra una referencia y se guarda el pago en el historial con destino enmascarado, monto/moneda y fecha/hora.
6. Ante fallo o expiración confirmados sin cargo, se ofrece volver o iniciar un nuevo intento; repetir solicitudes técnicas no duplica el cargo.
7. Cada éxito confirmado genera exactamente un evento `payer_payment_succeeded`; pagos pendientes, fallidos, expirados, de prueba y totalmente revertidos antes del cierre mensual no cuentan para MAU.
8. La tasa de abandono se calcula por `checkout_id` único con ventana de 24 horas y se monitorea contra la meta de 30% a 8% durante el trimestre posterior al lanzamiento; los pendientes se reportan por separado.

## Evidencia esperada

- Resultados de pruebas para éxito, fallo confirmado, expiración confirmada, timeout, notificación tardía y reintento.
- Checklist de límites, integridad de destino/monto y prevención de duplicados.
- Muestra no sensible de referencia, historial y evento analítico de pago exitoso.
- Reporte de abandono y MAU con la definición aprobada por Producto.

## Decisiones cerradas

- No hay un importe global fijo para “bajo monto”; aplica el menor límite vigente entre regulación, contrato y tope MVP configurado. Una ruta sin configuración se mantiene deshabilitada.
- El estado desconocido se mantiene “En proceso”; timeout no significa fallo. La expiración exige confirmación de no-cargo. Al agotarse la ventana documentada del proveedor, se deriva a conciliación y se bloquea un nuevo intento.
- Los estados visibles y la política de reintentos son los definidos en “Decisiones de producto”; solo se reintenta tras confirmación definitiva de no-cargo.
- Abandono usa checkout único y ventana de 24 horas; los pendientes se informan separadamente.
- MAU cuenta usuarios únicos con pago elegible exitoso en el mes calendario; la activación de wallet no basta. Pagos totalmente revertidos antes del cierre se excluyen; reversiones parciales no eliminan la actividad.
- El historial muestra destino enmascarado, monto/moneda, fecha/hora y referencia interna no sensible.
