# Wallet para pagos cotidianos en Perú

MVP de wallet enfocado en hacer más simples y confiables los pagos de bajo monto en Perú. La propuesta prioriza un onboarding rápido, un checkout con menos fricción y confirmaciones claras para cada pago.

## Problema

Las personas que realizan transferencias pequeñas y frecuentes pueden abandonar un checkout lento o confuso. Los pequeños comercios que cobran con QR necesitan saber con certeza si recibieron el pago. El MVP busca resolver estos momentos cotidianos sin asumir un único riel de procesamiento.

## Usuarios

- **Personas pagadoras:** usuarios de smartphone que necesitan transferir montos bajos de forma rápida y saber si el pago se realizó.
- **Pequeños comercios:** negocios que cobran con QR y necesitan aceptación predecible y confirmación confiable.
- **Operaciones y cumplimiento:** equipos que necesitan monitorear estados y conciliar las transacciones del MVP.

## Alcance del MVP

- Onboarding de wallet para el segmento inicial en Perú.
- Transferencias de bajo monto y cobros de comercios con QR.
- Visibilidad del estado y confirmación del pago para pagadores y comercios.
- Instrumentación del checkout, la finalización del onboarding y la actividad de usuarios.

El lanzamiento considera un ecosistema multirriel, integraciones habilitadas mediante partners y límites que pueden variar por proveedor. La experiencia debe comunicar con claridad los estados de pago y evitar duplicidades ante reintentos.

## Metas de negocio

| Indicador | Meta |
| --- | --- |
| Lanzamiento del MVP | Dentro de un trimestre |
| Abandono de checkout | Reducir de 30% a 8% |
| Finalización de onboarding | Reducir de 8 a 3 minutos |
| Usuarios activos mensuales (MAU) | Mantener entre 1,000 y 2,000 en los meses 1-3 posteriores al lanzamiento |
| Costo de adquisición (CAC) | PEN 10 por nuevo usuario en los meses 1-3 posteriores al lanzamiento |

## Fuera de alcance

- Despliegue multipaís amplio.
- Programas avanzados de lealtad o crecimiento no esenciales.
- Paridad completa con todos los bancos y proveedores desde el primer día.
- APIs de pago abiertas y autoservicio para terceros.

## Documentación

- [Definición de la épica](docs/epic.md)
- [HU-01: Registro y activación](issues/HU-01-Onboarding.md)
- [HU-02: Confirmación de pagos de bajo monto](issues/HU-02-Pago-contacto.md)