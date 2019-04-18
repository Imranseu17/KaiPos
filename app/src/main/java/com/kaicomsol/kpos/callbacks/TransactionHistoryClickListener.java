package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.TransactionModel;

public interface TransactionHistoryClickListener {

    public void onHistoryClick(TransactionModel transactionModel);
    public void retryPageLoad();


}
