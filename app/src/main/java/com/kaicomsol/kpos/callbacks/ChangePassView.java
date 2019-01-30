package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Login;

public interface ChangePassView {

    public void onSuccess(boolean success);
    public void onError(String error);
    public void onLogout(int code);

}