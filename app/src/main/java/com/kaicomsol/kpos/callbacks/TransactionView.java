package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.TransactionModel;

import java.util.List;

public interface TransactionView {

    public void onSuccess(List<TransactionModel> transactionModelList, int currentPage);
    public void onError(String error);
    public void onLogout(int code);
}
