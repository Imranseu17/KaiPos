package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.SalesHistory;

public interface HistoryView {

    public void onSuccess(SalesHistory salesHistory, int currentPage);
    public void onError(String error);
    public void onLogout(int code);

}
