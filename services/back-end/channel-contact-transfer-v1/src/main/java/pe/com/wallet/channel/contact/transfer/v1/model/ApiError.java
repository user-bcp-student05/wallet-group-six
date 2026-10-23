package pe.com.wallet.channel.contact.transfer.v1.model;

public class ApiError {

    private String code;
    private String description;

    public ApiError() {
    }

    public ApiError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
