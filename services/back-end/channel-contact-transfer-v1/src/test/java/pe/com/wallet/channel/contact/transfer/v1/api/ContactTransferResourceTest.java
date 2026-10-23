package pe.com.wallet.channel.contact.transfer.v1.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ContactTransferResourceTest {

    private static final String SESSION_ID = "session-123";
    private static final String AUTHORIZATION = "Bearer mock-token";

    @Test
    void affiliateWalletHappyPath() {
        String requestBody = """
            {
              "documentType": "DNI",
              "documentNumber": "QUJDRA==",
              "phone": "MTIzNA==",
                            "email": "ZGVtb0BleGFtcGxlLmNvbQ==",
                            "password": "c2VjcmV0MTIz"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/affiliation-wallet")
        .then()
            .statusCode(204);
    }

    @Test
    void affiliateWalletUnhappyPath422() {
        String requestBody = """
            {
              "documentType": "DNI",
              "documentNumber": "RkFJTF80MjI=",
              "phone": "MTIzNA==",
                            "email": "ZGVtb0BleGFtcGxlLmNvbQ==",
                            "password": "c2VjcmV0MTIz"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/affiliation-wallet")
        .then()
            .statusCode(422)
            .body("code", equalTo("HB0700"));
    }

    @Test
    void affiliateWalletUnauthorized() {
        String requestBody = """
            {
              "documentType": "DNI",
              "documentNumber": "QUJDRA==",
              "phone": "MTIzNA==",
                            "email": "ZGVtb0BleGFtcGxlLmNvbQ==",
                            "password": "c2VjcmV0MTIz"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .body(requestBody)
        .when()
            .post("/affiliation-wallet")
        .then()
            .statusCode(401)
            .body("code", equalTo("HB0302"));
    }

    @Test
    void searchFinancialEntitiesHappyPath() {
        String requestBody = """
            {
              "phone": "MTIzNA=="
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/financial-entities/search")
        .then()
            .statusCode(200)
            .body("", hasSize(2))
            .body("[0].entity", equalTo("Yape"));
    }

    @Test
    void searchFinancialEntitiesUnhappyPath500() {
        String requestBody = """
            {
              "phone": "RkFJTF81MDA="
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/financial-entities/search")
        .then()
            .statusCode(500)
            .body("code", equalTo("H99999"));
    }

    @Test
    void verifyTransferHappyPath() {
        String requestBody = """
            {
              "amount": 25.50
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/authorization/verify")
        .then()
            .statusCode(200)
            .body("origin.name", equalTo("M**** P****"))
            .body("destination.financialEntity.code", equalTo("01"));
    }

    @Test
    void verifyTransferUnhappyPath422() {
        String requestBody = """
            {
              "amount": 422.00
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/authorization/verify")
        .then()
            .statusCode(422)
            .body("code", equalTo("HB0700"));
    }

    @Test
    void verifyTransferUnhappyPath400() {
        String requestBody = """
            {
              "amount": 0.00
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/authorization/verify")
        .then()
            .statusCode(400)
            .body("code", equalTo("HB0305"));
    }

    @Test
    void verifyTransferRejectsAmountAboveMaximum() {
        String requestBody = """
            {
              "amount": 100000000.00
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/authorization/verify")
        .then()
            .statusCode(400)
            .body("code", equalTo("HB0305"));
    }

    @Test
    void executeTransferHappyPath() {
        String requestBody = """
            {
              "description": "Pago de almuerzo"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/execute")
        .then()
            .statusCode(200)
            .body("operationCode", equalTo("CT202610230001"))
            .body("transactionDateTime", notNullValue());
    }

    @Test
    void executeTransferUnhappyPath422() {
        String requestBody = """
            {
              "description": "fail422 prueba"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("sessionId", SESSION_ID)
            .header("Authorization", AUTHORIZATION)
            .header("autentication", "mock-auth-header")
            .body(requestBody)
        .when()
            .post("/contact-transfers/execute")
        .then()
            .statusCode(422)
            .body("code", equalTo("HB0700"));
    }
}
