package pe.com.wallet.channel.contact.transfer.v1.functional.support;

import static io.restassured.RestAssured.given;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class TestRequests {

    // Valores ficticios del servicio mock; no son credenciales reales.
    public static final String SESSION_ID = "session-123";
    public static final String AUTENTICATION = "mock-auth-header";
    public static final String BEARER = "Bearer mock-token";

    public static final String AFFILIATION = "/affiliation-wallet";
    public static final String SEARCH = "/contact-transfers/financial-entities/search";
    public static final String VERIFY = "/contact-transfers/authorization/verify";
    public static final String EXECUTE = "/contact-transfers/execute";
    public static final List<String> ALL_ENDPOINTS = List.of(AFFILIATION, SEARCH, VERIFY, EXECUTE);

    public static final String PHONE_PLAIN = "+51999888777";
    public static final String VALID_PHONE = b64(PHONE_PLAIN);
    public static final String DOCUMENT_PLAIN = "ABCD1234";
    public static final String FAIL_422 = b64("FAIL_422");
    public static final String FAIL_500 = b64("FAIL_500");

    private TestRequests() {
    }

    /** Headers válidos H del catálogo de casos. */
    public static RequestSpecification authenticated() {
        return authenticatedWithSession(SESSION_ID);
    }

    public static RequestSpecification authenticatedWithSession(String sessionId) {
        return given()
            .contentType(ContentType.JSON)
            .header("sessionId", sessionId)
            .header("autentication", AUTENTICATION)
            .header("Authorization", BEARER);
    }

    public static RequestSpecification withoutAuthorization() {
        return given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("autentication", AUTENTICATION);
    }

    public static RequestSpecification withoutAutentication() {
        return given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", BEARER);
    }

    public static String b64(String plain) {
        return Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

    public static String affiliationBody(String documentType, String documentNumber, String phone, String email) {
        return "{\"documentType\":%s,\"documentNumber\":%s,\"phone\":%s,\"email\":%s,\"password\":%s}"
            .formatted(json(documentType), json(documentNumber), json(phone), json(email), json(b64("Passw0rd!x")));
    }

    /** El contrato declara password como requerido; este cuerpo lo omite. */
    public static String affiliationBodyWithoutPassword(String documentType, String documentNumber, String phone, String email) {
        return "{\"documentType\":%s,\"documentNumber\":%s,\"phone\":%s,\"email\":%s}"
            .formatted(json(documentType), json(documentNumber), json(phone), json(email));
    }

    public static String validAffiliationBody() {
        return affiliationBody("DNI", b64(DOCUMENT_PLAIN), VALID_PHONE, b64("demo@example.com"));
    }

    public static String amountBody(String amount) {
        return "{\"amount\":" + amount + "}";
    }

    public static String descriptionBody(String description) {
        return "{\"description\":\"" + description + "\"}";
    }

    /** Cuerpo válido de cada endpoint del contrato. */
    public static String validBodyFor(String path) {
        return switch (path) {
            case AFFILIATION -> validAffiliationBody();
            case SEARCH -> "{\"phone\":\"" + VALID_PHONE + "\"}";
            case VERIFY -> amountBody("25.50");
            case EXECUTE -> descriptionBody("Pago de almuerzo");
            default -> throw new IllegalArgumentException(path);
        };
    }

    private static String json(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
