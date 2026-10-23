# Design

## Context

La motivacion y el alcance aprobados estan en [proposal.md](proposal.md). Los contratos son [payment-confirmation](specs/payment-confirmation/spec.md), [payment-status-resolution](specs/payment-status-resolution/spec.md) y [payment-history-and-measurement](specs/payment-history-and-measurement/spec.md), derivados del [Issue #2](https://github.com/user-bcp-student05/wallet-group-six/issues/2) y del baseline [HU-02](../../../issues/HU-02-Pago-contacto.md). Este diseno desarrolla el siguiente artefacto autorizado; no modifica esos documentos ni sus requisitos.

Estado observado en `wallet-group-six` al 2026-10-23:

- OpenSpec usa `spec-driven`; existen la propuesta y tres specs, y no hay `tasks.md`.
- `front-end/`, `services/api/` y `services/back-end/` contienen solo un `readme.txt` vacio. No hay codigo, manifiestos, almacenamiento ni pruebas ejecutables, por lo que no hay comportamiento existente que migrar.
- La [epica](../../../docs/epic.md) establece un ecosistema multirriel con partners, que el MVP no debe asumir un unico riel, y senala como riesgo los reintentos sin idempotencia robusta (duplicidad financiera y desvios de conciliacion).
- Los contratos, estados, senales definitivas, ventanas de consulta y limites por proveedor son insumos externos de lanzamiento. Este repositorio no los contiene y no se suponen.
- HU-01 (onboarding) es una dependencia: HU-02 solo consume wallets ya activas. El diseno de HU-01 no se modifica.

Los componentes siguientes son responsabilidades propuestas, no sistemas existentes. Dado que no hay codigo previo, la "preservacion" de comportamiento se materializa como suite de contrato basada en las specs (ver Pruebas de caracterizacion).

## Goals / Non-Goals

**Goals:**

- Una autoridad unica de escritura para el ciclo de vida del pago, que garantice como maximo un cargo por checkout ante reintentos, concurrencia y notificaciones repetidas.
- Aislar las diferencias entre proveedores detras de una configuracion y un adaptador por ruta, sin que el nucleo asuma un riel.
- Tratar el resultado desconocido como estado de primera clase (`En proceso`) hasta confirmacion o conciliacion.
- Medicion (evento, MAU, abandono) reproducible y derivada de resultados confirmados.
- Despliegue incremental con una ruta deshabilitada por defecto y reversible sin perder pagos ni auditoria.

**Non-Goals:**

- Elegir framework, base de datos, nube, proveedor, formato de clave de idempotencia o URLs de una API inexistente.
- Implementar adaptadores reales de proveedores, el proceso operativo de conciliacion o los valores de limites.
- Disenar onboarding/KYC, checkout completo, contabilidad, un bus de eventos o event sourcing general.
- Cambiar definiciones, metas o formulas aprobadas.
- Escribir codigo, pruebas ejecutables, desplegar o producir `tasks.md` en esta entrega.

## Contratos protegidos

| Contrato | Proteccion tecnica | Referencia |
| --- | --- | --- |
| Destino y monto mostrados son los que se confirman | Confirmacion ligada a un intento inmutable (destino, monto, moneda, ruta); cualquier cambio exige nueva revision | payment-confirmation: Revision |
| Bajo monto = menor limite aplicable; sin configuracion no hay ruta | Evaluacion en servidor al crear y al confirmar el intento; ruta habilitada solo con configuracion completa y validada | payment-confirmation: Definicion; Ruta sin configuracion |
| Un pago activo por checkout | Restriccion de unicidad sobre intento no terminal por `checkout_id` | payment-confirmation: Un pago activo |
| Estados visibles y `En proceso` no terminal | Mapa cerrado de estados internos a los cuatro visibles; desconocido siempre se proyecta como `En proceso` | payment-status-resolution: Estados |
| Timeout/ambiguedad no son fallo ni expiracion | Solo senales definitivas documentadas del proveedor producen un estado terminal | payment-status-resolution: Timeout |
| Ventana agotada: bloqueo y conciliacion | Estado interno de conciliacion que bloquea reintentos y se muestra como `En proceso` | payment-status-resolution: Ventana |
| Un unico resultado terminal; notificaciones idempotentes; correccion auditada | Transicion terminal condicionada, deduplicacion de notificaciones y registro de contradicciones sin cambio visible | payment-status-resolution: Resultado terminal |
| Idempotencia y reintento solo tras no-cargo | Clave por intento; nuevo intento solo desde terminal con no-cargo confirmado, con nueva autorizacion y clave | payment-status-resolution: Idempotencia |
| Referencia visible interna y no sensible | Identificador interno generado por el sistema; referencia del proveedor en campo interno no expuesto | payment-history-and-measurement: Referencia |
| Un `payer_payment_succeeded` por exito | Evento registrado en la misma transaccion que la transicion a exito, con identidad unica por intento | payment-history-and-measurement: Evento |
| MAU y abandono segun definiciones | Proyecciones derivadas de intentos y checkouts confirmados, con exclusiones y corte explicitos | payment-history-and-measurement: MAU; Abandono |

## Componentes afectados

| Ubicacion propuesta | Responsabilidad | Limite |
| --- | --- | --- |
| `front-end/` | Pantalla de revision y confirmacion, estados visibles, oferta de nuevo intento, historial y referencia | No decide limites, estados ni resultados; no marca fallo por timeout local |
| `services/api/` | Frontera autenticada: crear/confirmar intento, consultar estado, recibir notificaciones de proveedor; valida pertenencia al pagador y la clave de idempotencia | No duplica reglas de limites ni interpreta senales de proveedor |
| `services/back-end/` (pagos) | Modulo de intentos: evaluacion de limites, maquina de estados, idempotencia, reintentos, auditoria | Unica autoridad de escritura del estado del pago |
| `services/back-end/` (adaptadores) | Un adaptador por ruta que traduce a un modelo comun (aceptado, definitivo exito, definitivo no-cargo, expirado, desconocido) segun la documentacion del proveedor | No exponen vocabulario del proveedor al nucleo ni a la UI |
| `services/back-end/` (configuracion) | Limites por proveedor/canal, ventana de consulta y estados documentados; habilitacion de ruta | Sin valores por defecto; falta = ruta deshabilitada |
| `services/back-end/` (medicion) | Evento, historial, MAU y abandono | Solo lee resultados confirmados; no procesa pagos |

Pueden convivir en un solo proceso de servidor; no se exigen despliegues separados.

## Decisions

### 1. Intento inmutable y maquina de estados con resultado desconocido explicito

Cada confirmacion crea un intento con destino, monto, moneda, ruta, `checkout_id` y clave de idempotencia fijos. Estados internos: `creado`, `enviado`, `en_proceso` (incluye desconocido), `en_conciliacion`, y terminales `exito`, `fallo_sin_cargo`, `expirado`. Se proyectan a los visibles: `en_proceso`/`en_conciliacion`/`enviado` a "En proceso". Solo el adaptador, ante una senal definitiva documentada, produce un terminal; un timeout o respuesta ambigua deja el estado sin cambio. Las transiciones se aplican con control de version, de modo que un unico resultado terminal es vigente.

Al agotarse la ventana de consulta de la ruta, el intento pasa a `en_conciliacion`: sigue visible como "En proceso", bloquea reintentos y se deriva a conciliacion. Una confirmacion posterior autoritativa puede resolverlo a terminal con registro auditable.

**Alternativas:** usar timeout como fallo simplifica la UI pero produce doble cobro (rechazado por contrato). Un estado visible "Desconocido" se descarta: el contrato fija cuatro estados.

### 2. Idempotencia en tres niveles

- Peticion del cliente: la clave del intento se conserva en reintentos tecnicos; el servidor devuelve el mismo intento si la clave y su contenido coinciden, y rechaza la misma clave con contenido distinto.
- Checkout: unicidad de un intento no terminal por `checkout_id` impide dos pagos activos.
- Proveedor: la misma clave del intento se envia al proveedor cuando lo soporte; si la ruta no ofrece idempotencia propia, el adaptador consulta el estado antes de reenviar, y una ruta sin garantia equivalente no se habilita.

Un nuevo intento tras no-cargo confirmado crea otra clave y requiere nueva autorizacion del pagador.

**Alternativas:** deduplicar solo por clave del cliente no cubre dos pestanas ni dispositivos; deduplicar solo por checkout no cubre el reenvio tecnico dentro del mismo intento. Se combinan.

### 3. Notificaciones y consulta como entradas equivalentes, deduplicadas

Notificaciones del proveedor y consultas activas pasan por el mismo camino de transicion. Una notificacion se identifica por intento y resultado; la repetida no produce efecto. Una contradictoria se registra en auditoria y no cambia el estado visible hasta confirmacion autoritativa y conciliacion. La autenticidad de la notificacion se verifica segun el mecanismo del proveedor antes de procesarla; una no verificable se descarta y se registra.

### 4. Configuracion obligatoria que habilita rutas

Una ruta solo esta habilitada si estan configurados y validados limite regulatorio, contractual y tope MVP del proveedor/canal, ventana de consulta y mapa de estados documentado. El limite efectivo es el minimo y se evalua en servidor al crear y al confirmar; la UI puede mostrarlo pero no decide. La version de configuracion usada queda registrada en el intento. Sin valores por defecto.

**Alternativas:** limite global fijo (rechazado por contrato); validar solo en cliente (eludible).

### 5. Exito, historial y evento en una sola transaccion

La transicion a `exito` persiste en la misma transaccion el asiento de historial (destino enmascarado, monto, moneda, fecha/hora, referencia interna) y el registro pendiente de `payer_payment_succeeded`, con identidad unica por intento. Un publicador entrega al menos una vez y la analitica deduplica por esa identidad, igual que en el diseno de HU-01. La referencia visible es un identificador interno generado; la del proveedor se guarda aparte y no se expone. El enmascarado se calcula al escribir y la analitica no recibe el destino completo.

**Alternativas:** emitir el evento antes del commit generaria exitos inexistentes; derivar historial desde el proveedor expondria su referencia.

### 6. Proyecciones de MAU y abandono con corte explicito

- MAU: wallets distintas con al menos un intento `exito` elegible en el mes calendario; excluye pruebas, anulados o totalmente revertidos antes del cierre; la reversion parcial no excluye. Requiere marca de prueba y estado de reversion como entradas autoritativas.
- Abandono: por `checkout_id` unico, numerador de iniciados sin exito en 24 horas (excluidos los pendientes), denominador de iniciados; pendientes se reportan aparte; varios intentos del mismo checkout cuentan una vez.
- Cada reporte identifica periodo, corte y version de calculo; la falta de una fuente se senala como no disponible, no como cero. El mes calendario se define en `America/Lima` con timestamps UTC almacenados, consistente con HU-01.

**Alternativas:** calcular desde eventos analiticos unicamente depende de su entrega; se prefiere derivar de intentos confirmados y usar el evento como salida.

## Estrategia incremental

1. **Contrato y fixtures:** escribir la suite de contrato desde las specs con un adaptador simulado que cubra las cinco salidas del modelo comun. Sin comportamiento visible.
2. **Nucleo de intentos:** maquina de estados, idempotencia, unicidad por checkout y auditoria detras de una bandera de habilitacion apagada.
3. **Configuracion y limites:** carga y validacion de limites por ruta; ruta deshabilitada si falta algo.
4. **Resolucion de estados:** consulta, notificaciones, ventana, conciliacion y contradicciones.
5. **Exito:** historial, referencia y evento transaccional.
6. **UI:** revision, estados, oferta de reintento e historial.
7. **Medicion:** proyecciones de MAU y abandono con reportes.
8. **Habilitacion por ruta:** una ruta a la vez, solo con documentacion del proveedor completa, evidencia de pruebas y aprobacion del reporte de definiciones.

Cada paso es desplegable y verificable por separado; ninguno habilita pagos reales hasta el paso 8.

## Pruebas de caracterizacion y aceptacion

No existe codigo que caracterizar. La matriz es primero una suite de contrato desde las specs y pasa a ser baseline de regresion antes de modificar cualquier ruta critica posterior; si en el futuro se sustituye una implementacion existente, se ejecuta la anterior y la nueva sobre los mismos casos y toda divergencia con las specs se trata como defecto, no como conducta a perpetuar. No se declara ninguna prueba ejecutada en esta entrega.

| Superficie critica | Casos previos a cambiar | Resultado esperado |
| --- | --- | --- |
| Limites y habilitacion | Importe 0, negativo, igual y justo por encima de cada limite; limite contractual menor que el regulatorio; falta de un limite o de configuracion | Solo permite importes dentro del minimo; ruta sin configuracion no disponible |
| Integridad destino/monto | Cambio de destino o monto entre revision y confirmacion | Se exige nueva revision; el intento confirmado coincide con lo mostrado |
| Estados y desconocido | Exito, fallo sin cargo, expiracion confirmada, timeout, respuesta ambigua | Solo senales definitivas son terminales; timeout y ambiguedad siguen "En proceso" |
| Ventana y conciliacion | Ventana agotada con estado desconocido; confirmacion tardia posterior | Bloqueo de reintentos, derivacion a conciliacion, no mostrado como fallido; resolucion auditada |
| Notificaciones | Repetida; contradictoria con terminal vigente; no verificable; llegada antes de la respuesta sincrona | Un solo efecto; contradiccion sin cambio visible y auditada; invalida descartada |
| Idempotencia y concurrencia | Misma solicitud repetida; misma clave con otro contenido; dos confirmaciones concurrentes del mismo checkout; respuesta perdida tras envio | Un unico cargo; rechazo en contenido distinto; un solo intento activo |
| Reintento | Tras fallo y expiracion sin cargo; durante "En proceso"; durante conciliacion | Nuevo intento solo con no-cargo confirmado, nueva autorizacion y nueva clave |
| Exito, historial, evento | Exito unico; confirmacion repetida; fallo tras commit antes de publicar; reentrega | Una entrada de historial, una referencia, un evento; sin perdida ante caidas |
| Privacidad | Inspeccion de historial, evento, logs y respuestas | Destino enmascarado; sin referencia del proveedor ni datos completos fuera del ambito permitido |
| MAU | Varias exitosas de una wallet; solo activacion; prueba; reversion total y parcial antes del cierre; cambio de mes en Lima | Conteo distinto correcto con las exclusiones definidas |
| Abandono | Exito a 23h59 y a mas de 24h; pendiente; varios intentos por checkout; cero iniciados | Tasa por `checkout_id` unico; pendientes aparte; sin division invalida |

Las pruebas combinan reglas unitarias, integracion contra la persistencia real con inyeccion de fallos y simulacion de proveedor. La atomicidad, la unicidad y la idempotencia deben verificarse contra el almacenamiento seleccionado; un mock no las demuestra. Antes de habilitar una ruta, sus senales definitivas se validan en el entorno de pruebas del proveedor.

## Risks / Trade-offs

- **Documentacion del proveedor incompleta o ambigua:** impide clasificar senales definitivas. Mitigacion: la ruta no se habilita hasta completarla; no se infieren estados.
- **Proveedor sin idempotencia propia:** riesgo de doble cargo en el reenvio. Mitigacion: consulta previa al reenvio y no habilitar rutas sin garantia equivalente.
- **Intentos prolongados en "En proceso" y conciliacion manual:** mayor abandono aparente y carga operativa. Trade-off aceptado: preferible a presumir fallo; los pendientes se reportan aparte.
- **Contradicciones tardias:** pueden exigir corregir un estado ya mostrado. Mitigacion: correccion solo con confirmacion autoritativa y auditoria.
- **Entrega al menos una vez de eventos:** duplicados de red. Mitigacion: identidad unica por intento y deduplicacion en la proyeccion.
- **Dependencia de datos de prueba y reversion para MAU:** si faltan, el reporte es no disponible, no una estimacion.
- **Limites dependientes de configuracion:** un error de configuracion puede abrir o cerrar una ruta. Mitigacion: validacion al cargar, version registrada por intento y habilitacion explicita.
- **Un proceso unico acopla modulos:** se acepta por el alcance del MVP; los limites logicos permiten separarlos despues.

## Rollback

- Cada ruta tiene una bandera de habilitacion independiente; apagarla impide nuevos intentos sin afectar los existentes.
- Los intentos en `en_proceso` o `en_conciliacion` siguen resolviendose aunque la ruta se deshabilite: consulta, notificaciones y conciliacion no dependen de la bandera.
- No se revierte ni se borra un resultado terminal, historial, evento ni auditoria; las correcciones usan el flujo autoritativo y conciliado.
- Las proyecciones de MAU y abandono se reconstruyen desde los intentos confirmados, por lo que revertir una version de calculo no pierde datos.
- Los cambios de esquema o configuracion se aplican de forma aditiva y compatible hacia atras mientras existan intentos no terminales.
- Antes de la habilitacion real (paso 8) no hay estado productivo que revertir; basta con retirar los cambios.
