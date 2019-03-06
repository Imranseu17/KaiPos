package com.kaicomsol.kpos.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LogState {
    public String cardStatus;
    public int historyNo;
    public int newHistoryNo;
    public String state;

    public LogState() {
    }

    public LogState(String cardStatus, int historyNo, int newHistoryNo, String state) {
        this.cardStatus = cardStatus;
        this.historyNo = historyNo;
        this.newHistoryNo = newHistoryNo;
        this.state = state;
    }
}
