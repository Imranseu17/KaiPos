package com.kaicomsol.kpos.activity;

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaicomsol.kpos.models.Receipt;

/**
 * A basic class representing an entity that is a row in a one-column database table.
 *
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 * @ PrimaryKey - You must identify the primary key.
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 */

@Entity(tableName = "transaction")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "cardIdm")
    public String cardIdm;

    @ColumnInfo(name = "paymentId")
    public int paymentId;

    @ColumnInfo(name = "gasUnit")
    public double gasUnit;

    @ColumnInfo(name = "unitPrice")
    public double unitPrice;

    @ColumnInfo(name = "baseFee")
    public int baseFee;

    @ColumnInfo(name = "emergencyValue")
    public int emergencyValue;

    @ColumnInfo(name = "meterSerialNo")
    public String meterSerialNo;

    @ColumnInfo(name = "historyNo")
    public String historyNo;

    @ColumnInfo(name = "newHistoryNo")
    public int newHistoryNo;

    @ColumnInfo(name = "status")
    public String status;

    public Transaction(String cardIdm, int paymentId, double gasUnit, double unitPrice, int baseFee, int emergencyValue, String meterSerialNo, String historyNo, int newHistoryNo, String status) {
        this.cardIdm = cardIdm;
        this.paymentId = paymentId;
        this.gasUnit = gasUnit;
        this.unitPrice = unitPrice;
        this.baseFee = baseFee;
        this.emergencyValue = emergencyValue;
        this.meterSerialNo = meterSerialNo;
        this.historyNo = historyNo;
        this.newHistoryNo = newHistoryNo;
        this.status = status;
    }

    @NonNull
    public String getCardIdm() {
        return cardIdm;
    }
    @NonNull
    public int getPaymentId() {
        return paymentId;
    }

    public double getGasUnit() {
        return gasUnit;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getBaseFee() {
        return baseFee;
    }

    public int getEmergencyValue() {
        return emergencyValue;
    }

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public String getHistoryNo() {
        return historyNo;
    }

    public int getNewHistoryNo() {
        return newHistoryNo;
    }

    public String getStatus() {
        return status;
    }
}