package pe.com.wallet.channel.contact.transfer.v1.functional.component;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VERIFY;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.amountBody;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticated;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticatedWithSession;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@Tag("component")
class VerifyTransferComponentTest {

    @Test
    @Tag("smoke")
    @DisplayName("TC-001 CA-1 payment-confirmation: Revisión previa -> verify devuelve origen y destino a confirmar")
    void tc001_verifyShowsOriginAndDestinationBeforeConfirming() {
        authenticated().body(amountBody("25.50"))
            .when().post(VERIFY)
            .then().statusCode(200)
            .body("origin.name", equalTo("M**** P****"))
            .body("destination.name", equalTo("J**** R****"))
            .body("destination.financialEntity.code", equalTo("01"))
            .body("destination.financialEntity.entity", equalTo("Yape"));
    }

    @Test
    @DisplayName("TC-005 CA-1: nombres enmascarados y sin documento, teléfono ni correo en la respuesta")
    void tc005_verifyMasksNamesAndHidesPersonalData() {
        authenticated().body(amountBody("25.50"))
            .when().post(VERIFY)
            .then().statusCode(200)
            .body("origin.name", matchesPattern("^\\S\\*+ \\S\\*+$"))
            .body("destination.name", matchesPattern("^\\S\\*+ \\S\\*+$"))
            .body(not(containsString("documentNumber")))
            .body(not(containsString("phone")))
            .body(not(containsString("email")));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-006 CA-1: verify sin amount -> 400 HB0305")
    void tc006_verifyWithoutAmountReturns400() {
        authenticated().body("{}")
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @DisplayName("TC-007 CA-2 payment-confirmation: Importe mínimo -> amount 0.10 -> 200")
    void tc007_verifyAcceptsMinimumAmount() {
        authenticated().body(amountBody("0.10"))
            .when().post(VERIFY)
            .then().statusCode(200);
    }

    @Test
    @DisplayName("TC-008 CA-2 payment-confirmation: Importe máximo -> amount 500.00 -> 200")
    void tc008_verifyAcceptsMaximumAmount() {
        authenticated().body(amountBody("500.00"))
            .when().post(VERIFY)
            .then().statusCode(200);
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "0.00" })
    @Tag("negative")
    @DisplayName("TC-009 CA-2 payment-confirmation: Importe cero -> 400 HB0305")
    void tc009_verifyBlocksZeroAmount(String amount) {
        authenticated().body(amountBody(amount))
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-010 CA-2 payment-confirmation: Importe negativo -> 400 HB0305")
    void tc010_verifyBlocksNegativeAmount() {
        authenticated().body(amountBody("-1"))
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-011 CA-2 payment-confirmation: Importe sobre el máximo del contrato -> 400 HB0305")
    void tc011_verifyBlocksAmountAboveContractMaximum() {
        authenticated().body(amountBody("100000000.00"))
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.234", "0.001" })
    @Tag("negative")
    @DisplayName("TC-012 CA-2: amount con más de 2 decimales -> 400 HB0305")
    void tc012_verifyBlocksMoreThanTwoDecimals(String amount) {
        authenticated().body(amountBody(amount))
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-013 CA-2: amount no numérico -> 400")
    void tc013_verifyRejectsNonNumericAmount() {
        authenticated().body("{\"amount\":\"abc\"}")
            .when().post(VERIFY)
            .then().statusCode(400);
    }

    @Test
    @DisplayName("TC-023 CA-2: sessionId de 128 caracteres -> 200; de 129 -> 400 HB0305")
    void tc023_verifySessionIdLengthBoundary() {
        authenticatedWithSession("a".repeat(128)).body(amountBody("25.50"))
            .when().post(VERIFY)
            .then().statusCode(200);
        authenticatedWithSession("a".repeat(129)).body(amountBody("25.50"))
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-027 CA-2: campo no declarado en el cuerpo (additionalProperties false) -> 400 HB0305")
    void tc027_verifyRejectsUnexpectedField() {
        authenticated().body("{\"amount\":25.50,\"extra\":\"x\"}")
            .when().post(VERIFY)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-054 CA-6 payment-status-resolution: Fallo confirmado -> verify amount 422.00 -> 422 HB0700")
    void tc054_verifyRejectedTransferReturns422() {
        authenticated().body(amountBody("422.00"))
            .when().post(VERIFY)
            .then().statusCode(422).body("code", equalTo("HB0700"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-056 CA-6: verify amount 499.99 -> 500 H99999")
    void tc056_verifyInternalErrorReturns500() {
        authenticated().body(amountBody("499.99"))
            .when().post(VERIFY)
            .then().statusCode(500).body("code", equalTo("H99999"));
    }
}
