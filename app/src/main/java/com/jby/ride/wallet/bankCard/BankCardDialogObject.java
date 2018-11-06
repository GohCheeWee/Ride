package com.jby.ride.wallet.bankCard;

public class BankCardDialogObject {
    private String provider;
    private String type;
    private String cardNumber;
    private String id;
    private String status;

    public BankCardDialogObject(String provider, String type, String cardNumber, String id, String status) {
        this.provider = provider;
        this.type = type;
        this.cardNumber = cardNumber;
        this.id = id;
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public String getType() {
        return type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
