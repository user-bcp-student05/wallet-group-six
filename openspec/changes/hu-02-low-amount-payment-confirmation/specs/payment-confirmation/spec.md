# Spec Delta

## Purpose

Permitir al pagador revisar destino y monto y confirmar una transferencia de bajo monto solo por rutas completamente configuradas, sin iniciar pagos duplicados. Fuente: [Issue #2](https://github.com/user-bcp-student05/wallet-group-six/issues/2); preserva el contrato de HU-02 sin excepciones.

## ADDED Requirements

### Requirement: Revision de destino y monto antes de confirmar

El sistema MUST mostrar, antes de la confirmacion, la identidad del destino y el monto correspondientes al intento que se va a confirmar.

#### Scenario: Revision previa
- **WHEN** el pagador llega al paso de confirmacion de un intento
- **THEN** observa la identidad del destino y el monto de ese intento antes de poder confirmar

### Requirement: Definicion de bajo monto por limite aplicable

Un importe MUST considerarse de bajo monto solo si es mayor que cero y menor o igual al menor de los limites aplicables: regulatorio, contractual del proveedor y tope MVP configurado para el proveedor/canal. No MUST existir un importe global fijo.

#### Scenario: Importe dentro del menor limite
- **WHEN** el importe es mayor que cero y no excede ninguno de los limites aplicables
- **THEN** el envio se permite

#### Scenario: Importe que excede algun limite
- **WHEN** el importe excede cualquiera de los limites aplicables configurados, aunque cumpla los demas
- **THEN** el envio se bloquea

#### Scenario: Importe cero o negativo
- **WHEN** el importe no es mayor que cero
- **THEN** el envio se bloquea

#### Scenario: Limite menor que el maximo regulatorio
- **WHEN** el limite contractual o el tope MVP del proveedor/canal es inferior al limite regulatorio
- **THEN** se aplica el menor de ellos

### Requirement: Ruta sin configuracion no habilitada

El sistema MUST NOT habilitar una ruta de pago si falta cualquiera de sus limites aplicables o su configuracion. Los parametros contractuales por proveedor MUST ser configuracion obligatoria de lanzamiento.

#### Scenario: Falta un limite o su configuracion
- **WHEN** a una ruta de proveedor/canal le falta algun limite aplicable o su configuracion
- **THEN** la ruta no esta disponible para pagar

### Requirement: Un pago activo por checkout

Mientras el resultado de un intento sea desconocido, el sistema MUST NOT permitir iniciar otro pago para el mismo checkout.

#### Scenario: Segundo pago con resultado desconocido
- **WHEN** el intento de un checkout esta "En proceso" y el pagador intenta pagar de nuevo ese checkout
- **THEN** no se inicia otro pago

### Requirement: Evidencia de limites, integridad y duplicados

La entrega MUST incluir un checklist verificable de limites, integridad de destino/monto y prevencion de duplicados.

#### Scenario: Checklist disponible
- **WHEN** se revisa la evidencia de verificacion
- **THEN** existe un checklist que cubre limites, integridad de destino/monto y prevencion de duplicados con resultado por elemento
