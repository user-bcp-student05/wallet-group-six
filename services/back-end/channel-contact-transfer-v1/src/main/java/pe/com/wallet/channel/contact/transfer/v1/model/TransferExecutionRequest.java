package pe.com.wallet.channel.contact.transfer.v1.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TransferExecutionRequest {

    @NotBlank
    @Size(min = 1, max = 140)
    @Pattern(regexp = "^[^\\u0000-\\u001F\\u007F]{1,140}$")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
