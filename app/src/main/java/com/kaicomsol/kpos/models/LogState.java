package com.kaicomsol.kpos.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LogState {
    public String cardStatus;
    public String historyNo;
    public int newHistoryNo;
    public String state;

    public LogState() {
    }

    public LogState(String cardStatus, String historyNo, int newHistoryNo, String state) {
        this.cardStatus = cardStatus;
        this.historyNo = historyNo;
        this.newHistoryNo = newHistoryNo;
        this.state = state;
    }
}
