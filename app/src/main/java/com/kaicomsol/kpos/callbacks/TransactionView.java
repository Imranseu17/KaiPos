package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Transaction;

import java.util.List;

public interface TransactionView {

    public void onSuccess(List<Transaction> transactionList);
    public void onError(String error);
}
