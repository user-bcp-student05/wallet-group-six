package pe.com.wallet.channel.contact.transfer.v1.functional.component;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.AFFILIATION;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.DOCUMENT_PLAIN;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.FAIL_422;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.FAIL_500;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VALID_PHONE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.affiliationBody;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.affiliationBodyWithoutPassword;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticated;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.b64;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.validAffiliationBody;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@Tag("component")
class AffiliationWalletComponentTest {

    private static final String EMAIL = b64("demo@example.com");

    @Test
    @Tag("smoke")
    @DisplayName("TC-079 Contrato affiliation-wallet: afiliación válida -> 204 sin contenido")
    void tc079_affiliationSucceedsWithNoContent() {
        authenticated().body(validAffiliationBody())
            .when().post(AFFILIATION)
            .then().statusCode(204).body(equalTo(""));
    }

    @ParameterizedTest
    @ValueSource(strings = { "DNI", "CE", "PASSPORT" })
    @DisplayName("TC-080 Contrato affiliation-wallet: cada tipo de documento -> 204")
    void tc080_affiliationAcceptsEveryDocumentType(String documentType) {
        authenticated().body(affiliationBody(documentType, b64(DOCUMENT_PLAIN), VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(204);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-081 Contrato affiliation-wallet: documentType fuera de la enumeración -> 400")
    void tc081_affiliationRejectsUnknownDocumentType() {
        authenticated().body(affiliationBody("RUC", b64(DOCUMENT_PLAIN), VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(400);
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-081 Contrato affiliation-wallet: documentType ausente -> 400 HB0305")
    void tc081_affiliationRejectsMissingDocumentType() {
        authenticated().body(affiliationBody(null, b64(DOCUMENT_PLAIN), VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "abc", "no es base64!" })
    @Tag("negative")
    @DisplayName("TC-082 Contrato affiliation-wallet: documentNumber vacío, ausente o no Base64 -> 400 HB0305")
    void tc082_affiliationRejectsInvalidDocumentNumber(String documentNumber) {
        authenticated().body(affiliationBody("DNI", documentNumber, VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "abc", "no es base64!" })
    @Tag("negative")
    @DisplayName("TC-082 Contrato affiliation-wallet: phone vacío, ausente o no Base64 -> 400 HB0305")
    void tc082_affiliationRejectsInvalidPhone(String phone) {
        authenticated().body(affiliationBody("DNI", b64(DOCUMENT_PLAIN), phone, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "abc", "no es base64!" })
    @Tag("negative")
    @DisplayName("TC-082 Contrato affiliation-wallet: email vacío, ausente o no Base64 -> 400 HB0305")
    void tc082_affiliationRejectsInvalidEmail(String email) {
        authenticated().body(affiliationBody("DNI", b64(DOCUMENT_PLAIN), VALID_PHONE, email))
            .when().post(AFFILIATION)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-083 Contrato affiliation-wallet: sin password (requerido por el contrato) -> 400 HB0305")
    void tc083_affiliationRequiresPassword() {
        authenticated().body(affiliationBodyWithoutPassword("DNI", b64(DOCUMENT_PLAIN), VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-084 Contrato affiliation-wallet: documento rechazado -> 422 H9999")
    void tc084_affiliationRejectedForDocumentReturns422() {
        authenticated().body(affiliationBody("DNI", FAIL_422, VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(422)
            .body("code", equalTo("H9999"))
            .body("description", notNullValue());
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-085 Contrato affiliation-wallet: error interno -> 500 H9999")
    void tc085_affiliationInternalErrorReturns500() {
        authenticated().body(affiliationBody("DNI", FAIL_500, VALID_PHONE, EMAIL))
            .when().post(AFFILIATION)
            .then().statusCode(500).body("code", equalTo("H9999"));
    }
}
