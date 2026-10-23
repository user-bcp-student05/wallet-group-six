# HU-01 - Registro y activación del MVP de wallet

## Veredicto
**Definición PO cerrada para el MVP.** La wallet solo se activa al cumplir la política KYC aprobada para Perú. La política es configuración obligatoria de lanzamiento; no se inventan documentos ni validaciones fuera de ella.

## Historia de usuario
Como usuario de smartphone en Perú que realiza transferencias de bajo monto,
quiero completar un onboarding guiado, saber en qué paso estoy y qué debo corregir,
para activar mi wallet y empezar a pagar sin abandonar el proceso.

## Contexto
- Épica: [MVP de wallet en Perú](../epic/peru-wallet-mvp.sp.md).
- La meta de negocio es reducir la mediana de finalización del onboarding de 8 a 3 minutos dentro del trimestre posterior al lanzamiento.
- Los objetivos de adquisición son MAU de 1,000-2,000 y CAC de PEN 10 por nuevo usuario durante los primeros 3 meses.
- Los requisitos KYC/documentales deben provenir de la política vigente aprobada por Cumplimiento para el segmento MVP en Perú.

## Definiciones PO
- El inicio se registra cuando el usuario selecciona “Comenzar” por primera vez. Reanudar el flujo no reinicia el timestamp.
- La finalización se registra cuando el estado `wallet_active` queda persistido. La duración primaria es tiempo transcurrido entre inicio y activación, incluyendo pausas y revisiones; los flujos incompletos y pendientes se informan por separado y no entran en la mediana de finalización.
- La wallet no se activa si hay pasos obligatorios pendientes, información inválida o una revisión KYC sin aprobar. Si la política permite corrección, el flujo indica la acción; si el caso requiere revisión, informa que está pendiente y el siguiente paso permitido.
- Los eventos analíticos son `onboarding_started`, `onboarding_step_completed`, `onboarding_activated` y `onboarding_closed`. Se registran con timestamp UTC y un identificador seudónimo; no se envían documentos ni datos sensibles en la analítica.
- MAU cuenta wallets únicas con al menos una transacción elegible confirmada como exitosa durante el mes calendario. Activar la wallet no cuenta por sí solo como MAU. Se excluyen pagos de prueba, anulados o totalmente revertidos antes del cierre del mes; una reversión parcial no elimina la actividad.
- CAC se calcula mensualmente como gasto directo de adquisición atribuible dividido entre nuevos usuarios con wallet activada atribuibles. Se usa atribución de primer contacto, capturada al inicio del onboarding, con ventana de 30 días para activar. El gasto directo incluye medios pagados, agencias/afiliados y recompensas de referido; excluye costos de producto y gastos generales. Los usuarios y gastos sin atribución se reportan aparte, no se reasignan.
- El onboarding no comunica límites numéricos por proveedor. La pantalla de pago valida y muestra el límite efectivo antes de confirmar una transacción.

## Alcance de integraciones y mocks
- Para esta entrega usamos **mocks propios del equipo** y no los mocks definidos en el documento inicial de la hackathon. Los contratos de nuestros mocks viven en este repositorio (`services/api/`) y su implementación en `services/back-end/`.
- La política KYC aprobada por Cumplimiento sigue siendo condición de lanzamiento. Mientras no esté disponible, las pruebas y demostraciones usan una **política KYC sintética de prueba** (pasos y verificaciones ficticios, sin documentos reales); su resultado no equivale a una aprobación de Cumplimiento.
- Los eventos analíticos, la atribución de primer contacto, el gasto de adquisición y los reportes de onboarding, MAU y CAC se alimentan con datos sintéticos de nuestros mocks. Los valores obtenidos con ellos no constituyen resultados reales ni se comparan como tales con las metas de negocio.
- Los comportamientos que el documento inicial de la hackathon daba por resueltos y que nuestros mocks aún no cubren se simularán con mocks propios, definidos y versionados en el repositorio antes de automatizar sus pruebas.
- Este alcance no cambia los criterios de aceptación ni las definiciones de arriba: solo delimita contra qué se verifican en esta entrega. Pasar a producción exige reemplazar los mocks por las integraciones y la política KYC reales.

## Criterios de aceptación
1. El flujo muestra la secuencia de pasos requeridos y el paso actual en todo momento.
2. Ante información obligatoria faltante o inválida, se bloquea el avance y se indica cómo corregirla, conforme a la política KYC vigente.
3. La wallet pasa a activa solo cuando todos los pasos obligatorios están completos y las verificaciones requeridas por la política KYC están aprobadas; entonces se habilita el punto de entrada a pagos de bajo monto.
4. Cada inicio, paso completado, activación y cierre genera el evento definido una sola vez por ocurrencia, con timestamp UTC y sin documentos ni datos sensibles.
5. La duración se calcula desde el primer `onboarding_started` hasta `wallet_active`; reanudaciones no reinician el inicio. Pendientes e incompletos se reportan por separado.
6. Para la cohorte MVP en Perú, se monitorea la mediana de finalización contra la meta de 8 a 3 minutos durante el trimestre posterior al lanzamiento.
7. El reporte mensual presenta inicios, activaciones, MAU y CAC usando las definiciones y ventanas establecidas en “Definiciones PO”; CAC se compara con la meta de PEN 10 durante los meses 1-3.

## Evidencia esperada
- Resultados de pruebas para flujo completo, datos inválidos, KYC pendiente/aprobado y reanudación del onboarding.
- Checklist de cumplimiento que identifique la versión de la política KYC aplicada, sin adjuntar documentos reales de usuarios.
- Muestra analítica con eventos, timestamps y atribución usando datos de prueba no sensibles.
- Reporte de onboarding, MAU y CAC con las fórmulas y ventanas descritas en esta historia.
