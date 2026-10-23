package pe.com.wallet.channel.contact.transfer.v1.exception;

public class ApiException extends RuntimeException {

    private final int status;
    private final String code;
    private final String description;

    public ApiException(int status, String code, String description) {
        super(description);
        this.status = status;
        this.code = code;
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
