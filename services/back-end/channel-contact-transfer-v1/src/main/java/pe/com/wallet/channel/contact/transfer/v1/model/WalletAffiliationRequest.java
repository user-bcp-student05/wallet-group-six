package pe.com.wallet.channel.contact.transfer.v1.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WalletAffiliationRequest {

    private static final String BASE64_PATTERN = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";

    @NotNull
    private DocumentType documentType;

    @NotBlank
    @Size(min = 4, max = 4096)
    @Pattern(regexp = BASE64_PATTERN)
    private String documentNumber;

    @NotBlank
    @Size(min = 4, max = 4096)
    @Pattern(regexp = BASE64_PATTERN)
    private String phone;

    @NotBlank
    @Size(min = 4, max = 4096)
    @Pattern(regexp = BASE64_PATTERN)
    private String email;

    @NotBlank
    @Size(min = 4, max = 4096)
    @Pattern(regexp = BASE64_PATTERN)
    private String password;

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
