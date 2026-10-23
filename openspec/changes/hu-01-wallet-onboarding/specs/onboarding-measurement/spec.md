# Spec Delta

## Purpose

Medir la finalizacion del onboarding, la actividad mensual y la adquisicion del MVP en Peru con eventos no sensibles y formulas verificables. Fuente: [Issue #1](https://github.com/user-bcp-student05/wallet-group-six/issues/1); se conservan las definiciones de HU-01 sin cambiar sus ventanas ni exclusiones.

## ADDED Requirements

### Requirement: Eventos del ciclo de onboarding

El sistema MUST registrar `onboarding_started` al primer "Comenzar", `onboarding_step_completed` por paso completado, `onboarding_activated` al persistir `wallet_active` y `onboarding_closed` por cierre. Cada evento MUST generarse una sola vez por ocurrencia, sin confundir reanudacion con inicio.

#### Scenario: Inicio y reanudacion
- **WHEN** el usuario selecciona "Comenzar" por primera vez y luego pausa y reanuda el mismo onboarding
- **THEN** existe un solo `onboarding_started` con el timestamp del primer inicio y la reanudacion no genera otro inicio

#### Scenario: Pasos, activacion y cierre
- **WHEN** se completan dos pasos requeridos, se persiste `wallet_active` y ocurre un cierre
- **THEN** se observan dos `onboarding_step_completed`, un `onboarding_activated` y un `onboarding_closed`, cada uno asociado a su ocurrencia

#### Scenario: Duplicacion de una misma ocurrencia
- **WHEN** una misma ocurrencia de inicio, paso completado, activacion o cierre se procesa mas de una vez
- **THEN** la muestra analitica contiene solo un evento correspondiente a esa ocurrencia

#### Scenario: Cierre sin activacion
- **WHEN** el usuario cierra un onboarding sin que `wallet_active` este persistido
- **THEN** se observa `onboarding_closed` por ese cierre, sin `onboarding_activated` ni finalizacion atribuida a ese cierre

### Requirement: Privacidad y referencia temporal de eventos

Cada evento analitico MUST incluir un timestamp UTC y un identificador seudonimo. La analitica MUST NOT enviar documentos ni datos sensibles de usuarios.

#### Scenario: Inspeccion de eventos
- **WHEN** se inspecciona una muestra de los cuatro tipos de eventos de onboarding
- **THEN** cada evento tiene timestamp UTC e identificador seudonimo, sin documentos ni datos sensibles

### Requirement: Duracion transcurrida hasta activacion persistida

La duracion primaria MUST calcularse desde el primer `onboarding_started` hasta la persistencia de `wallet_active`, incluyendo pausas y revisiones. Reanudar MUST NOT reiniciar el timestamp de inicio.

#### Scenario: Pausa incluida en la duracion
- **WHEN** el primer inicio ocurre a las 08:00 UTC, el usuario pausa a las 08:02, reanuda a las 08:10 y `wallet_active` se persiste a las 08:12 del mismo dia
- **THEN** la duracion primaria es 12 minutos, no 4 ni 2 minutos

#### Scenario: Tiempo de revision incluido
- **WHEN** el onboarding permanece pendiente de revision entre el primer inicio y la activacion persistida
- **THEN** la duracion incluye todo ese intervalo de revision, sin descontarlo como tiempo ajeno al onboarding

### Requirement: Mediana y flujos no finalizados

La mediana de finalizacion MUST incluir solo onboardings con `wallet_active` persistido. Los flujos pendientes e incompletos MUST informarse por separado y MUST NOT entrar en esa mediana.

#### Scenario: Calculo con pendientes e incompletos
- **WHEN** hay tres onboardings finalizados con duraciones de 2, 4 y 9 minutos, uno pendiente de KYC y otro incompleto
- **THEN** la mediana es 4 minutos y el pendiente y el incompleto se reportan por separado, sin asignarles una duracion de finalizacion

### Requirement: Reporte y seguimiento de metas del MVP

El reporte mensual MUST presentar inicios, activaciones, MAU y CAC. Para la cohorte MVP en Peru MUST monitorearse la mediana contra la meta de 8 a 3 minutos durante el trimestre posterior al lanzamiento; MAU MUST compararse con 1,000-2,000 y CAC con PEN 10 durante los meses 1-3.

#### Scenario: Reporte mensual posterior al lanzamiento
- **WHEN** se consulta el reporte de uno de los meses 1-3 para la cohorte MVP en Peru
- **THEN** se muestran inicios, activaciones, MAU y CAC calculados segun este contrato, junto con sus metas de MAU y CAC y el seguimiento de la mediana contra la meta del trimestre

#### Scenario: Meta aun no alcanzada
- **WHEN** las mediciones no alcanzan una meta de negocio
- **THEN** el reporte refleja el valor observado y su comparacion con la meta sin alterar formulas, excluir pausas ni contar pendientes como finalizados para aparentar cumplimiento

### Requirement: MAU por wallets unicas con actividad elegible

MAU MUST contar wallets unicas con al menos una transaccion elegible confirmada exitosa durante el mes calendario. Activar una wallet por si solo MUST NOT contar como MAU, y varias transacciones elegibles de una misma wallet MUST contar una sola wallet.

#### Scenario: Activacion y multiples pagos
- **WHEN** en un mes una wallet se activa sin transacciones y otra confirma tres transacciones elegibles exitosas
- **THEN** esas dos wallets aportan un total de 1 al MAU del mes

#### Scenario: Separacion de meses calendario
- **WHEN** una wallet tiene actividad elegible exitosa solo en el mes calendario anterior y ninguna en el mes reportado
- **THEN** esa actividad no aporta al MAU del mes reportado

### Requirement: Exclusiones y reversiones de MAU

MAU MUST excluir pagos de prueba, anulados y totalmente revertidos antes del cierre del mes. Una reversion parcial MUST NOT eliminar la actividad elegible de la wallet.

#### Scenario: Actividad excluida
- **WHEN** tres wallets solo tienen, respectivamente, pagos de prueba, pagos anulados y pagos totalmente revertidos antes del cierre del mes
- **THEN** ninguna de esas wallets aporta al MAU por esos pagos

#### Scenario: Reversion parcial
- **WHEN** una wallet tiene una transaccion elegible confirmada exitosa en el mes y esta se revierte solo parcialmente
- **THEN** la wallet sigue contando una vez en el MAU de ese mes

### Requirement: Atribucion de primer contacto y ventana de activacion

La atribucion para CAC MUST ser de primer contacto y capturarse al inicio del onboarding, con una ventana de 30 dias para activar. Los contactos posteriores MUST NOT reemplazar el primer contacto para atribuir la activacion.

#### Scenario: Primer contacto conservado
- **WHEN** se captura un primer contacto A al iniciar, aparece despues un contacto B y la wallet se activa 20 dias despues del inicio
- **THEN** la activacion se atribuye a A, no a B, dentro de la ventana de 30 dias

#### Scenario: Activacion fuera de ventana
- **WHEN** una wallet se activa 31 dias despues del inicio atribuido
- **THEN** esa activacion no integra el denominador atribuible de CAC por ese contacto fuera de ventana

### Requirement: Formula mensual y costos de CAC

CAC MUST calcularse mensualmente como gasto directo de adquisicion atribuible dividido entre nuevos usuarios con wallet activada atribuibles. El gasto MUST incluir medios pagados, agencias o afiliados y recompensas de referido; MUST excluir costos de producto y gastos generales.

#### Scenario: Calculo con costos elegibles
- **WHEN** un mes tiene PEN 100 de medios pagados, PEN 20 de agencias o afiliados, PEN 10 de recompensas de referido, PEN 50 de producto, PEN 40 de gastos generales y 13 nuevos usuarios activados atribuibles dentro de ventana
- **THEN** el gasto elegible es PEN 130 y el CAC es PEN 10, excluyendo los PEN 90 de producto y gastos generales

### Requirement: Usuarios y gastos sin atribucion separados

El reporte MUST presentar los usuarios y gastos sin atribucion por separado, sin reasignarlos a contactos o canales atribuidos ni incorporarlos a los componentes atribuibles de CAC.

#### Scenario: Datos sin atribucion
- **WHEN** ademas de los PEN 130 y 13 activados atribuibles del ejemplo anterior existen PEN 30 de gasto y dos usuarios activados sin atribucion
- **THEN** el CAC atribuible sigue siendo PEN 10 y los PEN 30 y los dos usuarios aparecen separados, sin reasignacion

### Requirement: Evidencia analitica verificable y no sensible

La evidencia de aceptacion MUST incluir una muestra de eventos, timestamps y atribucion con datos de prueba no sensibles, y un reporte de onboarding, MAU y CAC que permita verificar las formulas y ventanas definidas.

#### Scenario: Revision de evidencia de medicion
- **WHEN** se revisa la evidencia de aceptacion de las metricas
- **THEN** la muestra no sensible permite comprobar eventos y atribucion, y el reporte permite reproducir duraciones, mediana, MAU y CAC conforme a este contrato