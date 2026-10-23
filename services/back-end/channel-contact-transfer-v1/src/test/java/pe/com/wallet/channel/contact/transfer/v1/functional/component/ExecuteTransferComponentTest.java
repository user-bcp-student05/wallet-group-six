package pe.com.wallet.channel.contact.transfer.v1.functional.component;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.DOCUMENT_PLAIN;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.EXECUTE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.PHONE_PLAIN;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticated;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.descriptionBody;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@Tag("component")
class ExecuteTransferComponentTest {

    @Test
    @Tag("smoke")
    @DisplayName("TC-044 CA-5 payment-status-resolution: Éxito confirmado -> operationCode y transactionDateTime válidos")
    void tc044_executeReturnsOperationCodeAndTransactionDateTime() {
        authenticated().body(descriptionBody("Pago de almuerzo"))
            .when().post(EXECUTE)
            .then().statusCode(200)
            .body("operationCode", matchesPattern("^[A-Za-z0-9_-]{1,64}$"))
            .body("transactionDateTime",
                matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|[+-]\\d{2}:\\d{2})$"));
    }

    @Test
    @DisplayName("TC-045 CA-5: description de 140 caracteres (límite) -> 200")
    void tc045_executeAcceptsMaxLengthDescription() {
        authenticated().body(descriptionBody("a".repeat(140)))
            .when().post(EXECUTE)
            .then().statusCode(200);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-046 CA-5: description de 141 caracteres -> 400 HB0305")
    void tc046_executeRejectsTooLongDescription() {
        authenticated().body(descriptionBody("a".repeat(141)))
            .when().post(EXECUTE)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @Tag("negative")
    @DisplayName("TC-047 CA-5: description vacía, en blanco o ausente -> 400 HB0305")
    void tc047_executeRejectsBlankOrMissingDescription(String description) {
        String body = description == null ? "{}" : descriptionBody(description);
        authenticated().body(body)
            .when().post(EXECUTE)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @DisplayName("TC-048 CA-5 payment-history-and-measurement: Referencia tras éxito -> operationCode sin datos personales")
    void tc048_operationCodeDoesNotContainPersonalData() {
        authenticated().body(descriptionBody("Pago de almuerzo"))
            .when().post(EXECUTE)
            .then().statusCode(200)
            .body("operationCode", not(containsString(DOCUMENT_PLAIN)))
            .body("operationCode", not(containsString(PHONE_PLAIN)))
            .body("operationCode", not(containsString("999888777")))
            .body("operationCode", not(containsString("J****")));
    }

    @Test
    @DisplayName("TC-052 CA-5 payment-history-and-measurement: Referencia tras éxito -> solo operationCode y transactionDateTime")
    void tc052_executeResponseExposesOnlyContractFields() {
        var keys = authenticated().body(descriptionBody("Pago de almuerzo"))
            .when().post(EXECUTE)
            .then().statusCode(200)
            .extract().jsonPath().getMap("$").keySet();
        org.hamcrest.MatcherAssert.assertThat(keys, equalTo(Set.of("operationCode", "transactionDateTime")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "fail422", "FAIL422", "Pago FaIl422" })
    @Tag("negative")
    @DisplayName("TC-053 CA-6 payment-status-resolution: Fallo confirmado -> 422 HB0700")
    void tc053_executeRejectedTransferReturns422(String description) {
        authenticated().body(descriptionBody(description))
            .when().post(EXECUTE)
            .then().statusCode(422).body("code", equalTo("HB0700"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-055 CA-6: execute con description fail500 -> 500 H9999")
    void tc055_executeInternalErrorReturns500() {
        authenticated().body(descriptionBody("fail500"))
            .when().post(EXECUTE)
            .then().statusCode(500).body("code", equalTo("H9999"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "'; DROP TABLE x;--", "<script>alert(1)</script>" })
    @Tag("negative")
    @DisplayName("TC-025 CA-2: description con inyección -> 200 sin reflejar el texto")
    void tc025_executeDoesNotReflectInjectionPayloads(String description) {
        authenticated().body(descriptionBody(description))
            .when().post(EXECUTE)
            .then().statusCode(200)
            .body(not(containsString("DROP TABLE")))
            .body(not(containsString("<script>")));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-026 CA-2: description con carácter de control -> 400 HB0305")
    void tc026_executeRejectsControlCharacters() {
        authenticated().body("{\"description\":\"hola\\u0001mundo\"}")
            .when().post(EXECUTE)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "GET", "PUT" })
    @Tag("negative")
    @DisplayName("TC-028 CA-2: método HTTP no permitido sobre execute -> 405")
    void tc028_executeRejectsUnsupportedMethods(String method) {
        given().header("sessionId", "session-123")
            .header("autentication", "mock-auth-header")
            .header("Authorization", "Bearer mock-token")
            .contentType(ContentType.JSON)
            .when().request(method, EXECUTE)
            .then().statusCode(405);
    }
}
