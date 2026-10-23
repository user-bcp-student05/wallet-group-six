package pe.com.wallet.channel.contact.transfer.v1.api;

import java.util.List;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.com.wallet.channel.contact.transfer.v1.exception.ApiException;
import pe.com.wallet.channel.contact.transfer.v1.model.FinancialEntitiesSearchRequest;
import pe.com.wallet.channel.contact.transfer.v1.model.FinancialEntity;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferExecutionRequest;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferExecutionResponse;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferVerificationRequest;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferVerificationResponse;
import pe.com.wallet.channel.contact.transfer.v1.model.WalletAffiliationRequest;
import pe.com.wallet.channel.contact.transfer.v1.service.MockContactTransferService;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ContactTransferResource {

    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]{1,128}$");

    @Inject
    MockContactTransferService service;

    @POST
    @Path("/affiliation-wallet")
    public Response affiliateWallet(
        @HeaderParam("sessionId") String sessionId,
        @HeaderParam("Authorization") String authorization,
        @NotNull @Valid WalletAffiliationRequest request
    ) {
        validateHeaders(sessionId, authorization);
        service.affiliateWallet(request);
        return Response.noContent().build();
    }

    @POST
    @Path("/contact-transfers/financial-entities/search")
    public List<FinancialEntity> searchFinancialEntitiesByContact(
        @HeaderParam("sessionId") String sessionId,
        @HeaderParam("Authorization") String authorization,
        @NotNull @Valid FinancialEntitiesSearchRequest request
    ) {
        validateHeaders(sessionId, authorization);
        return service.searchFinancialEntities(request);
    }

    @POST
    @Path("/contact-transfers/authorization/verify")
    public TransferVerificationResponse verifyContactTransfer(
        @HeaderParam("sessionId") String sessionId,
        @HeaderParam("Authorization") String authorization,
        @NotNull @Valid TransferVerificationRequest request
    ) {
        validateHeaders(sessionId, authorization);
        return service.verifyTransfer(request);
    }

    @POST
    @Path("/contact-transfers/execute")
    public TransferExecutionResponse executeContactTransfer(
        @HeaderParam("sessionId") String sessionId,
        @HeaderParam("Authorization") String authorization,
        @NotNull @Valid TransferExecutionRequest request
    ) {
        validateHeaders(sessionId, authorization);
        return service.executeTransfer(request);
    }

    private void validateHeaders(String sessionId, String authorization) {
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "HB0302", "Se requiere un token de autorizacion valido.");
        }

        if (sessionId == null || !SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            throw new ApiException(400, "HB0305", "La solicitud no cumple con el formato o las restricciones de los campos.");
        }
    }
}
