package com.kaicomsol.kpos.golobal;

import com.squareup.otto.Bus;

public class GlobalBus {

    private static Bus sBus;
    public static Bus getBus() {
        if (sBus == null)
            sBus = new Bus();
        return sBus;
    }

}
