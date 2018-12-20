package com.kaicomsol.kpos.nfcfelica;

import java.util.Calendar;

public class ClockTimeModel {
    public Calendar ClockTime = Calendar.getInstance();
    public int ClockTimeFlg = 0;

    public ClockTimeModel() {
        this.ClockTime.clear();
    }
}
