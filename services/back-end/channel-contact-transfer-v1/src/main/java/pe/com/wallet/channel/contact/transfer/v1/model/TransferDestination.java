package pe.com.wallet.channel.contact.transfer.v1.model;

public class TransferDestination {

    private String name;
    private FinancialEntity financialEntity;

    public TransferDestination() {
    }

    public TransferDestination(String name, FinancialEntity financialEntity) {
        this.name = name;
        this.financialEntity = financialEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FinancialEntity getFinancialEntity() {
        return financialEntity;
    }

    public void setFinancialEntity(FinancialEntity financialEntity) {
        this.financialEntity = financialEntity;
    }
}
