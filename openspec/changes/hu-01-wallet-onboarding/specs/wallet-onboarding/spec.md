# Spec Delta

## Purpose

Permitir a usuarios de smartphone del MVP en Peru completar un registro guiado y activar su wallet solo al satisfacer la politica KYC aprobada. Fuente: [Issue #1](https://github.com/user-bcp-student05/wallet-group-six/issues/1); preserva el contrato de HU-01 sin excepciones.

## ADDED Requirements

### Requirement: Politica KYC aprobada como condicion de lanzamiento

El sistema MUST usar la politica vigente aprobada por Cumplimiento para el segmento MVP en Peru como unica fuente de requisitos KYC y documentales. Su configuracion MUST ser obligatoria para el lanzamiento; no se inventan documentos ni validaciones adicionales.

#### Scenario: Politica disponible
- **WHEN** se evalua el onboarding con una politica vigente aprobada y configurada
- **THEN** los documentos y verificaciones exigidos corresponden a esa politica, sin agregar requisitos ajenos a ella

#### Scenario: Politica no disponible
- **WHEN** se verifica la preparacion de lanzamiento y falta la politica aprobada o su configuracion
- **THEN** no se considera cumplida la condicion de lanzamiento y no se habilita una activacion que eluda KYC

### Requirement: Secuencia y paso actual visibles

El flujo MUST mostrar la secuencia de pasos requeridos y el paso actual en todo momento durante el onboarding.

#### Scenario: Recorrido guiado
- **WHEN** el usuario inicia, avanza o reanuda el onboarding
- **THEN** puede observar la secuencia requerida y distinguir el paso actual, tambien en estados de correccion o revision pendiente

### Requirement: Bloqueo y correccion de informacion obligatoria

El flujo MUST bloquear el avance ante informacion obligatoria faltante o invalida. MUST indicar la accion de correccion permitida por la politica KYC, sin ofrecer acciones que esa politica no permite.

#### Scenario: Dato obligatorio ausente
- **WHEN** el usuario intenta continuar sin informacion obligatoria
- **THEN** permanece bloqueado y observa que informacion falta y como completarla conforme a la politica

#### Scenario: Dato invalido corregible
- **WHEN** la informacion es invalida y la politica permite corregirla
- **THEN** se bloquea el avance y se indica la correccion requerida; solo se permite continuar al satisfacer la validacion aplicable

### Requirement: Revision KYC pendiente sin activacion

El sistema MUST mantener la wallet sin activar mientras una revision KYC requerida no este aprobada. Si el caso requiere revision, el flujo MUST informar que esta pendiente y el siguiente paso permitido por la politica.

#### Scenario: Caso pendiente de revision
- **WHEN** los datos han sido enviados pero la revision KYC requerida sigue pendiente
- **THEN** el usuario observa el estado pendiente y el siguiente paso permitido, sin activacion ni habilitacion del acceso a pagos de bajo monto

#### Scenario: Verificacion no aprobada
- **WHEN** una verificacion requerida carece de aprobacion, aunque los pasos de captura esten completos
- **THEN** la wallet sigue sin activar y el flujo no presenta la captura completa como aprobacion KYC

### Requirement: Activacion condicionada y acceso a pagos

El sistema MUST activar la wallet solo con todos los pasos obligatorios completos, informacion valida y verificaciones KYC requeridas aprobadas. La finalizacion MUST corresponder a `wallet_active` persistido; entonces MUST habilitarse el punto de entrada a pagos de bajo monto.

#### Scenario: Flujo completo y aprobado
- **WHEN** todos los pasos obligatorios estan completos, la informacion es valida y las verificaciones requeridas estan aprobadas
- **THEN** la wallet queda activa al persistirse `wallet_active` y se habilita el punto de entrada a pagos de bajo monto

#### Scenario: Paso obligatorio pendiente
- **WHEN** existe al menos un paso obligatorio incompleto, aunque otras verificaciones esten aprobadas
- **THEN** la wallet no se activa y no se habilita el punto de entrada a pagos

#### Scenario: Activacion aun no persistida
- **WHEN** las condiciones KYC estan satisfechas pero `wallet_active` aun no esta persistido
- **THEN** el onboarding no se contabiliza como finalizado ni se presenta como una activacion completada

### Requirement: Reanudacion sin reinicio del inicio

El sistema MUST conservar como inicio la primera seleccion de "Comenzar". Reanudar el flujo MUST mantener ese timestamp y mostrar el estado actual del onboarding, sin tratar la reanudacion como un nuevo inicio.

#### Scenario: Pausa y reanudacion
- **WHEN** el usuario selecciona "Comenzar", pausa el recorrido y lo reanuda posteriormente
- **THEN** se conserva el timestamp original y el usuario ve el paso actual y las acciones permitidas por su estado

### Requirement: Limites de pago fuera del onboarding

El onboarding MUST NOT comunicar limites numericos por proveedor. MUST preservar la responsabilidad de la pantalla de pago de validar y mostrar el limite efectivo antes de confirmar; esta spec no define limites ni implementa el pago.

#### Scenario: Registro sin limites por proveedor
- **WHEN** el usuario recorre el onboarding hasta la activacion
- **THEN** no se muestran limites numericos por proveedor ni se presentan como garantizados para una transaccion futura

### Requirement: Evidencia de cumplimiento sin documentos reales

La evidencia de aceptacion MUST incluir resultados para flujo completo, datos invalidos, KYC pendiente y aprobado, y reanudacion. MUST incluir un checklist con la version de la politica KYC aplicada, sin adjuntar documentos reales de usuarios.

#### Scenario: Revision de aceptacion
- **WHEN** se revisa la evidencia del onboarding
- **THEN** se encuentran los resultados de esos casos y un checklist que identifica la version de la politica aplicada sin documentos reales de usuarios