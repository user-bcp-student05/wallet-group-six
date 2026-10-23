# Tasks

Unidad de trabajo del producto: [GitHub Issue #1 - HU-01](https://github.com/user-bcp-student05/wallet-group-six/issues/1). Este archivo es exclusivamente el checklist de implementacion del cambio `hu-01-wallet-onboarding`; no crea historias nuevas ni amplia el compromiso del Issue.

Referencias aprobadas: [proposal.md](proposal.md), [wallet-onboarding](specs/wallet-onboarding/spec.md), [onboarding-measurement](specs/onboarding-measurement/spec.md) y [design.md](design.md). El baseline protegido es [HU-01-Onboarding.md](../../../issues/HU-01-Onboarding.md).

Ejecutar por dependencia. El grupo 1 precede a cualquier cambio funcional. Hoy no hay implementacion que caracterizar: no inventar resultados existentes. Si aparece codigo antes de ejecutar este checklist, capturar su comportamiento antes de modificarlo; si sigue vacio, establecer primero las pruebas de contrato. Las caracterizaciones existentes deben pasar; las pruebas de funcionalidades ausentes pueden fallar inicialmente por la ausencia esperada y deben pasar con su incremento. No marcar una tarea sin su evidencia de verificacion.

Todo el trabajo queda en `wallet-group-six`, sobre `main`, sin crear ni cambiar de rama. No incluir pagos, HU-02, nuevos rieles, proveedores KYC, programas de crecimiento, infraestructura distribuida ni redisenos globales. La eleccion del soporte minimo para ejecutar las pruebas y el codigo no autoriza scaffolding ajeno al onboarding.

## 1. Caracterizacion y contratos antes del cambio funcional

- [ ] 1.1 Registrar el estado inicial de los directorios tecnicos y preparar el arnes minimo de pruebas del cambio, reutilizando cualquier herramienta que ya exista; verificar un comando reproducible de ejecucion y documentar explicitamente si no hay implementacion que caracterizar, sin agregar codigo funcional.
- [ ] 1.2 Crear casos de contrato para politica ausente, dato faltante o invalido, correccion, KYC pendiente y aprobado, secuencia visible y activacion condicionada; si existe una ruta equivalente, capturar tambien entradas, salidas y efectos persistidos de esa version. Verificar su correspondencia con los escenarios de wallet-onboarding y registrar el resultado inicial sin asumir que un defecto existente es correcto.
- [ ] 1.3 Crear casos previos para primer "Comenzar", reanudacion, primer contacto A seguido de B, pasos distintos, cierres distintos y reenvios de los cuatro eventos; verificar que los resultados esperados conservan inicio y atribucion y distinguen ocurrencias nuevas de duplicados.
- [ ] 1.4 Preparar pruebas de integracion para fallo de commit, respuesta perdida, inicios y activaciones concurrentes, cambio de politica durante evaluacion y caida antes de publicar; verificar que las aserciones exigen estado y evento atomicos y ausencia de activacion obsoleta, sin considerar un mock como evidencia de transacciones reales.
- [ ] 1.5 Crear fixtures sinteticos y expectativas para duracion de 12 minutos, mediana de 4, MAU con exclusiones y reversiones, CAC PEN 10 y no atribuidos separados; verificar que reproducen los ejemplos de onboarding-measurement sin documentos ni datos sensibles.
- [ ] 1.6 Agregar casos de frontera para mes calendario en America/Lima, timestamps UTC, activacion a 30 dias inclusive y justo despues, atribucion entre meses, CAC sin denominador y fuente incompleta; verificar las expectativas contra la decision 4 del diseno antes de implementar los calculos.

## 2. Base persistente y politica del onboarding

- [ ] 2.1 Incorporar de forma aditiva el estado persistente del onboarding, inicio original, primer contacto, versiones de estado y politica, timestamp de activacion y registro pendiente de eventos con identidad unica; verificar con el almacenamiento seleccionado que una transaccion fallida no deja escrituras parciales y que las restricciones de unicidad se cumplen.
- [ ] 2.2 Incorporar lectura y validacion de la configuracion KYC versionada sin inventar documentos ni verificaciones; verificar con una politica sintetica de prueba los casos disponible, ausente y no verificable, manteniendo bloqueada la activacion en los dos ultimos.
- [ ] 2.3 Registrar como prerrequisito externo la referencia a la politica vigente aprobada por Cumplimiento y su estado de disponibilidad; verificar un checklist de lanzamiento que no acepte un fixture como aprobacion real y que impida habilitar activaciones mientras el prerrequisito falte.
- [ ] 2.4 Documentar en la documentacion tecnica local del onboarding el modelo persistente, el comando de pruebas y la configuracion requerida, sin secretos; verificar que las instrucciones reproducen la preparacion del almacenamiento de prueba y no requieren servicios distribuidos nuevos.

## 3. Transiciones autoritativas de registro y activacion

- [ ] 3.1 Implementar el primer inicio con atribucion de primer contacto y ocurrencia de onboarding_started en una transaccion, y la consulta de reanudacion sin reinicializacion; verificar los casos de 1.3 y dos inicios concurrentes con el mismo timestamp y atribucion conservados.
- [ ] 3.2 Implementar avance de pasos con validacion segun politica, correcciones permitidas y ocurrencia de onboarding_step_completed al completar cada paso; verificar con los casos de 1.2 que datos faltantes o invalidos bloquean y que repetir una peticion no completa ni cuenta el mismo paso dos veces por la misma ocurrencia.
- [ ] 3.3 Incorporar el resultado de revision desde una frontera confiable de cumplimiento, su estado pendiente y las acciones permitidas, con control de version; verificar que una aprobacion enviada por el cliente se rechaza y que un cambio concurrente de politica o estado obliga a reevaluar.
- [ ] 3.4 Implementar activacion condicionada persistiendo wallet_active, timestamp y ocurrencia de onboarding_activated atomicamente; verificar sobre almacenamiento real los casos de 1.4, incluyendo aprobacion KYC con paso pendiente, fallo de commit, peticiones concurrentes y respuesta perdida tras commit.
- [ ] 3.5 Exponer consulta y comandos con verificacion de identidad y propiedad del onboarding, devolviendo solo estado confirmado, secuencia y acciones permitidas; verificar rechazo de acceso de otro usuario y documentar entradas, respuestas y reintentos en la documentacion local de la API, sin construir un sistema global de identidad.

## 4. Eventos, privacidad y recuperacion de entrega

- [ ] 4.1 Implementar el registro idempotente de onboarding_closed para cierres observados, sin activacion ni borrado de inicio; verificar cierre sin activar, reenvio del mismo cierre y dos cierres legitimos distintos con los casos de 1.3.
- [ ] 4.2 Implementar publicacion reintentable de los eventos pendientes tras commit; verificar la recuperacion ante caida entre commit y publicacion y ante confirmacion de entrega perdida, sin perder eventos comprometidos ni publicar transiciones revertidas.
- [ ] 4.3 Implementar deduplicacion analitica por identidad estable de ocurrencia para los cuatro eventos; verificar que una reentrega cuenta una vez y que pasos y cierres distintos no se fusionan, aun al reconstruir la proyeccion.
- [ ] 4.4 Aplicar lista permitida de campos con timestamp UTC, seudonimo y referencias controladas de paso y atribucion; verificar eventos y logs con cargas sinteticas sensibles para demostrar que no exportan documentos, cuerpos de peticion, URLs libres ni datos personales.
- [ ] 4.5 Documentar la identidad de ocurrencias, los reintentos, las restricciones de privacidad y la reconciliacion de pendientes; verificar que el procedimiento local permite detectar y recuperar una entrega pendiente sin registrar documentos reales.

## 5. Recorrido guiado conectado al estado confirmado

- [ ] 5.1 Implementar visualizacion de secuencia, paso actual, datos faltantes o invalidos y acciones de correccion permitidas desde el servidor; verificar los recorridos de 1.2, incluida la visibilidad de pasos durante correccion y revision, sin limites numericos por proveedor.
- [ ] 5.2 Implementar presentacion de KYC pendiente o sin aprobar sin activar ni habilitar el punto de entrada a pagos; verificar que una captura de datos completa no se presenta como aprobacion y que solo se ofrecen acciones autorizadas por la politica.
- [ ] 5.3 Conectar comienzo, reanudacion y cierres observados con claves de reintento conservadas hasta conocer el resultado; verificar pausa, reconexion y respuesta perdida sin reiniciar inicio o atribucion, duplicar eventos ni inferir cierres por inactividad.
- [ ] 5.4 Habilitar el punto de entrada a pagos solo tras observar wallet_active confirmado; verificar activacion persistida, persistencia fallida y respuesta aun pendiente sin activacion optimista, sin implementar la transaccion de pago.
- [ ] 5.5 Documentar en la documentacion local del front-end los comandos para ejecutar los recorridos sinteticos y la limitacion de captura ante terminacion abrupta; verificar esos comandos en los clientes soportados sin afirmar entrega inmediata cuando el proceso no ejecuta codigo.

## 6. Medicion reproducible de onboarding, MAU y CAC

- [ ] 6.1 Implementar la proyeccion de inicios y activaciones y la duracion desde primer inicio hasta activacion persistida; verificar el fixture de 12 minutos con pausas y los casos con espera KYC, sin usar el timestamp de entrega analitica como finalizacion.
- [ ] 6.2 Implementar mediana solo de finalizados y reporte separado de pendientes e incompletos; verificar mediana de 4 minutos para 2, 4 y 9, sin asignar finalizacion a los otros flujos.
- [ ] 6.3 Incorporar una entrada validable de actividad confirmada para el calculo de MAU, sin implementar pagos; verificar rechazo o senalizacion de fuentes incompletas y que los fixtures de actividad no se presentan como datos reales.
- [ ] 6.4 Implementar MAU como wallets distintas con actividad elegible en el mes, excluyendo pruebas, anulaciones y reversiones totales antes del cierre; verificar multiples pagos, solo activacion, reversion parcial y otra transaccion aun elegible, con los fixtures de 1.5 y fronteras de 1.6.
- [ ] 6.5 Incorporar entradas validables de gastos clasificados y atribucion para CAC, sin implementar compra de medios ni integraciones de proveedores; verificar separacion de datos sin atribucion y exclusiones de producto y gastos generales.
- [ ] 6.6 Implementar CAC decimal con gastos del mes y nuevos activados atribuibles del mes, primer contacto inmutable y ventana de 30 dias; verificar CAC PEN 10, contactos A/B, activacion a 20 y 31 dias, limite exacto, cruce de mes y denominador cero sin presentar PEN 0.
- [ ] 6.7 Generar el reporte mensual de la cohorte MVP Peru con inicios, activaciones, MAU, CAC, mediana, metas y datos no disponibles; verificar comparaciones de meses 1-3 y del trimestre, con periodo, corte y version de calculo, sin alterar formulas para aparentar alcanzar objetivos.
- [ ] 6.8 Documentar fuentes, formulas, zona America/Lima, ventana, exclusiones y comando de reconstruccion de reportes; verificar que el mismo fixture y corte reproducen los mismos resultados incluso con eventos duplicados y publicacion demorada.

## 7. Habilitacion incremental y rollback

- [ ] 7.1 Incorporar un control acotado para detener nuevas entradas o activaciones sin eliminar el estado en curso; verificar en entorno de prueba que mantiene wallets ya activadas, timestamps, atribucion y evidencia, y que no deriva a un flujo que omita KYC.
- [ ] 7.2 Preparar comprobaciones de errores, conflictos de version, pendientes de publicacion y reconciliacion de activaciones contra eventos; verificar con fallos inducidos que cada discrepancia resulta detectable sin logs sensibles ni infraestructura de observabilidad ajena al cambio.
- [ ] 7.3 Documentar y ensayar recuperacion de publicacion y reconstruccion de un reporte defectuoso en entorno de prueba; verificar conservacion de identidades de ocurrencia, datos y wallets activadas, sin migraciones destructivas ni exigir una version previa inexistente.

## 8. Validacion integral y evidencia del Issue

- [ ] 8.1 Ejecutar los recorridos completos de registro, datos invalidos, KYC pendiente y aprobado, cierre y reanudacion sobre UI, API y persistencia; verificar conjuntamente estado visible, controles de acceso, activacion confirmada y eventos sin duplicados contra las dos specs.
- [ ] 8.2 Ejecutar la reconciliacion integral entre estado persistido, eventos y reporte con concurrencia, caidas y reentregas; verificar los resultados de la matriz de design.md con almacenamiento real y adjuntar resultados sinteticos reproducibles.
- [ ] 8.3 Reunir el checklist de version de politica KYC, resultados de pruebas, muestra analitica no sensible y reporte de onboarding, MAU y CAC con formulas y ventanas; verificar que la evidencia cubre los siete criterios de aceptacion del Issue #1 sin afirmar que fixtures acreditan metas de negocio reales.
- [ ] 8.4 Verificar la habilitacion gradual en entorno de prueba y documentar las condiciones de lanzamiento: suite aprobada, politica real aprobada y configurada, fuentes verificables y rollback ensayado; mantener bloqueado cualquier lanzamiento real si falta una condicion, sin desplegar a produccion como parte de este checklist.
- [ ] 8.5 Ejecutar los comandos documentados de pruebas y validacion del cambio, incluido `openspec validate hu-01-wallet-onboarding --strict`; verificar que no quedan escenarios aprobados sin evidencia ni cambios funcionales fuera del Issue antes de someter el incremento a revision.