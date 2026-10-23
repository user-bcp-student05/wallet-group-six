---
name: software-testing-automation
description: Procedimiento para automatizar pruebas funcionales de APIs con JUnit 5, Quarkus Test y REST Assured a partir del archivo test-cases.md generado por el skill software-testing-design, validando los casos contra el contrato OpenAPI y las specs aprobadas, con trazabilidad caso-prueba, datos sintéticos y reportes Surefire.
---
# Automatización de pruebas funcionales de API (REST Assured + Quarkus)

Objetivo: automatizar los casos de `test-cases.md` que se validan en backend y verificar el comportamiento observable de la API (contrato y reglas de negocio de las specs) a través de HTTP, como caja negra. Las pruebas funcionales validan lo que el consumidor puede observar; no prueban clases internas ni detalles de implementación.

## Fuente de entrada: test-cases.md

La entrada de este skill es `openspec/changes/<cambio>/test-cases.md` (o la ruta indicada en su encabezado), generado o actualizado por `software-testing-design`. Este skill **no diseña casos**: implementa los existentes.

Se automatizan solo las filas de la tabla de casos (sección 2) que cumplan a la vez:

```text
Automatizable = Sí o Parcial
Capa = Backend (o Ambas, solo la parte de backend)
Nivel = Componente, Integración o E2E
Estado distinto de OBSOLETO y distinto de NO VERIFICABLE
```

Reglas de uso:

- **Sin contrato ni spec, no se automatiza.** Un caso solo se automatiza si su comportamiento está respaldado a la vez por el contrato de API (`services/api/*.yaml`: ruta, método, headers, request y responses) y por la spec aprobada (escenario o criterio que define el resultado esperado). Si falta alguno de los dos, el caso queda **bloqueado**: no se escribe su prueba, ni siquiera como borrador o con `@Disabled`, hasta que ambos insumos existan. Se reporta en el resultado con el ID, el insumo que falta (contrato, spec o ambos) y quién debe aportarlo, y su `Estado` en `test-cases.md` permanece `NO VERIFICABLE`.
- Cuando el insumo faltante se aporta (contrato publicado o spec aprobada) y `software-testing-design` actualiza el caso, el caso pasa a ser aplicable y se automatiza en la siguiente ejecución.
- Si `test-cases.md` no existe, no generar pruebas: indicar que primero debe ejecutarse `software-testing-design`.
- Si un caso aplicable carece de datos, petición o resultado esperado suficientes, no lo inventes: márcalo como bloqueado en el reporte y devuélvelo al diseño con la información faltante.
- Si detectas un comportamiento del contrato o la spec sin caso en el archivo, repórtalo como brecha; no agregues casos por tu cuenta ni modifiques las columnas de diseño.
- Tras implementar y ejecutar, actualiza en `test-cases.md` solo las columnas `Prueba automatizada` (`Clase#método`) y `Estado` (`APROBADO` o `FALLIDO` con resultado real) de los casos trabajados, y deja `NO EJECUTADO` los no ejecutados. No cambies IDs, descripciones, prioridades ni cobertura.
- Los casos de Frontend, performance y seguridad no automatizables por API quedan fuera de este skill.

## Alcance y niveles

Este skill genera **solo** pruebas funcionales de API en tres niveles:

| Nivel | Qué valida | Dependencias | Tag |
| --- | --- | --- | --- |
| Componente | Un servicio de API aislado vía HTTP, con sus dependencias externas simuladas (`@InjectMock`, WireMock) | Ninguna real | `@Tag("component")` |
| Integración | El servicio con sus dependencias reales de prueba (persistencia, mensajería, adaptadores) usando recursos aislados (`@QuarkusTestResource`, Testcontainers si ya está aprobado) | Recursos de prueba | `@Tag("integration")` |
| E2E | Flujos completos entre servicios o contra un ambiente de pruebas desplegado, desde la frontera pública de la API | Ambiente de pruebas | `@Tag("e2e")` |

Fuera de alcance: las **pruebas unitarias** (clases, métodos o mapeadores aislados, con o sin Mockito). Las genera otro skill y viven en su propio paquete `unit`. Este skill no crea, modifica ni mueve pruebas unitarias, y no coloca pruebas funcionales en el paquete `unit`. Tampoco cubre pruebas de UI, rendimiento ni seguridad exhaustiva.

