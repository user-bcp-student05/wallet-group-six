package pe.com.wallet.channel.contact.transfer.v1.model;

import java.time.OffsetDateTime;

public class TransferExecutionResponse {

    private String operationCode;
    private OffsetDateTime transactionDateTime;

    public TransferExecutionResponse() {
    }

    public TransferExecutionResponse(String operationCode, OffsetDateTime transactionDateTime) {
        this.operationCode = operationCode;
        this.transactionDateTime = transactionDateTime;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public OffsetDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(OffsetDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }
}
