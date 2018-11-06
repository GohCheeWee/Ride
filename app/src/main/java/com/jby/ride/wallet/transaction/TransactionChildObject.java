package com.jby.ride.wallet.transaction;

public class TransactionChildObject {
    String id, amount, title, status;

    public TransactionChildObject(String id, String amount, String title, String status) {
        this.id = id;
        this.amount = amount;
        this.title = title;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }
}
