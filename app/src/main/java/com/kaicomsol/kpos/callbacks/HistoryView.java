package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.SalesHistory;

public interface HistoryView {

    public void onSuccess(SalesHistory salesHistory);
    public void onError(String error);
    public void onLogout(int code);

}
