# Tasks

Unidad de trabajo del producto: [GitHub Issue #2 - HU-02](https://github.com/user-bcp-student05/wallet-group-six/issues/2). Este archivo es exclusivamente el checklist de implementacion del cambio `hu-02-low-amount-payment-confirmation`; no crea historias nuevas ni amplia el compromiso del Issue.

Referencias aprobadas: [proposal.md](proposal.md), [payment-confirmation](specs/payment-confirmation/spec.md), [payment-status-resolution](specs/payment-status-resolution/spec.md), [payment-history-and-measurement](specs/payment-history-and-measurement/spec.md) y [design.md](design.md). El baseline protegido es [HU-02-Pago-contacto.md](../../../issues/HU-02-Pago-contacto.md).

Ejecutar por dependencia. El grupo 1 precede a cualquier cambio funcional. Hoy no hay implementacion que caracterizar: no inventar resultados existentes. Si aparece codigo antes de ejecutar este checklist, capturar su comportamiento antes de modificarlo; si sigue vacio, establecer primero las pruebas de contrato. Las pruebas de funcionalidades ausentes pueden fallar inicialmente por la ausencia esperada y deben pasar con su incremento. No marcar una tarea sin su evidencia de verificacion.

Todo el trabajo queda en `wallet-group-six`, sobre `main`, sin crear ni cambiar de rama. No incluir onboarding/KYC (HU-01), adaptadores reales de proveedores, valores de limites, procesos operativos de conciliacion, otros paises, bus de eventos ni redisenos globales. La eleccion del soporte minimo para ejecutar pruebas y codigo no autoriza scaffolding ajeno a este cambio. La habilitacion de una ruta real queda condicionada a insumos externos (documentacion y limites del proveedor).

## 1. Caracterizacion y contratos antes del cambio funcional

- [ ] 1.1 Registrar el estado inicial de los directorios tecnicos y preparar el arnes minimo de pruebas del cambio, reutilizando cualquier herramienta existente; verificar un comando reproducible de ejecucion y documentar explicitamente si no hay implementacion que caracterizar, sin agregar codigo funcional.
- [ ] 1.2 Crear un adaptador de proveedor simulado que emita las salidas del modelo comun (aceptado, exito definitivo, no-cargo definitivo, expirado, desconocido, timeout); verificar que cada salida es reproducible en pruebas y que ninguna usa datos reales.
- [ ] 1.3 Crear casos de contrato para limites: importe 0, negativo, igual y justo por encima de cada limite, limite contractual menor que el regulatorio, y falta de un limite o de su configuracion; verificar su correspondencia con los escenarios de payment-confirmation.
- [ ] 1.4 Crear casos de contrato para integridad de destino y monto (cambio entre revision y confirmacion) y para un unico pago activo por checkout; verificar que las expectativas exigen nueva revision y rechazan un segundo pago con resultado desconocido.
- [ ] 1.5 Crear casos de contrato para estados: exito, fallo sin cargo, expiracion confirmada, timeout y respuesta ambigua; verificar que solo las senales definitivas producen un terminal y que timeout y ambiguedad se muestran "En proceso".
- [ ] 1.6 Crear casos para ventana agotada con estado desconocido, confirmacion tardia posterior, notificacion repetida, contradictoria y no verificable, y llegada de la notificacion antes de la respuesta sincrona; verificar bloqueo de reintentos, derivacion a conciliacion, ausencia de cambio visible ante contradiccion y registro auditable.
- [ ] 1.7 Crear casos de idempotencia y reintento: solicitud tecnica repetida, misma clave con contenido distinto, dos confirmaciones concurrentes del mismo checkout, respuesta perdida tras envio, y reintento tras no-cargo, durante "En proceso" y durante conciliacion; verificar un unico cargo y que el nuevo intento exige nueva autorizacion y nueva clave.
- [ ] 1.8 Crear casos de integracion para exito con historial y evento: exito unico, confirmacion repetida, fallo tras commit antes de publicar y reentrega; verificar una entrada de historial, una referencia y un `payer_payment_succeeded` sin perdida ante caidas, sin considerar un mock evidencia de transacciones reales.
- [ ] 1.9 Crear fixtures sinteticos y expectativas para MAU (varias exitosas de una wallet, solo activacion, prueba, reversion total y parcial, cambio de mes en America/Lima) y abandono (exito a 23h59 y despues de 24h, pendiente, varios intentos por checkout, cero iniciados); verificar que reproducen los escenarios de payment-history-and-measurement sin datos sensibles.

## 2. Configuracion y limites por ruta

- [ ] 2.1 Incorporar de forma aditiva la configuracion por proveedor/canal (limites regulatorio, contractual y tope MVP, ventana de consulta y mapa de estados documentado) sin valores por defecto; verificar que falta de cualquier elemento deja la ruta deshabilitada.
- [ ] 2.2 Implementar el calculo del limite efectivo como el minimo de los limites y su registro de version en cada intento; verificar con los casos de 1.3 que solo se permiten importes mayores que cero y menores o iguales al minimo.
- [ ] 2.3 Implementar la habilitacion de ruta con bandera independiente que exige configuracion completa y validada; verificar que una ruta incompleta no esta disponible para pagar y que apagar la bandera no afecta intentos existentes.
- [ ] 2.4 Registrar como prerrequisito externo la documentacion del proveedor y los limites aprobados con su estado de disponibilidad; verificar un checklist de lanzamiento que no acepte un fixture como aprobacion real y que impida habilitar la ruta mientras falte.

## 3. Nucleo de intentos, idempotencia y estados

- [ ] 3.1 Incorporar de forma aditiva la persistencia del intento inmutable (destino, monto, moneda, ruta, `checkout_id`, clave de idempotencia, version de configuracion), estados internos, version de estado y auditoria; verificar con el almacenamiento seleccionado que una transaccion fallida no deja escrituras parciales y que las restricciones de unicidad se cumplen.
- [ ] 3.2 Implementar la creacion y confirmacion del intento con revision de destino y monto, evaluacion de limite en servidor al crear y al confirmar, y unicidad de un intento no terminal por `checkout_id`; verificar los casos de 1.3 y 1.4.
- [ ] 3.3 Implementar la idempotencia de peticion (misma clave y contenido devuelve el mismo intento; mismo clave con otro contenido se rechaza) y el envio al proveedor con la misma clave o consulta previa al reenvio; verificar los casos de 1.7 de repeticion, respuesta perdida y concurrencia con un unico cargo en el simulador.
- [ ] 3.4 Implementar la maquina de estados con proyeccion a los cuatro estados visibles, donde solo una senal definitiva del adaptador produce un terminal y el desconocido se muestra "En proceso"; verificar los casos de 1.5.
- [ ] 3.5 Implementar la consulta activa dentro de la ventana de la ruta y el paso a conciliacion al agotarse, con bloqueo de reintentos; verificar el caso de ventana agotada de 1.6 sin presentarlo como fallo.
- [ ] 3.6 Implementar el procesamiento de notificaciones con verificacion de autenticidad, deduplicacion, resolucion tardia y registro auditable de contradicciones sin cambio visible; verificar los casos de 1.6.
- [ ] 3.7 Implementar el nuevo intento solo desde fallo o expiracion con no-cargo confirmado, con nueva autorizacion y nueva clave; verificar los casos de reintento de 1.7 en cada estado.

## 4. Exito, historial y evento

- [ ] 4.1 Implementar la transicion a exito con asiento de historial (destino enmascarado, monto, moneda, fecha/hora, referencia interna) y registro pendiente de `payer_payment_succeeded` en una sola transaccion; verificar los casos de 1.8, incluida la ausencia de entrada o evento si falla el commit.
- [ ] 4.2 Implementar la generacion de la referencia interna estable y no sensible, y el almacenamiento interno de la referencia del proveedor sin exponerla; verificar que la respuesta, el historial y el evento no contienen la referencia del proveedor ni el destino completo.
- [ ] 4.3 Implementar el publicador del evento con entrega al menos una vez e identidad unica por intento, con deduplicacion en la proyeccion; verificar reentrega y confirmacion repetida con un solo evento contado.

## 5. Frontera y pantalla de pago

- [ ] 5.1 Exponer en la frontera autenticada la creacion y confirmacion de intento, la consulta de estado y la recepcion de notificaciones, validando pertenencia al pagador y clave de idempotencia; verificar que otro usuario es rechazado y que la frontera no replica reglas de limites ni interpreta senales del proveedor.
- [ ] 5.2 Implementar la pantalla de revision con destino y monto, los estados visibles, la oferta de volver o nuevo intento solo tras no-cargo confirmado, y el historial con referencia; verificar los escenarios de payment-confirmation y payment-status-resolution desde la interfaz, y que un timeout local no se muestra como fallo.
- [ ] 5.3 Verificar que ocultar el boton de pago no es el control de autorizacion: intentar pagar por la frontera sobre una ruta deshabilitada, un monto sobre limite o un checkout con intento activo debe ser rechazado.

## 6. Medicion

- [ ] 6.1 Implementar la proyeccion de MAU desde intentos exitosos elegibles con exclusiones de prueba, anulacion y reversion total, sin excluir la reversion parcial ni contar la activacion sola; verificar los casos de MAU de 1.9, incluido el limite de mes en America/Lima.
- [ ] 6.2 Implementar la proyeccion de abandono por `checkout_id` unico con ventana de 24 horas, pendientes aparte y varios intentos contados una vez, y el reporte contra la meta de 30% a 8%; verificar los casos de abandono de 1.9 y que cero iniciados no genera una division invalida.
- [ ] 6.3 Agregar a cada reporte periodo, fecha de corte, version de calculo y marca de dato no disponible cuando falte una fuente; verificar que la reconstruccion produce el mismo resultado y que una fuente ausente no se reemplaza por cero.

## 7. Validacion y evidencia

- [ ] 7.1 Ejecutar la suite completa de los grupos 1 a 6 contra el almacenamiento real de pruebas con inyeccion de fallos; verificar que todos los casos pasan y registrar el resultado y el comando utilizados.
- [ ] 7.2 Revisar los logs, eventos, respuestas e historial con datos sinteticos; verificar que no contienen la referencia del proveedor, el destino completo ni otros datos sensibles.
- [ ] 7.3 Reunir la evidencia esperada del Issue: resultados de pruebas de exito, fallo confirmado, expiracion confirmada, timeout, notificacion tardia y reintento; checklist de limites, integridad y duplicados; muestra no sensible de referencia, historial y evento; y reportes de abandono y MAU con la definicion aprobada por Producto.
- [ ] 7.4 Ejecutar la validacion de OpenSpec del cambio y comprobar que ningun requisito de las specs queda sin tarea ni evidencia correspondiente; registrar las brechas, sin ampliar el alcance.
- [ ] 7.5 Verificar el procedimiento de rollback de design.md en el entorno de pruebas: apagar la bandera de una ruta impide nuevos intentos, los intentos "En proceso" o en conciliacion siguen resolviendose, y no se borran terminales, historial, eventos ni auditoria.
- [ ] 7.6 Antes de habilitar una ruta real, confirmar con evidencia que cuenta con documentacion de estados, senales y ventana del proveedor, limites aprobados, senales definitivas validadas en el entorno de pruebas del proveedor y aprobacion de Producto de las definiciones de reporte; habilitar una ruta a la vez.
