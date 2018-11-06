package com.jby.ride.wallet.transaction;

import java.util.ArrayList;

public class TransactionParentObject {
    private String date;
    private ArrayList<TransactionChildObject> transactionChildObjectArrayList;

    public TransactionParentObject(String date, ArrayList<TransactionChildObject> transactionChildObjectArrayList) {
        this.date = date;
        this.transactionChildObjectArrayList = transactionChildObjectArrayList;
    }

    public TransactionParentObject(ArrayList<TransactionChildObject> transactionChildObjectArrayList) {
        this.transactionChildObjectArrayList = transactionChildObjectArrayList;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<TransactionChildObject> getTransactionChildObjectArrayList() {
        return transactionChildObjectArrayList;
    }
}
