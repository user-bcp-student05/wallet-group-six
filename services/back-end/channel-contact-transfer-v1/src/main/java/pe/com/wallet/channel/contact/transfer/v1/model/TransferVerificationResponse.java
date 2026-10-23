package pe.com.wallet.channel.contact.transfer.v1.model;

public class TransferVerificationResponse {

    private TransferOrigin origin;
    private TransferDestination destination;

    public TransferVerificationResponse() {
    }

    public TransferVerificationResponse(TransferOrigin origin, TransferDestination destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public TransferOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(TransferOrigin origin) {
        this.origin = origin;
    }

    public TransferDestination getDestination() {
        return destination;
    }

    public void setDestination(TransferDestination destination) {
        this.destination = destination;
    }
}
