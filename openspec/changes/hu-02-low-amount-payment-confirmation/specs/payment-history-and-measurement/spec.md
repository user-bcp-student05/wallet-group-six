# Spec Delta

## Purpose

Dejar constancia visible de los pagos exitosos y medir actividad de pago (MAU) y abandono de checkout. Fuente: [Issue #2](https://github.com/user-bcp-student05/wallet-group-six/issues/2); preserva el contrato de HU-02 sin excepciones.

## ADDED Requirements

### Requirement: Referencia visible no sensible

En un exito confirmado, el sistema MUST mostrar una referencia que sea un identificador interno estable y no sensible. La referencia del proveedor MUST conservarse asociada internamente y MUST NOT ser la referencia visible.

#### Scenario: Referencia tras exito
- **WHEN** un pago se confirma como exitoso
- **THEN** se muestra una referencia interna estable y no sensible, y la referencia del proveedor queda asociada sin exponerse

### Requirement: Historial de pagos exitosos

El sistema MUST guardar cada pago exitoso en el historial con destino enmascarado, monto y moneda, fecha/hora y referencia.

#### Scenario: Pago en historial
- **WHEN** se consulta el historial tras un pago exitoso
- **THEN** aparece una entrada con destino enmascarado, monto y moneda, fecha/hora y referencia

#### Scenario: Pagos no exitosos
- **WHEN** un pago esta "En proceso", "No se pudo realizar" o "Expirado"
- **THEN** no aparece como pago exitoso en el historial

### Requirement: Evento payer_payment_succeeded

El sistema MUST emitir el evento `payer_payment_succeeded` exactamente una vez por cada exito confirmado.

#### Scenario: Exito confirmado
- **WHEN** un pago se confirma como exitoso
- **THEN** se emite un solo `payer_payment_succeeded`

#### Scenario: Confirmacion repetida
- **WHEN** se recibe de nuevo la confirmacion del mismo exito
- **THEN** no se emite un segundo evento

#### Scenario: Pago no exitoso
- **WHEN** el pago esta pendiente, fallido o expirado
- **THEN** no se emite `payer_payment_succeeded`

### Requirement: Definicion de MAU

El reporte MUST contar como MAU las wallets unicas con al menos una transferencia elegible confirmada como exitosa en el mes calendario. MUST excluir pagos pendientes, fallidos, expirados, de prueba, y anulados o totalmente revertidos antes del cierre del mes. Una reversion parcial MUST NOT eliminar la actividad. La activacion de wallet por si sola MUST NOT contar.

#### Scenario: Varias transferencias exitosas
- **WHEN** una wallet tiene varias transferencias elegibles exitosas en el mes
- **THEN** cuenta una sola vez

#### Scenario: Wallet solo activada
- **WHEN** una wallet se activo pero no tiene pagos exitosos en el mes
- **THEN** no cuenta en MAU

#### Scenario: Reversion total antes del cierre
- **WHEN** la unica transferencia exitosa de una wallet se anula o revierte totalmente antes del cierre del mes
- **THEN** la wallet no cuenta en MAU

#### Scenario: Reversion parcial
- **WHEN** una transferencia exitosa se revierte solo parcialmente
- **THEN** la wallet sigue contando en MAU

#### Scenario: Pagos de prueba o no exitosos
- **WHEN** los unicos pagos de una wallet son de prueba, pendientes, fallidos o expirados
- **THEN** la wallet no cuenta en MAU

### Requirement: Definicion de abandono

La tasa de abandono MUST calcularse por `checkout_id` unico con ventana de 24 horas: checkouts iniciados que no alcanzan exito confirmado dentro de las 24 horas / checkouts iniciados. Los checkouts pendientes MUST reportarse por separado. El reporte MUST permitir monitorear la tasa contra la meta de 30% a 8% durante el trimestre posterior al lanzamiento.

#### Scenario: Checkout con exito dentro de 24 horas
- **WHEN** un checkout alcanza exito confirmado dentro de las 24 horas de iniciado
- **THEN** no cuenta como abandonado

#### Scenario: Checkout sin exito tras 24 horas
- **WHEN** pasan 24 horas sin exito confirmado y el checkout no esta pendiente
- **THEN** cuenta como abandonado

#### Scenario: Checkout pendiente
- **WHEN** el resultado de un checkout sigue pendiente
- **THEN** se reporta aparte de los abandonados

#### Scenario: Multiples intentos en un checkout
- **WHEN** un mismo `checkout_id` tiene varios intentos
- **THEN** se cuenta una sola vez en numerador y denominador

### Requirement: Evidencia de referencia, historial, evento y reportes

La entrega MUST incluir una muestra no sensible de referencia, historial y evento `payer_payment_succeeded`, y los reportes de abandono y MAU con la definicion aprobada por Producto.

#### Scenario: Evidencia disponible
- **WHEN** se revisa la evidencia de verificacion
- **THEN** existen la muestra no sensible y los reportes de abandono y MAU conforme a las definiciones anteriores
