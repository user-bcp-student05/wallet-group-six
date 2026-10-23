package pe.com.wallet.channel.contact.transfer.v1.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public class TransferVerificationRequest {

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("99999999.99")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
