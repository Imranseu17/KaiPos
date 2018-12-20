package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Login;

public interface LoginView {

    public void onSuccess(Login login);
    public void onError(String error);
}
