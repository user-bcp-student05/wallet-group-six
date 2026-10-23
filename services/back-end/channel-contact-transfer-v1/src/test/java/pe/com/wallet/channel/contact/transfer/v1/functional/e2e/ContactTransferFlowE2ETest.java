package pe.com.wallet.channel.contact.transfer.v1.functional.e2e;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.EXECUTE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.SEARCH;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VALID_PHONE;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.VERIFY;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.amountBody;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.authenticatedWithSession;
import static pe.com.wallet.channel.contact.transfer.v1.functional.support.TestRequests.descriptionBody;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

// Se ejecuta en proceso: no hay ambiente de pruebas desplegado.
@QuarkusTest
@Tag("e2e")
class ContactTransferFlowE2ETest {

    @Test
    @Tag("smoke")
    @DisplayName("TC-003 CA-1 payment-confirmation: Revisión previa -> search, verify y execute con el mismo sessionId")
    void tc003_searchVerifyAndExecuteFlow() {
        String session = "flow-" + java.util.UUID.randomUUID();

        authenticatedWithSession(session).body("{\"phone\":\"" + VALID_PHONE + "\"}")
            .when().post(SEARCH)
            .then().statusCode(200).body("", hasSize(2));

        authenticatedWithSession(session).body(amountBody("25.50"))
            .when().post(VERIFY)
            .then().statusCode(200)
            .body("destination.financialEntity.entity", equalTo("Yape"));

        authenticatedWithSession(session).body(descriptionBody("Pago de almuerzo"))
            .when().post(EXECUTE)
            .then().statusCode(200)
            .body("operationCode", matchesPattern("^[A-Za-z0-9_-]{1,64}$"));
    }
}
