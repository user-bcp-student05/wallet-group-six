package pe.com.wallet.channel.contact.transfer.v1.functional.component;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.ALL_ENDPOINTS;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.AUTENTICATION;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.BEARER;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.EXECUTE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.SESSION_ID;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VALID_PHONE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VERIFY;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.amountBody;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticated;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticatedWithSession;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.validBodyFor;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.withoutAuthorization;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.withoutAutentication;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@Tag("component")
class CommonRequestRulesComponentTest {

    static Stream<String> endpoints() {
        return ALL_ENDPOINTS.stream();
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    @Tag("negative")
    @DisplayName("TC-019 CA-2: sin Authorization -> 401 HB0302")
    void tc019_missingAuthorizationReturns401(String path) {
        withoutAuthorization().body(validBodyFor(path))
            .when().post(path)
            .then().statusCode(401).body("code", equalTo("HB0302"));
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    @Tag("negative")
    @DisplayName("TC-020 CA-2: Authorization con esquema distinto de Bearer o vacío -> 401 HB0302")
    void tc020_invalidAuthorizationSchemeReturns401(String path) {
        for (String value : new String[] { "Basic abc123", "Bearer", "" }) {
            withoutAuthorization().header("Authorization", value).body(validBodyFor(path))
                .when().post(path)
                .then().statusCode(401).body("code", equalTo("HB0302"));
        }
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    @Tag("negative")
    @DisplayName("TC-021 CA-2: sin header autentication -> 400 HB0305")
    void tc021_missingAutenticationReturns400(String path) {
        withoutAutentication().body(validBodyFor(path))
            .when().post(path)
            .then().statusCode(400).body("code", equalTo("HB0305"));
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    @Tag("negative")
    @DisplayName("TC-022 CA-2: sessionId con caracteres fuera del patrón -> 400 HB0305")
    void tc022_invalidSessionIdReturns400(String path) {
        for (String sessionId : new String[] { "bad id!", "con espacios", "a/b" }) {
            authenticatedWithSession(sessionId).body(validBodyFor(path))
                .when().post(path)
                .then().statusCode(400).body("code", equalTo("HB0305"));
        }
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-024 CA-2: errores 400, 401, 422 y 500 solo con code y description, sin datos enviados")
    void tc024_errorBodiesDoNotLeakData() {
        var bodies = new String[] {
            withoutAuthorization().body(amountBody("25.50")).post(VERIFY).then().statusCode(401).extract().asString(),
            authenticated().body(amountBody("0")).post(VERIFY).then().statusCode(400).extract().asString(),
            authenticated().body(amountBody("422.00")).post(VERIFY).then().statusCode(422).extract().asString(),
            authenticated().body(amountBody("499.99")).post(VERIFY).then().statusCode(500).extract().asString(),
        };
        for (String body : bodies) {
            var keys = io.restassured.path.json.JsonPath.from(body).getMap("$").keySet();
            assertThat(keys, equalTo(Set.of("code", "description")));
            assertThat(body, not(containsString("mock-token")));
            assertThat(body, not(containsString("Bearer")));
            assertThat(body, not(containsString(AUTENTICATION)));
            assertThat(body, not(containsString(SESSION_ID)));
            assertThat(body, not(containsString("Exception")));
        }
    }

    @Test
    @Tag("negative")
    @DisplayName("TC-029 CA-2: Content-Type text/plain -> 415")
    void tc029_unsupportedContentTypeReturns415() {
        given().contentType(ContentType.TEXT)
            .header("sessionId", SESSION_ID)
            .header("autentication", AUTENTICATION)
            .header("Authorization", BEARER)
            .body("amount=25.50")
            .when().post(VERIFY)
            .then().statusCode(415);
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    @Tag("negative")
    @DisplayName("TC-089 Contrato: cuerpo ausente -> 400")
    void tc089_missingBodyReturns400(String path) {
        authenticated()
            .when().post(path)
            .then().statusCode(400);
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    @Tag("negative")
    @DisplayName("TC-090 Contrato: JSON malformado -> 400")
    void tc090_malformedJsonReturns400(String path) {
        authenticated().body("{\"amount\":")
            .when().post(path)
            .then().statusCode(400);
    }
}