Referencias oficiales: documentación de Quarkus (Testing your application, `@QuarkusTest`, `@TestHTTPEndpoint`, `@TestProfile`, `QuarkusTestResourceLifecycleManager`), REST Assured (https://rest-assured.io/) y JUnit 5 (`@ParameterizedTest`, `@Tag`).

## Precondiciones

No generar pruebas si faltan:

```text
test-cases.md con casos aplicables (ver Fuente de entrada)
contrato del endpoint (método, ruta, headers, request, responses, códigos de error)
estrategia de datos de prueba y credenciales (sin secretos en el repo)
```

Fuentes en este repositorio:

- Casos: `openspec/changes/<cambio>/test-cases.md`.
- Contrato: `services/api/*.yaml` (OpenAPI), para validar y completar los datos de cada caso.
- Comportamiento esperado: escenarios `WHEN/THEN` de `openspec/changes/<cambio>/specs/**/spec.md` y criterios de aceptación del Issue, para confirmar el resultado esperado de cada caso.

No inventar campos ni códigos de estado. Si un caso contradice el contrato o la spec, no lo automatices: repórtalo como discrepancia. Un caso que no se puede observar por la API se deja fuera y se mantiene como está en `test-cases.md`.

## Procedimiento

1. Lee `test-cases.md` y filtra los casos aplicables (ver Fuente de entrada).
2. Valida cada caso contra el contrato (ruta, método, headers, body, respuestas) y la spec; lista discrepancias y casos bloqueados.
3. Verifica el módulo Maven bajo `services/back-end/<modulo>` y sus dependencias de test (`quarkus-junit5`, `rest-assured`); no agregues otro framework.
4. Crea las clases de test en el paquete que corresponda al nivel (`functional/component`, `functional/integration`, `functional/e2e`), con `@QuarkusTest` en componente e integración, y su `@Tag` de nivel. Nunca las crees en el paquete `unit`. Para flujos de varios pasos (por ejemplo, verificar y luego ejecutar), usa un test por flujo con los pasos en orden dentro del mismo método.
5. Centraliza constantes, headers y construcción de requests en un helper de test o `RequestSpecification` compartida.
6. Valida status, headers y cuerpo con Hamcrest (`body(...)`) o `JsonPath`; para esquemas usa `json-schema-validator` solo si ya está en el `pom.xml` o se aprueba agregarlo.
7. Parametriza los casos negativos y de borde con `@ParameterizedTest`.
8. Referencia en cada test el ID del caso (`TC-xxx`) y el escenario o criterio que verifica (ver Trazabilidad).
9. Ejecuta la suite, revisa `target/surefire-reports` y corrige flakiness.
10. Actualiza `Prueba automatizada` y `Estado` en `test-cases.md` con resultados reales y documenta cómo ejecutar por nivel y por tag.

## Trazabilidad con las specs

- Nombre del método descriptivo del comportamiento (`executeTransferRejectsAmountAboveLimit`), no del endpoint.
- `@DisplayName` con el ID del caso y el escenario de la spec, y un comentario de una línea solo si el nombre no basta:

```java
@Test
@Tag("regression")
@DisplayName("TC-012 payment-confirmation: Importe que excede algun limite -> el envio se bloquea")
void amountAboveLimitIsBlocked() { /* ... */ }
```

- Cada caso aplicable de `test-cases.md` termina con al menos un test que lo referencia por su ID; el vínculo inverso se registra en la columna `Prueba automatizada`.

## Estructura del proyecto

```text
services/back-end/<modulo>/
  pom.xml
  src/main/resources/application.properties
  src/test/java/<paquete>/
    unit/                         # pruebas unitarias: otro skill, no tocar
    functional/                   # este skill
      component/<Recurso>ComponentTest.java      # @QuarkusTest + @Tag("component")
      integration/<Flujo>IntegrationTest.java    # @QuarkusTest + @Tag("integration")
      e2e/<Flujo>E2ETest.java                    # @Tag("e2e"), contra ambiente de pruebas
      support/
        TestRequests.java         # RequestSpecification y headers comunes
        TestData.java             # datos sintéticos y builders de request
  src/test/resources/
    application.properties        # overrides de test (opcional)
    data/                         # JSON/CSV para pruebas parametrizadas
```

Si el equipo prefiere un directorio fuente separado (por ejemplo `src/functional-test/java`), requiere cambios en el `pom.xml` y aprobación previa; mientras tanto se mantiene la separación por paquete y tag.

## Configuración

- Quarkus levanta la aplicación en un puerto de test y configura REST Assured automáticamente: no hardcodear `localhost` ni puertos. Si hace falta ruta base, usar `@TestHTTPEndpoint(Recurso.class)` o `RestAssured.basePath`.
- Overrides para test: `src/test/resources/application.properties` o perfiles con `@TestProfile`. Nunca URLs de ambientes reales en los tests.
- Para pruebas contra un ambiente desplegado, usar una propiedad de sistema (`-Dtest.base-url=...`) leída en el helper y sin valor por defecto que apunte a producción.
- Credenciales y secretos: variables de entorno (`System.getenv`) o propiedades; nunca en el repositorio. Los valores mock del servicio local (por ejemplo, tokens ficticios) pueden vivir en `TestData`, marcados claramente como mock.
- Dependencias externas: sustituir con `@InjectMock`, `@QuarkusTestResource` o WireMock; nunca llamar a sistemas externos o productivos.

## Plantilla de test

```java
package pe.com.wallet.channel.contact.transfer.v1.functional.component;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
@Tag("component")
class AffiliationWalletComponentTest {

    private RequestSpecification authenticated() {
        return given()
            .contentType(ContentType.JSON)
            .header("sessionId", "session-123")
            .header("autentication", "mock-auth-header")
            .header("Authorization", "Bearer mock-token");
    }

    @Test
    @Tag("smoke")
    void affiliationSucceeds() {
        authenticated()
            .body("""
                { "documentType": "DNI", "documentNumber": "QUJDRA==",
                  "phone": "MTIzNA==", "email": "ZGVtb0BleGFtcGxlLmNvbQ==" }
                """)
        .when()
            .post("/affiliation-wallet")
        .then()
            .statusCode(204);
    }

    @Test
    @Tag("negative")
    void affiliationWithoutAuthorizationIsRejected() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/affiliation-wallet")
        .then()
            .statusCode(401);
    }

    @ParameterizedTest
    @Tag("negative")
    @CsvSource({ "RkFJTF80MjI=, 422, H9999" })
    void affiliationErrorsFollowContract(String documentNumber, int status, String code) {
        authenticated()
            .body("{\"documentType\":\"DNI\",\"documentNumber\":\"" + documentNumber + "\"}")
        .when()
            .post("/affiliation-wallet")
        .then()
            .statusCode(status)
            .body("code", equalTo(code));
    }
}
```

Nota: rutas, headers, códigos y cuerpos son ilustrativos; ajustarlos al contrato real.

## Buenas prácticas

- Un test = un comportamiento verificable; tests independientes y sin orden implícito.
- Verificar siempre `statusCode` y, cuando aplique, `header(...)` y `contentType`; en errores, validar el cuerpo de error del contrato (`code`, `message`).
- Preferir aserciones específicas de Hamcrest sobre comparar cuerpos completos frágiles: `notNullValue()`, `hasSize`, `everyItem`, `containsInAnyOrder`.
- Para respuestas complejas, deserializar a un tipo (`.extract().as(Clase.class)`) o usar `JsonPath`.
- Cubrir casos negativos: campos faltantes, tipos inválidos, headers de autenticación ausentes o inválidos, valores límite.
- Parametrizar con `@ParameterizedTest` + `@CsvSource`/`@MethodSource`/`@ValueSource`; volúmenes grandes en `src/test/resources/data`.
- Etiquetar con `@Tag("smoke")`, `@Tag("regression")`, `@Tag("negative")` y filtrar en Maven con `-Dgroups=smoke`.
- Los tests que crean datos deben limpiarlos (`@AfterEach`) o usar datos únicos (`UUID.randomUUID()`).
- Evitar estado compartido mutable entre tests; no depender del orden de ejecución.
- Sin `Thread.sleep`; para operaciones asíncronas legítimas usar Awaitility con timeout acotado.
- Logging: `.log().ifValidationFails()` en lugar de `.log().all()`; no registrar headers de autorización ni datos sensibles (`RestAssured.config` puede ocultarlos con `blacklistHeaders`).
- Reportes: Surefire en `target/surefire-reports`; publicarlos como artefacto en CI.

## Reglas

- no incluir credenciales reales ni tokens en tests, recursos o logs;
- no hardcodear URLs, puertos ni ambientes en los tests;
- no inventar contratos, campos o códigos de estado;
- no automatizar casos sin contrato o sin spec que los respalden; quedan bloqueados hasta contar con ambos insumos;
- no automatizar casos que no estén en `test-cases.md` ni modificar el diseño de casos;
- no desactivar (`@Disabled`) ni comentar pruebas para conseguir verde;
- no ejecutar contra producción pruebas que mutan datos;
- no agregar otro framework de API (Karate, etc.) sin aprobación;
- no generar pruebas unitarias ni colocar pruebas funcionales en el paquete `unit`;
- no mezclar pruebas de UI con la suite de API;
- no reportar resultados que no se hayan ejecutado.

## Definition of Done

- cada endpoint del alcance tiene casos positivos y negativos;
- cada caso aplicable de `test-cases.md` tiene al menos una prueba, o está reportado como bloqueado con la información faltante;
- las pruebas son funcionales de caja negra: solo interactúan por HTTP, sin invocar clases internas;
- los tests son deterministas e independientes del orden;
- validaciones sobre status, headers y cuerpo según contrato;
- sin secretos en el repositorio;
- `mvn test` desde el módulo pasa y los resultados reales están documentados;
- instrucciones de ejecución en el README del módulo o docs de testing.

## Ejecución

Desde el módulo (`services/back-end/<modulo>`):

```text
mvn test                                  # toda la suite
mvn test -Dgroups=smoke                   # por tag
mvn test -Dgroups=component               # por nivel: component | integration | e2e
mvn test -Dtest=AffiliationWalletComponentTest     # por clase
```

## Salida esperada

```text
casos de test-cases.md automatizados / bloqueados / fuera de alcance (con IDs)
discrepancias o brechas detectadas contra el contrato y la spec
clases de test creadas/modificadas y helpers
datos de prueba (src/test/resources/data)
columnas Prueba automatizada y Estado actualizadas en test-cases.md
comando de ejecución y resultados reales
supuestos y riesgos pendientes
```
