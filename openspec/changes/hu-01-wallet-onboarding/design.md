# Design

## Context

La motivacion y el alcance aprobados estan en [proposal.md](proposal.md). Los contratos son [wallet-onboarding](specs/wallet-onboarding/spec.md) y [onboarding-measurement](specs/onboarding-measurement/spec.md), derivados del [Issue #1](https://github.com/user-bcp-student05/wallet-group-six/issues/1) y del baseline [HU-01](../../../issues/HU-01-Onboarding.md). Este diseno desarrolla el siguiente artefacto autorizado; no modifica esos documentos ni sus requisitos.

Estado observado en `wallet-group-six` al 2026-10-23:

- OpenSpec usa `spec-driven`; la propuesta y las dos specs existen y no hay `tasks.md`.
- `front-end/`, `services/api/` y `services/back-end/` contienen solamente un `readme.txt` vacio cada uno. No hay implementacion, manifiestos, almacenamiento ni pruebas ejecutables en esos directorios.
- Los nombres de carpetas no demuestran que existan servicios desplegados. Los componentes siguientes son responsabilidades propuestas, no sistemas ya disponibles.
- La politica KYC aprobada y configurada es un prerrequisito externo de lanzamiento. Este repositorio no proporciona su contenido; no se suplen documentos ni verificaciones por suposicion.

No hay migracion de codigo existente que ejecutar ahora. El diseno es necesario por la consistencia entre activacion, verificacion KYC y eventos, y por el tratamiento de informacion sensible.

## Goals / Non-Goals

**Goals:**

- Establecer una autoridad unica para validar y persistir el onboarding, resistente a reintentos y concurrencia.
- Separar datos de cumplimiento, estado del recorrido y proyecciones analiticas sin exigir servicios nuevos.
- Dar un significado operativo preciso al inicio, la activacion persistida y la identidad de cada ocurrencia.
- Mantener reportes reproducibles y un despliegue reversible sin desactivar wallets validamente activadas.

**Non-Goals:**

- Elegir framework, motor de base de datos, nube o proveedor KYC; tampoco definir URLs o contratos completos de una API aun inexistente.
- Crear infraestructura distribuida, un bus de eventos o event sourcing para toda la aplicacion.
- Redisenar pagos, identidad global, checkout o contabilidad. Para MAU solo se consume informacion de actividad confirmada; no se implementa su origen.
- Cambiar la politica KYC, las formulas aprobadas o las metas para facilitar su cumplimiento.
- Implementar codigo, crear pruebas ejecutables, desplegar o producir `tasks.md` en esta entrega.

## Contratos protegidos

| Contrato | Proteccion tecnica | Referencia |
| --- | --- | --- |
| Politica aprobada y pasos visibles | Evaluacion del lado servidor; respuesta con secuencia, paso actual, estado y acciones permitidas por la politica | wallet-onboarding: Politica KYC; Secuencia y paso actual |
| Datos invalidos, faltantes o revision sin aprobar no activan | Verificacion de precondiciones dentro de la transicion persistente, no solo en pantalla | wallet-onboarding: Bloqueo; Revision; Activacion |
| Solo `wallet_active` persistido finaliza | Confirmacion al cliente y evento de activacion posteriores al commit | wallet-onboarding: Activacion; onboarding-measurement: Eventos |
| Primer inicio y primer contacto inmutables | Escritura unica al primer "Comenzar"; reanudacion lee el estado sin volver a inicializarlo | Ambas specs: Reanudacion; Atribucion |
| Un evento por ocurrencia, UTC y seudonimo | Identidad estable de ocurrencia, deduplicacion y lista permitida de campos | onboarding-measurement: Eventos; Privacidad |
| Duracion incluye pausas y revisiones | Diferencia entre inicio original y activacion persistida; pendientes e incompletos separados | onboarding-measurement: Duracion; Mediana |
| MAU no equivale a activaciones | Proyeccion de wallets distintas desde actividad elegible, con exclusiones y reversiones | onboarding-measurement: MAU; Exclusiones |
| CAC de primer contacto en 30 dias | Atribucion original, costos elegibles y activados atribuibles; no atribuidos separados | onboarding-measurement: Atribucion; Formula; Usuarios y gastos |
| Sin limites numericos por proveedor en onboarding | No incluirlos en el contrato de presentacion del recorrido | wallet-onboarding: Limites |

Las metas de mediana, MAU y CAC se comparan con mediciones reales; nunca alteran filtros ni autorizan saltarse KYC. La version de politica aplicada debe quedar identificable en la evidencia de cumplimiento, sin documentos reales.

## Componentes afectados

| Ubicacion propuesta | Responsabilidad en este cambio | Limite |
| --- | --- | --- |
| `front-end/` | Recorrido, estado visible, correcciones, reanudacion y acceso a pagos tras confirmacion de activacion | No decide aprobaciones KYC ni activa localmente |
| `services/api/` | Frontera autenticada de consulta y comandos; valida pertenencia del onboarding al usuario y devuelve estado confirmado y acciones permitidas | No duplica reglas KYC ni expone documentos a analitica |
| `services/back-end/` | Modulos logicos de onboarding, evaluacion de politica, persistencia y publicacion de eventos | Una misma autoridad de escritura; no exige despliegues separados |
| `services/back-end/` | Proyecciones y calculos de onboarding, MAU y CAC; lectura de actividad y gastos atribuibles | No procesa pagos ni compra medios; consume datos autoritativos |

Estos limites pueden convivir en un solo proceso de servidor. Los datos de actividad, gastos y aprobaciones KYC se integraran mediante fronteras de entrada validables; no se inventan proveedores ni se implementan esas integraciones aqui.

## Decisions

### 1. Estado autoritativo del servidor y transiciones condicionadas

Modelar el recorrido con pasos requeridos y completados, resultado de validaciones, situacion KYC, acciones permitidas y activacion persistida. Mantener identificador de onboarding, version de estado, inicio original, primer contacto, version de politica evaluada y timestamp de activacion. Pendiente o corregible describen el recorrido: no son equivalentes a `wallet_active`.

Cada comando verifica identidad y propiedad. La activacion vuelve a comprobar pasos, datos y aprobaciones contra la politica vigente aprobada, y persiste mediante control de version o bloqueo equivalente. Si el estado o la version de politica cambian durante la evaluacion, se vuelve a evaluar; no se activa con una lectura obsoleta. Una aprobacion KYC solo procede de la frontera confiable de cumplimiento, nunca de un campo enviado por la pantalla. Si falta la politica o no puede verificarse, se bloquea la activacion sin fabricar reglas alternativas.

El primer "Comenzar" fija una unica vez el inicio y la atribucion de primer contacto. Consultar o reanudar no crea otro inicio. Dos peticiones concurrentes de comienzo o activacion deben converger al mismo resultado confirmado. La UI no aplica una activacion optimista y el servidor no confia en que el punto de entrada a pagos este oculto como control de autorizacion.

**Alternativas:** validar solo en cliente facilita la experiencia pero permite eludir KYC; duplicar reglas en cliente y servidor genera divergencias. Se elige el servidor, permitiendo ayudas de captura en cliente sin autoridad de aprobacion. Una maquina de estados externa se descarta para este alcance: las transiciones y precondiciones explicitas en un modulo son suficientes.

### 2. Consistencia transaccional y entrega idempotente de eventos

Requerir almacenamiento con transacciones y unicidad, sin seleccionar producto. El cambio de estado y el registro durable del evento pendiente se escriben en la misma transaccion local. Para la activacion, el estado `wallet_active`, su timestamp y la ocurrencia de `onboarding_activated` quedan comprometidos juntos; el timestamp representa el instante de esa escritura confirmada, no la posterior entrega analitica. No se confirma exito si falla el commit.

El mecanismo de publicacion lee esos registros pendientes y reintenta hasta recibir confirmacion. Puede ejecutarse en el mismo servidor, sin broker adicional. Se acepta entrega al menos una vez; la vista analitica deduplica por identidad de ocurrencia. No se promete una unica entrega de red: se garantiza una sola ocurrencia registrada y contabilizada.

Cada ocurrencia conserva su identidad entre reintentos. El primer inicio y la activacion tienen identidad unica por onboarding; los pasos y cierres distinguen ocurrencias legitimas diferentes de un reenvio de la misma. El cliente conserva una clave de reintento para sus comandos hasta conocer su resultado; el servidor comprueba su correspondencia con la operacion y propietario. Un cierre no activa ni borra el inicio. No se infiere un cierre simplemente por reanudar o por ausencia de actividad.

**Alternativas:** enviar eventos antes de persistir produciria activaciones inexistentes; enviarlos despues sin registro durable perderia eventos ante caidas. Una transaccion distribuida con analitica aumenta el acoplamiento sin necesidad. Se elige registro pendiente transaccional y consumidor idempotente por su recuperabilidad.

### 3. Separacion de datos sensibles y analitica por lista permitida

El contrato analitico permite tipo de evento, identidad de ocurrencia, timestamp UTC e identificador seudonimo, junto con referencias no sensibles de paso y atribucion necesarias para las metricas. La atribucion usa referencias controladas, no URLs libres ni texto que pueda contener datos personales. No se copian cuerpos de peticiones, documentos, resultados documentales detallados ni mensajes libres de errores a eventos o logs.

La evidencia KYC conserva referencia a la version de politica y resultados necesarios en el ambito de cumplimiento con acceso restringido. La correspondencia con el seudonimo no se exporta a la analitica. Los fixtures, capturas y reportes de aceptacion utilizan datos sinteticos.

**Alternativas:** exportar el objeto completo de onboarding reduce mapeos iniciales pero expone datos y hace fragil la privacidad. Se elige una proyeccion explicita validada; la seudonimizacion no se considera anonimizacion ni sustituye controles de acceso.

### 4. Proyecciones reproducibles separadas de la autorizacion

Los eventos persistidos alimentan la proyeccion de onboarding. MAU consume transacciones elegibles confirmadas y su estado de anulacion o reversion al cierre; CAC consume gastos clasificados y activaciones con atribucion original. Cada resultado identifica periodo, fecha de corte y version de calculo. La falta de una fuente no se reemplaza por ceros ni por activaciones: se senala como dato no disponible.

Se conservan timestamps en UTC. Para el mes calendario de la cohorte Peru se usa `America/Lima`, convirtiendo los limites del periodo a UTC para filtrar; esto no cambia la representacion UTC de eventos. La ventana de atribucion se evalua como tiempo transcurrido desde el inicio hasta la activacion, hasta 30 dias inclusive, sin redondeo por fecha. Se incluyen pruebas de ambos limites temporales.

La duracion es activacion menos primer inicio y la mediana usa solo finalizados. MAU cuenta wallets distintas con al menos una actividad que siga siendo elegible segun el contrato; una reversion total no elimina otra transaccion elegible de esa wallet. CAC usa gastos del mes reportado y nuevos activados atribuibles de ese mes, incluso si el primer contacto fue el mes anterior, siempre dentro de ventana. Los no atribuidos permanecen separados. Importes se calculan con precision decimal, sin redondear antes del cociente; con denominador cero se presenta CAC no calculable y sus componentes, no PEN 0.

**Alternativas:** calcular MAU desde activaciones o CAC desde todos los registros es mas simple pero contradice las specs. Computar todo desde pantallas hace imposible reconciliarlo. Se eligen proyecciones reconstruibles con corte explicito; sus retrasos no bloquean una activacion validamente persistida.

## Pruebas de caracterizacion y aceptacion

No hay codigo ejecutable que caracterizar en el estado observado. Antes de sustituir cualquier futura ruta critica existente, se deberan capturar sus entradas, salidas, efectos persistidos y eventos con datos sinteticos, ejecutando la version anterior y la candidata sobre los mismos casos. Esas observaciones son caracterizacion, no prueba de correccion: una divergencia frente a las specs se trata como defecto explicito, no como conducta que deba perpetuarse.

Si la implementacion comienza desde estos directorios vacios, la siguiente matriz sera primero una suite de contrato basada en las specs; se convertira en baseline de regresion antes de cambios posteriores. No se declara ninguna prueba ejecutada en esta entrega documental.

| Superficie critica | Casos que deben quedar caracterizados antes de cambiarla | Evidencia y resultado esperado |
| --- | --- | --- |
| Politica y validaciones | Politica ausente; dato faltante o invalido; correccion permitida; revision pendiente, no aprobada y aprobada; cambio concurrente de version | Secuencia y acciones visibles; no activacion sin precondiciones; version de politica verificable |
| Activacion persistente | Camino completo; paso pendiente; fallo de commit; respuesta perdida tras commit; dos activaciones concurrentes | Un estado activo confirmado y una ocurrencia de activacion, o ninguno si no hubo commit; acceso solo tras confirmacion |
| Inicio y reanudacion | Primer "Comenzar" duplicado o concurrente; pausa, cierre y reanudacion; primer contacto A seguido de B | Inicio original y A inmutables; mismo estado retomado; ninguna activacion por cerrar |
| Eventos | Reenvio de los cuatro tipos; dos pasos distintos; dos cierres distintos; caida entre commit y publicacion; reentrega despues de confirmacion perdida | Una ocurrencia contada por accion real, sin perder eventos comprometidos ni fusionar ocurrencias diferentes |
| Privacidad y acceso | Usuario distinto intenta consultar o avanzar; carga con documento o dato sensible; inspeccion de logs y eventos | Acceso rechazado; los campos sensibles no llegan a analitica ni logs; evidencia sintetica |
| Duracion y mediana | Inicio 08:00, pausa 08:02, retorno 08:10, activacion 08:12; espera KYC; finalizados de 2, 4 y 9 minutos con pendiente e incompleto | Duracion 12 minutos; espera incluida; mediana 4; pendientes e incompletos separados |
| MAU | Solo activacion; multiples pagos de una wallet; prueba, anulacion, reversion total antes del cierre, parcial y otra transaccion aun elegible | Conteo distinto correcto; excluir solo actividad no elegible; activacion sola no cuenta |
| CAC | PEN 100 + 20 + 10 elegibles y 13 activados; PEN 90 excluidos; PEN 30 y dos activados sin atribucion; activacion a 20 y 31 dias | CAC PEN 10 en el fixture aprobado; no atribuidos separados; primer contacto conservado y ventana aplicada |
| Fronteras y reportes | Cambio de mes en Lima frente a UTC; instante a 30 dias y justo despues; atribucion entre meses; cero activados; fuente incompleta; reconstruccion con duplicados | Periodos reproducibles; limite de ventana preciso; CAC no calculable cuando corresponde; ausencia de datos visible; mismo resultado al reconstruir |

Las pruebas futuras combinan reglas unitarias, integracion con la persistencia real e inyeccion de fallos, y recorridos de UI de los escenarios aprobados. Antes de habilitar activaciones, es obligatorio verificar atomicidad e idempotencia contra el almacenamiento seleccionado; un mock no demuestra esas garantias.

## Risks / Trade-offs

- [Politica KYC ausente o no vigente] -> Impedir lanzamiento o nuevas activaciones segun corresponda, mantener el estado y requerir la politica aprobada; no introducir un modo permisivo.
- [Concurrencia o resultado KYC obsoleto] -> Reevaluar precondiciones con control de version y registrar la politica aplicada al confirmar.
- [Desfase entre commit y analitica] -> Registro pendiente durable, reintentos y deduplicacion; supervisar pendientes y reconciliar activaciones contra eventos. El reporte puede retrasarse sin falsear el estado de wallet.
- [Cierre con terminacion abrupta o sin red] -> Registrar y reintentar las ocurrencias de cierre observadas, sin duplicarlas; no usar la ausencia de cierre para inferir activacion. La captura de cierres debe verificarse en los clientes soportados y no prometer entrega inmediata si el proceso termina sin ejecutar codigo.
- [Seudonimos correlacionables o atribucion con datos personales] -> Lista permitida, referencias controladas, acceso restringido y pruebas de ausencia de datos sensibles.
- [Fuentes de pagos o gastos aun inexistentes] -> Probar con fixtures; impedir presentar esos resultados como mediciones reales hasta disponer de entradas verificables. No ampliar este cambio para implementar pagos o adquisicion.
- [Zona de reporte y ventana temporal] -> Documentar las convenciones elegidas, compartirlas entre calculos y reportes y probar los bordes; no usar el huso del dispositivo o del servidor implicitamente.
- [Sobrecosto de transacciones y publicacion pendiente] -> Mantenerlo local al servidor y limitado al onboarding; se acepta para evitar activaciones sin evidencia o eventos ficticios.

## Migration Plan

Estrategia incremental para una implementacion posterior, no operaciones autorizadas en esta entrega:

1. Establecer fixtures y la suite de contrato anterior; si ya existe codigo al iniciar la implementacion, caracterizarlo antes de tocar sus rutas criticas. Confirmar politica aprobada y fuentes disponibles, sin datos reales en la evidencia.
2. Incorporar el estado autoritativo y el registro pendiente mediante cambios aditivos de datos. Probar comienzo, reanudacion, validacion y activacion sin exponer nuevas activaciones a usuarios hasta comprobar transacciones y KYC.
3. Incorporar publicacion y proyecciones; comparar los reportes con los resultados sinteticos y reconciliar eventos contra estado persistido. La analitica nueva puede verificarse en paralelo, pero nunca con una segunda autoridad que tambien active wallets.
4. Conectar el recorrido a las respuestas autoritativas y habilitarlo gradualmente para la cohorte MVP solo tras superar las pruebas, el checklist KYC y las comprobaciones de privacidad. No ampliar la cohorte para compensar fallos de metricas.
5. Observar errores de validacion, conflictos de concurrencia, eventos pendientes y diferencias de reconciliacion. Versionar las formulas y conservar el corte del reporte para poder reconstruir y comparar resultados.

### Rollback

Esta entrega solo crea un documento: no requiere rollback de runtime ni de datos.

En un despliegue futuro, permitir detener nuevas entradas o activaciones mediante control de habilitacion sin eliminar el estado en curso. Si existe una version anterior compatible y verificada, se puede restaurar; si no existe, se mantiene el flujo temporalmente no disponible, nunca una alternativa que omita KYC.

No revertir ni borrar wallets ya activadas validamente, el primer inicio, la atribucion o la evidencia de cumplimiento. Conservar registros pendientes y su identidad para continuar la entrega tras recuperar el servicio. Si el defecto esta en un reporte, retirar esa version del calculo y reconstruir desde datos autoritativos, sin alterar el estado de las wallets. Mantener cambios de datos aditivos y posponer eliminaciones de campos hasta cerrar el periodo de compatibilidad; un rollback no debe depender de una migracion destructiva.