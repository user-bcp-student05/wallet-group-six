package pe.com.wallet.channel.contact.transfer.v1.functional.component;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.FAIL_500;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.SEARCH;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VALID_PHONE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticated;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@Tag("component")
class SearchFinancialEntitiesComponentTest {

    @Test
    @Tag("smoke")
    @DisplayName("TC-086 Contrato financial-entities/search: teléfono válido -> 200 con Yape y Plin")
    void tc086_searchReturnsWalletsForPhone() {
        authenticated().body("{\"phone\":\"" + VALID_PHONE + "\"}")
            .when().post(SEARCH)
            .then().statusCode(200)
            .body("", hasSize(2))
            .body("[0].code", equalTo("01"))
            .body("[0].entity", equalTo("Yape"))
            .body("[1].code", equalTo("02"))
            .body("[1].entity", equalTo("Plin"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "abc", "no es base64!" })
    @Tag("negative")
    @DisplayName("TC-087 Contrato financial-entities/search: phone vacío o no Base64 -> 400 HB0305")
    void tc087_searchWithInvalidPhoneReturns400(String phone) {
        authenticated().body("{\"phone\":\"" + phone + "\"}")
            .when().post(SEARCH)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-087 Contrato financial-entities/search: phone ausente -> 400 HB0305")
    void tc087_searchWithoutPhoneReturns400() {
        authenticated().body("{}")
            .when().post(SEARCH)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-088 Contrato financial-entities/search: phone que dispara error interno -> 500 H9999")
    void tc088_searchInternalErrorReturns500() {
        authenticated().body("{\"phone\":\"" + FAIL_500 + "\"}")
            .when().post(SEARCH)
            .then().statusCode(500).body("code", equalTo("H9999"));
    }
}
