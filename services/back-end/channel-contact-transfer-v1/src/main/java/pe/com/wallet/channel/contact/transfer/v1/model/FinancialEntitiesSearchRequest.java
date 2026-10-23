package pe.com.wallet.channel.contact.transfer.v1.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FinancialEntitiesSearchRequest {

    private static final String BASE64_PATTERN = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";

    @NotBlank
    @Size(min = 4, max = 4096)
    @Pattern(regexp = BASE64_PATTERN)
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
