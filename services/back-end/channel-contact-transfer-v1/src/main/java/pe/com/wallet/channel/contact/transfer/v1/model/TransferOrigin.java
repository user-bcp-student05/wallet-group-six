package pe.com.wallet.channel.contact.transfer.v1.model;

public class TransferOrigin {

    private String name;

    public TransferOrigin() {
    }

    public TransferOrigin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
