# channel-contact-transfer-v1

Proyecto Quarkus con endpoints mock basados en el contrato `ux-contact-transfer-v1.yaml`.

## Requisitos

- Java 25 (LTS)
- Maven 3.9+

## Endpoints implementados

- `POST /affiliation-wallet`
- `POST /contact-transfers/financial-entities/search`
- `POST /contact-transfers/authorization/verify`
- `POST /contact-transfers/execute`

## Ejecutar en modo desarrollo

```bash
mvn quarkus:dev
```

## Ejecutar pruebas

```bash
mvn test
```

## Casos mock de error

- `affiliation-wallet`:
  - `documentNumber = RkFJTF80MjI=` => `422`
  - `documentNumber = RkFJTF81MDA=` => `500`
- `financial-entities/search`:
  - `phone = RkFJTF81MDA=` => `500`
- `authorization/verify`:
  - `amount = 422.00` => `422`
  - `amount = 500.00` => `500`
- `execute`:
  - `description` contiene `fail422` => `422`
  - `description` contiene `fail500` => `500`
