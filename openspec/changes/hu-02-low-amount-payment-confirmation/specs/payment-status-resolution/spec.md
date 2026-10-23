# Spec Delta

## Purpose

Comunicar con veracidad el estado de cada pago y garantizar que no se cobre dos veces. Fuente: [Issue #2](https://github.com/user-bcp-student05/wallet-group-six/issues/2); preserva el contrato de HU-02 sin excepciones.

## ADDED Requirements

### Requirement: Estados visibles

El sistema MUST presentar unicamente los estados "En proceso", "Pago realizado", "No se pudo realizar" y "Expirado". "En proceso" MUST NOT ser terminal.

#### Scenario: Resultado desconocido
- **WHEN** el resultado del intento aun no es definitivo
- **THEN** se muestra "En proceso"

#### Scenario: Exito confirmado
- **WHEN** el proveedor confirma el pago
- **THEN** se muestra "Pago realizado"

#### Scenario: Fallo confirmado sin cargo
- **WHEN** el proveedor confirma definitivamente que el pago fallo sin cargo
- **THEN** se muestra "No se pudo realizar"

#### Scenario: Expiracion confirmada
- **WHEN** el proveedor confirma que el intento vencio sin completar el pago
- **THEN** se muestra "Expirado"

### Requirement: Timeout y ambiguedad no son fallo ni expiracion

Un timeout de comunicacion o una respuesta ambigua MUST NOT interpretarse ni presentarse como fallo o expiracion.

#### Scenario: Timeout de comunicacion
- **WHEN** ocurre un timeout al comunicarse con el proveedor
- **THEN** el intento permanece "En proceso" y no se ofrece un nuevo intento

#### Scenario: Respuesta ambigua
- **WHEN** el proveedor responde sin una senal de resultado definitivo
- **THEN** el intento permanece "En proceso"

### Requirement: Documentacion y ventana de consulta por proveedor

Cada proveedor MUST tener documentados antes del lanzamiento sus estados, senales de resultado definitivo y ventana maxima de consulta. Si el estado sigue desconocido al terminar esa ventana, el intento MUST permanecer bloqueado para reintentos y derivarse a conciliacion operativa; no MUST presumirse que fallo.

#### Scenario: Ventana agotada con estado desconocido
- **WHEN** termina la ventana maxima de consulta y el estado sigue desconocido
- **THEN** el intento se deriva a conciliacion operativa, permanece bloqueado para reintentos y no se muestra como fallido

#### Scenario: Proveedor sin documentacion
- **WHEN** un proveedor no tiene documentados sus estados, senales definitivas y ventana de consulta
- **THEN** no se considera cumplida la condicion de lanzamiento de su ruta

### Requirement: Resultado terminal unico y correccion auditable

Cada intento MUST tener un unico resultado terminal vigente. Una notificacion repetida del mismo resultado MUST procesarse una sola vez. Una notificacion contradictoria MUST NOT cambiar silenciosamente un resultado terminal: requiere confirmacion autoritativa del proveedor, registro auditable y conciliacion antes de corregir el estado visible.

#### Scenario: Notificacion repetida
- **WHEN** llega de nuevo una notificacion del mismo resultado ya procesado
- **THEN** no se produce ningun efecto adicional

#### Scenario: Notificacion tardia que resuelve un intento pendiente
- **WHEN** llega la confirmacion definitiva de un intento que estaba "En proceso", incluso tras agotarse la ventana
- **THEN** se registra el resultado como terminal conforme a la confirmacion y queda auditado

#### Scenario: Notificacion contradictoria
- **WHEN** llega una notificacion que contradice un resultado terminal vigente
- **THEN** el estado visible no cambia hasta contar con confirmacion autoritativa, registro auditable y conciliacion

### Requirement: Idempotencia y reintento

Repetir una solicitud tecnica MUST conservar la misma clave de idempotencia y MUST NOT duplicar el cargo. Un nuevo intento MUST iniciarse solo tras confirmacion definitiva de no-cargo, con nueva autorizacion del pagador y una nueva clave.

#### Scenario: Solicitud tecnica repetida
- **WHEN** se repite la misma solicitud tecnica de un intento
- **THEN** se usa la misma clave de idempotencia y no se genera un cargo adicional

#### Scenario: Reintento tras fallo o expiracion sin cargo
- **WHEN** el intento termina en "No se pudo realizar" o "Expirado" con no-cargo confirmado
- **THEN** se ofrece volver o iniciar un nuevo intento, que requiere nueva autorizacion del pagador y una nueva clave

#### Scenario: Reintento sin confirmacion de no-cargo
- **WHEN** el intento esta "En proceso" o derivado a conciliacion
- **THEN** no se permite iniciar un nuevo intento

### Requirement: Evidencia de estados y reintentos

La entrega MUST incluir resultados de pruebas para exito, fallo confirmado, expiracion confirmada, timeout, notificacion tardia y reintento.

#### Scenario: Pruebas disponibles
- **WHEN** se revisa la evidencia de verificacion
- **THEN** existen resultados de prueba para cada uno de esos seis casos
