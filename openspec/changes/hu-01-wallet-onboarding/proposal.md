# Proposal

## Why

El [Issue #1: HU-01 - Registro y activacion del MVP de wallet](https://github.com/user-bcp-student05/wallet-group-six/issues/1), consultado mediante github-official el 2026-10-23, compromete un onboarding guiado para Peru que reduzca el abandono sin activar wallets antes de cumplir KYC. Formalizar ahora su contrato permite verificar la meta de mediana de 8 a 3 minutos durante el trimestre posterior al lanzamiento y medir adquisicion sin confundir activacion con actividad.

## What Changes

### Alcance

- Especificar secuencia y paso actual, bloqueo por datos faltantes o invalidos, correcciones permitidas y revision KYC pendiente.
- Condicionar la activacion y el acceso a pagos de bajo monto a completar los pasos obligatorios y aprobar las verificaciones de la politica vigente.
- Definir inicio, reanudacion, eventos sin datos sensibles y duracion hasta `wallet_active` persistido.
- Especificar reportes de inicios, activaciones, mediana, MAU y CAC con sus cohortes, exclusiones y ventanas.
- Exigir evidencia verificable con datos de prueba y version de la politica KYC aplicada.

### Comportamiento que se preserva

El baseline es [HU-01-Onboarding.md](../../../issues/HU-01-Onboarding.md). El cuerpo del Issue coincide con el baseline y no tiene comentarios al consultarlo: no hay cambios de contrato ni excepciones que justificar.

Se preservan la politica KYC aprobada por Cumplimiento como condicion obligatoria de lanzamiento; el primer inicio inmutable al reanudar; el bloqueo de activacion ante pendientes o invalidez; un evento por ocurrencia con timestamp UTC e identificador seudonimo; y las definiciones completas de duracion, MAU y CAC. El onboarding no comunica limites numericos por proveedor: la validacion y presentacion del limite efectivo corresponden al pago antes de confirmar.

### Fuera de alcance

- Definir o inventar documentos, verificaciones o excepciones KYC; sustituir la aprobacion de Cumplimiento.
- Implementar pagos, integraciones, rieles, limites por proveedor, checkout o funcionalidades de HU-02.
- Ampliar el MVP a otros paises o agregar programas de crecimiento.
- Elegir arquitectura, APIs, almacenamiento, proveedores, librerias o instrumentacion tecnica.
- Crear diseno tecnico, `tasks.md` o modificar codigo funcional en esta etapa.

## Capabilities

### New Capabilities

- `wallet-onboarding`: recorrido guiado, politica KYC, correccion, reanudacion y activacion condicionada con evidencia de cumplimiento.
- `onboarding-measurement`: eventos, duracion y reportes de onboarding, actividad mensual y adquisicion atribuible.

### Modified Capabilities

Ninguna. No existen specs previas; estas capacidades incorporan el contrato comprometido, no lo reemplazan.

## Impact

El contrato afecta la experiencia de registro, el control de activacion, la analitica y los reportes de Operaciones y Cumplimiento. La politica KYC aprobada es un prerrequisito de lanzamiento externo a esta propuesta. Esta entrega solo agrega documentacion del cambio bajo OpenSpec; no altera APIs, dependencias ni codigo. Las metas de negocio se monitorean, no se presentan como resultados ya alcanzados.