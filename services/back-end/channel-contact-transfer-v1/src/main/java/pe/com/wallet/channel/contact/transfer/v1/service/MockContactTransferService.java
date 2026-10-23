package pe.com.wallet.channel.contact.transfer.v1.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import pe.com.wallet.channel.contact.transfer.v1.exception.ApiException;
import pe.com.wallet.channel.contact.transfer.v1.model.FinancialEntitiesSearchRequest;
import pe.com.wallet.channel.contact.transfer.v1.model.FinancialEntity;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferDestination;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferExecutionRequest;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferExecutionResponse;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferOrigin;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferVerificationRequest;
import pe.com.wallet.channel.contact.transfer.v1.model.TransferVerificationResponse;
import pe.com.wallet.channel.contact.transfer.v1.model.WalletAffiliationRequest;

@ApplicationScoped
public class MockContactTransferService {

    private static final String TRIGGER_422 = "RkFJTF80MjI=";
    private static final String TRIGGER_500 = "RkFJTF81MDA=";

    public void affiliateWallet(WalletAffiliationRequest request) {
        if (TRIGGER_422.equals(request.getDocumentNumber())) {
            throw new ApiException(422, "HB0700", "No fue posible completar la afiliacion a la billetera para el documento informado.");
        }
        if (TRIGGER_500.equals(request.getDocumentNumber())) {
            throw new ApiException(500, "H99999", "Ocurrio un error interno al procesar la solicitud.");
        }
    }

    public List<FinancialEntity> searchFinancialEntities(FinancialEntitiesSearchRequest request) {
        if (TRIGGER_500.equals(request.getPhone())) {
            throw new ApiException(500, "H99999", "Ocurrio un error interno al procesar la solicitud.");
        }
        return List.of(
            new FinancialEntity("01", "Yape"),
            new FinancialEntity("02", "Plin")
        );
    }

    public TransferVerificationResponse verifyTransfer(TransferVerificationRequest request) {
        if (new BigDecimal("422.00").compareTo(request.getAmount()) == 0) {
            throw new ApiException(422, "HB0700", "No fue posible procesar la transferencia solicitada.");
        }
        if (new BigDecimal("500.00").compareTo(request.getAmount()) == 0) {
            throw new ApiException(500, "H99999", "Ocurrio un error interno al procesar la solicitud.");
        }

        return new TransferVerificationResponse(
            new TransferOrigin("M**** P****"),
            new TransferDestination("J**** R****", new FinancialEntity("01", "Yape"))
        );
    }

    public TransferExecutionResponse executeTransfer(TransferExecutionRequest request) {
        String normalized = request.getDescription().toLowerCase();
        if (normalized.contains("fail422")) {
            throw new ApiException(422, "HB0700", "No fue posible procesar la transferencia solicitada.");
        }
        if (normalized.contains("fail500")) {
            throw new ApiException(500, "H99999", "Ocurrio un error interno al procesar la solicitud.");
        }

        return new TransferExecutionResponse(
            "CT202610230001",
            OffsetDateTime.now(ZoneOffset.ofHours(-5)).withNano(0)
        );
    }
}
