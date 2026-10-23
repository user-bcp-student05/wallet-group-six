package pe.com.wallet.channel.contact.transfer.v1.model;

public class FinancialEntity {

    private String code;
    private String entity;

    public FinancialEntity() {
    }

    public FinancialEntity(String code, String entity) {
        this.code = code;
        this.entity = entity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
