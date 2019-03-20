package com.kaicomsol.kpos.models;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcF;
import android.support.annotation.NonNull;
import android.support.v4.internal.view.SupportMenu;

import com.epson.epos2.keyboard.Keyboard;
import com.google.common.base.Ascii;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kaicomsol.kpos.nfcfelica.ClockTimeModel;
import com.kaicomsol.kpos.nfcfelica.Cng2Model;
import com.kaicomsol.kpos.nfcfelica.CngModel;
import com.kaicomsol.kpos.nfcfelica.CntModel;
import com.kaicomsol.kpos.nfcfelica.Common;
import com.kaicomsol.kpos.nfcfelica.ContinueModel;
import com.kaicomsol.kpos.nfcfelica.FelicaBlock;
import com.kaicomsol.kpos.nfcfelica.GMA_CARD_HISTORY;
import com.kaicomsol.kpos.nfcfelica.GMA_ERROR_HISTORY;
import com.kaicomsol.kpos.nfcfelica.GMA_LOG_DATA;
import com.kaicomsol.kpos.nfcfelica.HistoryListData;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.nfcfelica.MaxFlowModel;
import com.kaicomsol.kpos.nfcfelica.OpenCockModel;
import com.kaicomsol.kpos.nfcfelica.ParModel;
import com.kaicomsol.kpos.nfcfelica.SettingData;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.StringUtils;

import org.joda.time.DateTimeConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;


public class AccessFalica {


    byte byCardGroup, byCardStatus;
    byte[] TargetIDm, targetServiceCode;
    public String strCustomerId, cardGroup, cardStatus, versionNO, cardIDm, credit, unit,
            basicFee, refund1, refund2, untreatedFee, openCount, lidTime, indexValue, LidTime;
    public int size, historyNO, errorNO;
    private boolean isChargeCheckFailed = false;
    private static Resources resources = Resources.getSystem();
    public HttpResponsAsync.ReadCardArgument readCardArgument = new HttpResponsAsync.ReadCardArgument();

    public String getCardIDm() {
        return cardIDm;
    }

    public void setCardIDm(String cardIDm) {
        this.cardIDm = cardIDm;
    }

    public ArrayList<byte[]> LogDay = new ArrayList<>();
    public ArrayList<byte[]> LogHour = new ArrayList<>();

    public HttpResponsAsync webAPI = new HttpResponsAsync();
    public ArrayList<HistoryListData> historyListData = new ArrayList();

    String strCardId;

    private enum IsEncryption {
        NotEncrypt,
        Encrypt
    }

    //@Proved@
    public void ReadTag(Tag tag) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            // System 1 System Code -> 0xFE00
            byte[] TargetSystemCode = new byte[]{(byte) 0xfe, (byte) 0x00};

            // create polling command
            byte[] polling = polling(nfc.getSystemCode());

            // get the result by sending a command
            byte[] PollingRes = nfc.transceive(polling);

            // Get the IDm of System 0 (1 byte textdata size, 2 byte response code, the size of the IDm is 8 bytes)
            TargetIDm = Arrays.copyOfRange(PollingRes, 2, 10);

            // the size of the textdata contained in the refund (this time it was 4)
            size = 1;

            // target refund code -> 0x1A8B
            targetServiceCode = new byte[]{(byte) 0, (byte) 9};

            strCardId = GetCardIdm(tag.getId());

            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    //@Proved@
    public String getCardGroup(Tag tag) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();

            BlockDataList datalist = new BlockDataList();
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 2)))[0], 2, true);
            try {
                byCardGroup = GetCardGroup(datalist.GetReadBlockData(2));
                cardGroup = String.format("%02X", new Object[]{Integer.valueOf(Integer.valueOf(GetCardGroup(datalist.GetReadBlockData(2))).intValue() & 255)});
                return cardGroup;

            } catch (Exception e) {
                e.printStackTrace();
            }
            nfc.close();
            return "";
        } catch (Exception e) {
            DebugLog.e(e.getMessage() + "");
            return "";

        }
    }

    //@Proved@
    public int checkCardRecharge(Tag tag) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            BlockDataList datalist = new BlockDataList();
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 2)))[0], 2, true);
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)))[0], 3, true);

            cardGroup = String.format("%02X", new Object[]{Integer.valueOf(Integer.valueOf(GetCardGroup(datalist.GetReadBlockData(2))).intValue() & 255)});
            cardStatus = String.format("%02X", new Object[]{Integer.valueOf(Integer.valueOf(GetCardStatus(datalist.GetReadBlockData(3))).intValue() & 255)});
            if (cardGroup.equals("77") && (cardStatus.equals("06") || cardStatus.equals("30"))) {
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return 200;
            }
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 401;


        } catch (Exception e) {
            e.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    e.printStackTrace();
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (Exception e3) {

                        }
                    }
                }
            }
            return 500;
        }
    }


    //@Proved@
    public boolean checkHistoryStatus(Tag tag, int newHistoryNo, FirebaseDatabase mDatabase) {
        NfcF nfc = NfcF.get(tag);
        try {

            nfc.connect();
            BlockDataList datalist = new BlockDataList();
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)))[0], 5, true);
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)))[0], 3, true);

            int cardHistory = GetCardHistoryNo(datalist.GetReadBlockData(5));
            cardStatus = String.format("%02X", new Object[]{Integer.valueOf(Integer.valueOf(GetCardStatus(datalist.GetReadBlockData(3))).intValue() & 255)});
            if (cardHistory == newHistoryNo && cardStatus.equals("15")) {
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;


        } catch (Exception e) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Exception Check: ");
            stringBuilder2.append(e.getMessage());
            DatabaseReference myRef = mDatabase.getReference("Version-1-1-10-" + timestamp.getTime());
            myRef.setValue(stringBuilder2.toString());

            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    e.printStackTrace();
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (Exception e3) {

                        }
                    }
                }
            }
            return false;
        }
    }

    //@Proved@
    public String getCardStatus(Tag tag) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            BlockDataList datalist = new BlockDataList();
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)))[0], 3, true);

            cardStatus = String.format("%02X", new Object[]{Integer.valueOf(Integer.valueOf(GetCardStatus(datalist.GetReadBlockData(3))).intValue() & 255)});
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return cardStatus;


        } catch (Exception e) {
            e.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    e.printStackTrace();
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (Exception e3) {

                        }
                    }
                }
            }
            return null;
        }
    }

    public boolean getReadCard(Tag tag) {

        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            HttpResponsAsync httpResponsAsync;
            HttpResponsAsync.ReadCardArgumentCardHistory WebApiCardHis;
            strCardId = GetCardIdm(tag.getId());
            String _cardIdm = GetCardIdm(TargetIDm);

            if (strCardId.equals(_cardIdm)) {
                int block, i;
                BlockDataList datalist = new BlockDataList();
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 0)))[0], 0, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 1)))[0], 1, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 2)))[0], 2, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)))[0], 3, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 4)))[0], 4, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)))[0], 5, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 6)))[0], 6, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 7)))[0], 7, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 8)))[0], 8, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 11)))[0], 11, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 12)))[0], 12, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 13)))[0], 13, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 14)))[0], 14, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 15)))[0], 15, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 16)))[0], 16, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 17)))[0], 17, true);
                byte[] req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 18);
                byte[] res = nfc.transceive(req);
                datalist.AddReadBlockData(parse(res)[0], 18, true);

                LogHour.clear();
                for (block = 19; block <= 109; block++) {
                    req = readWithoutEncryption(TargetIDm, size, targetServiceCode, block);
                    res = nfc.transceive(req);
                    LogHour.add(parse(res)[0]);
                }
                LogDay.clear();
                for (block = 110; block <= FelicaBlock.LogDay_Block_End; block++) {
                    req = readWithoutEncryption(TargetIDm, size, targetServiceCode, block);
                    res = nfc.transceive(req);
                    LogDay.add(parse(res)[0]);
                }

                versionNO = String.valueOf(GetVersionNo(datalist.GetReadBlockData(0)));
                cardStatus = String.format("%02X", new Object[]{Integer.valueOf
                        (Integer.valueOf(GetCardStatus(datalist.GetReadBlockData(3))).intValue() & 255)});
                cardIDm = GetCardIdm(TargetIDm);
                strCustomerId = GetCustomerId(datalist.GetReadBlockData(1), datalist.GetReadBlockData(2));
                cardGroup = String.format("%02X", new Object[]{Integer.valueOf
                        (Integer.valueOf(GetCardGroup(datalist.GetReadBlockData(2))).intValue() & 255)});
                credit = String.valueOf(GetCredit(datalist.GetReadBlockData(3)));
                unit = String.valueOf(GetUnit(datalist.GetReadBlockData(3)));
                basicFee = String.valueOf(GetBasicFee(datalist.GetReadBlockData(3)));
                refund1 = String.valueOf(GetRefund1(datalist.GetReadBlockData(4)));
                refund2 = String.valueOf(GetRefund2(datalist.GetReadBlockData(4)));
                untreatedFee = String.valueOf(GetUntreatedFee(datalist.GetReadBlockData(4)));
                readCardArgument.ConfigData.IndexValue = String.valueOf(GetIndexValue(datalist.GetReadBlockData(5)));
                historyNO = Integer.parseInt(String.valueOf(GetCardHistoryNo(datalist.GetReadBlockData(5))));
                errorNO = Integer.parseInt(String.valueOf(GetErrorNo(datalist.GetReadBlockData(5))));
                readCardArgument.ConfigData.LogDays = String.valueOf(GetLogDays(datalist.GetReadBlockData(5)));
                CngModel tempCngModel = GetCng(datalist.GetReadBlockData(5));
                readCardArgument.ConfigData.LogDaysFlg = String.valueOf(tempCngModel.LogDaysFlg);
                readCardArgument.ConfigData.IndexValueFlg = String.valueOf(tempCngModel.IndexValueFlg);
                readCardArgument.ConfigData.WeekControlFlg = String.valueOf(tempCngModel.WeekControlFlg);
                readCardArgument.ConfigData.WeekStartFlg = String.valueOf(tempCngModel.WeekStartFlg);
                readCardArgument.ConfigData.ClockTimeFlg = String.valueOf(tempCngModel.ClockTimeFlg);
                readCardArgument.ConfigData.LogCountFlg = String.valueOf(tempCngModel.LogCountFlg);
                readCardArgument.ConfigData.LogIntervalFlg = String.valueOf(tempCngModel.LogIntervalFlg);
                readCardArgument.ConfigData.OpenCockFlg = String.valueOf(tempCngModel.OpenCockFlg);
                readCardArgument.ConfigData.MaxFlowFlg = String.valueOf(tempCngModel.MaxFlowFlg);
                readCardArgument.ConfigData.ContinueFlg2 = String.valueOf(tempCngModel.ContinueFlg2);
                readCardArgument.ConfigData.ContinueFlg1 = String.valueOf(tempCngModel.ContinueFlg1);
                ContinueModel tempContinueModel = GetContinue1(datalist.GetReadBlockData(6));
                readCardArgument.ConfigData.ContinueValue1 = String.valueOf(tempContinueModel.ContinueValue);
                readCardArgument.ConfigData.ContinueTime1 = String.valueOf(tempContinueModel.ContinueTime);
                readCardArgument.ConfigData.ContinueFlg1 = String.valueOf(tempContinueModel.ContinueFlg);
                readCardArgument.ConfigData.ContinueCon1 = String.valueOf(tempContinueModel.ContinueCon);
                tempContinueModel = GetContinue2(datalist.GetReadBlockData(6));
                readCardArgument.ConfigData.ContinueValue2 = String.valueOf(tempContinueModel.ContinueValue);
                readCardArgument.ConfigData.ContinueTime2 = String.valueOf(tempContinueModel.ContinueTime);
                readCardArgument.ConfigData.ContinueFlg2 = String.valueOf(tempContinueModel.ContinueFlg);
                readCardArgument.ConfigData.ContinueCon2 = String.valueOf(tempContinueModel.ContinueCon);
                MaxFlowModel tempMaxFlowModel = GetMaxFlow(datalist.GetReadBlockData(6));
                readCardArgument.ConfigData.MaxFlowValue = String.valueOf(tempMaxFlowModel.MaxFlowValue);
                readCardArgument.ConfigData.MaxFlowFlg = String.valueOf(tempMaxFlowModel.MaxFlowFlg);
                readCardArgument.ConfigData.MaxFlowCon = String.valueOf(tempMaxFlowModel.MaxFlowCon);
                OpenCockModel tempOpenCockModel = GetOpenCock(datalist.GetReadBlockData(6));
                readCardArgument.ConfigData.OpenCockFlg = String.valueOf(tempOpenCockModel.OpenCockFlg);
                readCardArgument.ConfigData.OpenCockCon = String.valueOf(tempOpenCockModel.OpenCockCon);
                readCardArgument.ConfigData.LogInterval = String.valueOf(GetLogInterval(datalist.GetReadBlockData(6)));
                readCardArgument.ConfigData.LogCount = String.valueOf(GetLogCount(datalist.GetReadBlockData(6)));
                openCount = String.valueOf(GetOpenCount(datalist.GetReadBlockData(7)));
                ClockTimeModel tempClockTimeModel = GetClockTime(datalist.GetReadBlockData(7));
                readCardArgument.ConfigData.ClockTime = GetWebApiDate(tempClockTimeModel.ClockTime);
                readCardArgument.ConfigData.ClockTimeFlg = String.valueOf(tempClockTimeModel.ClockTimeFlg);
                lidTime = GetWebApiDate(GetLidTime(datalist.GetReadBlockData(7)));
                readCardArgument.ConfigData.WeekStart = String.valueOf(GetWeekStart(datalist.GetReadBlockData(7)));
                readCardArgument.ConfigData.WeekControl = String.valueOf(GetWeekControl(datalist.GetReadBlockData(7)));
                Cng2Model tempCng2Model = GetCng2(datalist.GetReadBlockData(8));
                readCardArgument.ConfigData.FlowDetectionFlg = String.valueOf(tempCng2Model.FlowDetectionFlg);
                readCardArgument.ConfigData.QuakeConFlg = String.valueOf(tempCng2Model.QuakeConFlg);
                readCardArgument.ConfigData.ReductionConFlg = String.valueOf(tempCng2Model.ReductionConFlg);
                readCardArgument.ConfigData.OpenCoverConFlg = String.valueOf(tempCng2Model.OpenCoverConFlg);
                readCardArgument.ConfigData.EmergencyValueFlg = String.valueOf(tempCng2Model.EmergencyValueFlg);
                readCardArgument.ConfigData.EmergencyConFlg = String.valueOf(tempCng2Model.EmergencyConFlg);
                ParModel tempParModel = GetPar(datalist.GetReadBlockData(8));
                readCardArgument.ConfigData.QuakeCon = String.valueOf(tempParModel.QuakeCon);
                readCardArgument.ConfigData.OpenCoverCon = String.valueOf(tempParModel.OpenCoverCon);
                readCardArgument.ConfigData.FlowDetection = String.valueOf(tempParModel.FlowDetection);
                readCardArgument.ConfigData.EmergencyCon = String.valueOf(tempParModel.EmergencyCon);
                readCardArgument.ConfigData.ReductionCon = String.valueOf(tempParModel.ReductionCon);
                CntModel tempCntModel = GetCnt(datalist.GetReadBlockData(8));
                readCardArgument.ConfigData.RemoteValveCon = String.valueOf(tempCntModel.RemoteValueCon);
                readCardArgument.ConfigData.SleepModeFlg = String.valueOf(tempCntModel.SleepModeFlg);
                readCardArgument.ConfigData.EmergencyValue = String.valueOf(GetEmergencyValue(datalist.GetReadBlockData(8)));
                GMA_CARD_HISTORY tempCardHis = GetCardHistory1(datalist.GetReadBlockData(11));
                readCardArgument.getClass();
                HttpResponsAsync.ReadCardArgumentCardHistory WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory2(datalist.GetReadBlockData(11));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory3(datalist.GetReadBlockData(12));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory4(datalist.GetReadBlockData(12));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory5(datalist.GetReadBlockData(13));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                GMA_ERROR_HISTORY tempErrorHis = GetErrorHistory1(datalist.GetReadBlockData(14));
                readCardArgument.getClass();
                HttpResponsAsync.ReadCardArgumentErrorHistory WebApiErrorHis = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis.ErrorGroup = String.valueOf(tempErrorHis.ErrorGroup);
                WebApiErrorHis.ErrorTime = GetWebApiDate(tempErrorHis.ErrorTime);
                WebApiErrorHis.ErrorType = String.valueOf(tempErrorHis.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis);
                GMA_ERROR_HISTORY tempErrorHis2 = GetErrorHistory2(datalist.GetReadBlockData(14));
                readCardArgument.getClass();
                HttpResponsAsync.ReadCardArgumentErrorHistory WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory3(datalist.GetReadBlockData(15));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory4(datalist.GetReadBlockData(15));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory5(datalist.GetReadBlockData(16));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory6(datalist.GetReadBlockData(16));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory7(datalist.GetReadBlockData(17));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory8(datalist.GetReadBlockData(17));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory9(datalist.GetReadBlockData(18));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory10(datalist.GetReadBlockData(18));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                GMA_LOG_DATA[] tempLogData = GetLogHour();
                int i2 = 0;
                while (true) {
                    GMA_ERROR_HISTORY tempErrorHis3 = tempErrorHis2;
                    GMA_CARD_HISTORY tempCardHis2 = tempCardHis;
                    i = i2;
                    if (i >= tempLogData.length) {
                        break;
                    }
                    readCardArgument.getClass();
                    HttpResponsAsync.ReadCardArgumentLogHour param = new HttpResponsAsync.ReadCardArgumentLogHour();
                    BlockDataList datalist2 = datalist;
                    param.GasTime = GetWebApiDate(tempLogData[i].GasTime);
                    WebApiCardHis = WebApiCardHis2;
                    param.GasValue = String.valueOf(tempLogData[i].GasValue);
                    readCardArgument.LogHour.add(param);
                    i2 = i + 1;
                    tempErrorHis2 = tempErrorHis3;
                    tempCardHis = tempCardHis2;
                    datalist = datalist2;
                    WebApiCardHis2 = WebApiCardHis;

                }
                WebApiCardHis = WebApiCardHis2;

                GMA_LOG_DATA[] tempLogData2 = GetLogDay();
                i = 0;
                while (i < tempLogData2.length) {
                    readCardArgument.getClass();
                    HttpResponsAsync.ReadCardArgumentLogDay param2 = new HttpResponsAsync.ReadCardArgumentLogDay();
                    param2.GasTime = GetWebApiDate(tempLogData2[i].GasTime);
                    param2.GasValue = String.valueOf(tempLogData2[i].GasValue);
                    readCardArgument.LogDay.add(param2);
                    i++;

                    httpResponsAsync = new HttpResponsAsync();
                }

                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        //LogUtil.i(e.toString());
                    }
                }


                return true;

            }

            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    //LogUtil.i(e2.toString());
                }
            }

            return false;

        } catch (Exception e3) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e22) {
                    //LogUtil.i(e22.toString());
                }
            }
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e222) {
                    //LogUtil.i(e222.toString());
                }
            }

            return false;
        }
    }

    public boolean getInspectCard(Tag tag) {

        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            HttpResponsAsync httpResponsAsync;
            HttpResponsAsync.ReadCardArgumentCardHistory WebApiCardHis;
            strCardId = GetCardIdm(tag.getId());
            String _cardIdm = GetCardIdm(TargetIDm);

            if (strCardId.equals(_cardIdm)) {
                int block, i;
                BlockDataList datalist = new BlockDataList();
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 0)))[0], 0, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 1)))[0], 1, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 2)))[0], 2, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)))[0], 3, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 4)))[0], 4, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)))[0], 5, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 6)))[0], 6, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 7)))[0], 7, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 8)))[0], 8, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 11)))[0], 11, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 12)))[0], 12, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 13)))[0], 13, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 14)))[0], 14, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 15)))[0], 15, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 16)))[0], 16, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 17)))[0], 17, true);
                datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 18)))[0], 18, true);


                versionNO = String.valueOf(GetVersionNo(datalist.GetReadBlockData(0)));
                cardStatus = String.format("%02X", new Object[]{Integer.valueOf
                        (Integer.valueOf(GetCardStatus(datalist.GetReadBlockData(3))).intValue() & 255)});
                cardIDm = GetCardIdm(TargetIDm);
                strCustomerId = GetCustomerId(datalist.GetReadBlockData(1), datalist.GetReadBlockData(2));
                cardGroup = String.format("%02X", new Object[]{Integer.valueOf
                        (Integer.valueOf(GetCardGroup(datalist.GetReadBlockData(2))).intValue() & 255)});
                credit = String.valueOf(GetCredit(datalist.GetReadBlockData(3)));
                unit = String.valueOf(GetUnit(datalist.GetReadBlockData(3)));
                basicFee = String.valueOf(GetBasicFee(datalist.GetReadBlockData(3)));
                refund1 = String.valueOf(GetRefund1(datalist.GetReadBlockData(4)));
                refund2 = String.valueOf(GetRefund2(datalist.GetReadBlockData(4)));
                untreatedFee = String.valueOf(GetUntreatedFee(datalist.GetReadBlockData(4)));
                readCardArgument.ConfigData.IndexValue = String.valueOf(GetIndexValue(datalist.GetReadBlockData(5)));
                historyNO = Integer.parseInt(String.valueOf(GetCardHistoryNo(datalist.GetReadBlockData(5))));
                errorNO = Integer.parseInt(String.valueOf(GetErrorNo(datalist.GetReadBlockData(5))));
                openCount = String.valueOf(GetOpenCount(datalist.GetReadBlockData(7)));
                lidTime = GetWebApiDate(GetLidTime(datalist.GetReadBlockData(7)));
                readCardArgument.ConfigData.EmergencyValue = String.valueOf(GetEmergencyValue(datalist.GetReadBlockData(8)));

                GMA_CARD_HISTORY tempCardHis = GetCardHistory1(datalist.GetReadBlockData(11));
                readCardArgument.getClass();
                HttpResponsAsync.ReadCardArgumentCardHistory WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory2(datalist.GetReadBlockData(11));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory3(datalist.GetReadBlockData(12));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory4(datalist.GetReadBlockData(12));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                tempCardHis = GetCardHistory5(datalist.GetReadBlockData(13));
                readCardArgument.getClass();
                WebApiCardHis2 = new HttpResponsAsync.ReadCardArgumentCardHistory();
                WebApiCardHis2.HistoryTime = GetWebApiDate(tempCardHis.HistoryTime);
                WebApiCardHis2.HistoryType = String.valueOf(tempCardHis.HistoryType);
                readCardArgument.CardHistory.add(WebApiCardHis2);
                GMA_ERROR_HISTORY tempErrorHis = GetErrorHistory1(datalist.GetReadBlockData(14));
                readCardArgument.getClass();
                HttpResponsAsync.ReadCardArgumentErrorHistory WebApiErrorHis = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis.ErrorGroup = String.valueOf(tempErrorHis.ErrorGroup);
                WebApiErrorHis.ErrorTime = GetWebApiDate(tempErrorHis.ErrorTime);
                WebApiErrorHis.ErrorType = String.valueOf(tempErrorHis.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis);
                GMA_ERROR_HISTORY tempErrorHis2 = GetErrorHistory2(datalist.GetReadBlockData(14));
                readCardArgument.getClass();
                HttpResponsAsync.ReadCardArgumentErrorHistory WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory3(datalist.GetReadBlockData(15));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory4(datalist.GetReadBlockData(15));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory5(datalist.GetReadBlockData(16));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory6(datalist.GetReadBlockData(16));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory7(datalist.GetReadBlockData(17));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory8(datalist.GetReadBlockData(17));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory9(datalist.GetReadBlockData(18));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);
                tempErrorHis2 = GetErrorHistory10(datalist.GetReadBlockData(18));
                readCardArgument.getClass();
                WebApiErrorHis2 = new HttpResponsAsync.ReadCardArgumentErrorHistory();
                WebApiErrorHis2.ErrorGroup = String.valueOf(tempErrorHis2.ErrorGroup);
                WebApiErrorHis2.ErrorTime = GetWebApiDate(tempErrorHis2.ErrorTime);
                WebApiErrorHis2.ErrorType = String.valueOf(tempErrorHis2.ErrorType);
                readCardArgument.ErrorHistory.add(WebApiErrorHis2);

                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        //LogUtil.i(e.toString());
                    }
                }


                return true;

            }

            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    //LogUtil.i(e2.toString());
                }
            }

            return false;

        } catch (Exception e3) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e22) {
                    //LogUtil.i(e22.toString());
                }
            }
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e222) {
                    //LogUtil.i(e222.toString());
                }
            }

            return false;
        }
    }

    private byte[] polling(byte[] systemCode) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0x00);            // textdata length byte dummy
        bout.write(0x00);            // command code
        bout.write(systemCode[0]);   // SystemCode
        bout.write(systemCode[1]);   // systemCode
        bout.write(0x01);            // request code
        bout.write(0x0f);            // time slot

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length;  // first byte is the textdata length
        return msg;
    }


    public byte[] readWithoutEncryption(byte[] idm, int size, byte[] serviceCode, int startBlock) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
        bout.write(0);
        bout.write(6);
        bout.write(idm);
        bout.write(1);
        bout.write(serviceCode[1]);
        bout.write(serviceCode[0]);
        bout.write(size);
        for (int i = 0; i < size; i++) {
            bout.write(128);
            bout.write(startBlock + i);
        }
        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length;
        return msg;
    }


    private byte[][] parse(byte[] res) throws Exception {
        // res [10] Error code. 0x00 is normal
        if (res[10] != 0x00)
            throw new RuntimeException("Read Without Encryption Command Error");
        // res [12] Reply block number
        // res [13 + n * 16] Repeat real textdata 16 (byte / block)
        int size = res[12];
        byte[][] data = new byte[size][16];
        String str = "";
        for (int i = 0; i < size; i++) {
            byte[] tmp = new byte[16];
            int offset = 13 + i * 16;
            for (int j = 0; j < 16; j++) {
                tmp[j] = res[offset + j];
            }
            data[i] = tmp;
        }
        return data;
    }

    public class BlockDataList {
        ArrayList<ReadBlockData> dataList = new ArrayList();

        BlockDataList() {
        }

        void AddReadBlockData(byte[] Data, int Block, boolean Overwrite) {
            boolean found = false;
            Iterator it = this.dataList.iterator();
            while (it.hasNext()) {
                ReadBlockData ReadData = (ReadBlockData) it.next();
                if (ReadData.ReadBlock == Block) {
                    if (Overwrite) {
                        ReadData.ReadData = Data;
                    }
                    found = true;
                    if (found) {
                        this.dataList.add(new ReadBlockData(Data, Block));
                        return;
                    }
                    return;
                }
            }
            if (found) {
            }
            this.dataList.add(new ReadBlockData(Data, Block));
        }

        public byte[] GetReadBlockData(int Block) {
            byte[] result = null;
            Iterator it = this.dataList.iterator();
            while (it.hasNext()) {
                ReadBlockData ReadData = (ReadBlockData) it.next();
                if (ReadData.ReadBlock == Block) {
                    result = ReadData.ReadData;
                    break;
                }
            }
            if (result != null) {
                return result;
            }
            ReadBlockData addData = new ReadBlockData(null, Block);
            this.dataList.add(addData);
            return addData.ReadData;
        }
    }

    public class ReadBlockData {
        public int ReadBlock;
        public byte[] ReadData;

        public ReadBlockData(byte[] Data, int Block) {
            this.ReadData = Data;
            this.ReadBlock = Block;
            if (this.ReadData != null) {
                return;
            }
            if (Block >= 11) {
                this.ReadData = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                return;
            }
            byte[] initData = new byte[16];
            new Random().nextBytes(initData);
            this.ReadData = initData;
        }
    }

    public byte GetCardStatus(byte[] getData) {
        int i = 1;
        String cardStatus = GetByteToHexString(getData, IsEncryption.Encrypt, 0, 1);
        int hashCode = cardStatus.hashCode();
        if (hashCode != 1536) {
            if (hashCode != 1572) {
                if (hashCode != 1629) {
                    switch (hashCode) {
                        case 1541:
                            if (cardStatus.equals("05")) {
                                i = 3;
                                break;
                            }
                        case 1542:
                            if (cardStatus.equals("06")) {
                                i = 2;
                                break;
                            }
                    }
                } else if (cardStatus.equals("30")) {
                    i = 0;
                    switch (i) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            break;
                        default:
                            RuntimeException runtimeException = new RuntimeException("InvalidProgramException cardStatus");
                            break;
                    }
                    return (byte) hex2int(cardStatus);
                }
            }
        } else if (cardStatus.equals("00")) {
            i = 4;
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                default:
                    break;
            }
            return (byte) hex2int(cardStatus);
        }
        i = -1;
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                break;
            default:
                break;
        }
        return (byte) hex2int(cardStatus);
    }

    private String GetByteToHexString(byte[] getData, IsEncryption enc, int start, int length) {
        byte[] _data = getData;
        if (_data.length != 16) {
            throw new RuntimeException("Failed to addCard.");
        }
        int end = (start + length) - 1;
        String _val = "";
        for (int i = start; i <= end; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_val);
            stringBuilder.append(PadLeft(Integer.toHexString(Decryption(enc, _data[i]) & 255), 2, '0'));
            _val = stringBuilder.toString();
        }
        return _val.toUpperCase();
    }

    private int hex2int(String s) {
        try {
            return Integer.parseInt(s, 16);
        } catch (Exception e) {
            return 0;
        }
    }

    private String PadLeft(String strOrg, int length, char setStr) {
        String strRet = strOrg;
        StringBuilder sb = new StringBuilder(strRet);
        while (strRet.length() < length) {
            strRet = sb.insert(0, setStr).toString();
        }
        return strRet;
    }

    private byte Decryption(IsEncryption enc, byte input) {
        if (enc == IsEncryption.NotEncrypt) {
            return input;
        }
        return (byte) (255 - ((256 + (input & 255)) - hex2int(strCardId.substring(14, 16))));
    }

    //proved
    public String GetCardIdm(byte[] Idm) {
        StringBuilder sb = new StringBuilder();
        int length = Idm.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(Idm[i])}));
        }
        return sb.toString();
    }

    //proved
    public String getPrepaidCode(Tag tag) {

        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();

            BlockDataList datalist = new BlockDataList();
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 1)))[0], 1, true);
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 2)))[0], 2, true);

            try {
                strCustomerId = GetCustomerId(datalist.GetReadBlockData(1), datalist.GetReadBlockData(2));
                if (nfc != null) {
                    nfc.close();
                }
                return strCustomerId;

            } catch (Exception e) {
                e.printStackTrace();
                if (nfc != null) {
                    nfc.close();
                }
                return "";

            }

        } catch (Exception e) {
            e.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return "";

        }
    }


    public String GetCustomerId(byte[] getData1, byte[] getData2) {
        String result = "";
        byte[] _b = new byte[20];
        if (getData1.length != 16) {
            throw new RuntimeException("GetCustomerId");
        } else if (getData2.length != 16) {
            throw new RuntimeException("GetCustomerId");
        } else {
            int i;
            for (i = 0; i < 16; i++) {
                _b[i] = Decryption(IsEncryption.Encrypt, getData1[i]);
            }
            for (i = 0; i < 4; i++) {
                _b[16 + i] = Decryption(IsEncryption.Encrypt, getData2[i]);
            }
            try {
                result = new String(_b, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                //LogUtil.e(e);
            }
            if (result.charAt(0) == '0') {
//                if (SettingData.WordCount.getSwitch()) {
                if (true) {
                    int resultLength = result.length();
                    try {
//                        int settingWordCount = Integer.parseInt(SettingData.WordCount.getCustomText());
                        int settingWordCount = 16;
                        if (settingWordCount <= 0 || settingWordCount > 20) {
                            throw new Exception();
                        }
                        result = result.substring(resultLength - settingWordCount, resultLength);
                    } catch (Exception e2) {
                        result = result.replaceFirst("^0+", "");
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Exception! at GetCustomerId.\nWordCount : ");
                        stringBuilder.append(SettingData.WordCount.getCustomText());
                        stringBuilder.append("\n");
//                        stringBuilder.append(//LogUtil.Output(e2.getStackTrace()));
                        //LogUtil.i(stringBuilder.toString());
                        return result;
                    }
                }
                result = result.replaceFirst("^0+", "");
            }
            return result;
        }
    }

    private byte GetCardGroup(byte[] getData) {
        int i = 1;
        String _s = GetByteToHexString(getData, IsEncryption.Encrypt, 15, 1);
        int hashCode = _s.hashCode();
        if (hashCode == 1536) {
            if (_s.equals("00")) {
                i = 0;
                switch (i) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                    default:
                        break;
                }
            }
        } else if (hashCode != 1760) {
            if (hashCode == 1792 && _s.equals("88")) {
                i = 2;
                switch (i) {
                    case 0:
                    case 1:
                    case 2:
                        return (byte) hex2int(_s);
                    default:
                        throw new RuntimeException("cardGroup");
                }
            }
        }
        i = -1;
        switch (i) {
            case 0:
            case 1:
            case 2:
                break;
            default:
                break;
        }
        return (byte) hex2int(_s);
    }

    public int GetVersionNo(byte[] getData) {
        return BCDTo(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 1));
    }

    private int BCDTo(String bcd) {
        if (bcd.length() % 4 > 0) {
            throw new RuntimeException("BCDTo Out Of Range");
        }
        String _s = "";
        int _len = bcd.length() / 4;
        for (int i = 0; i < _len; i++) {
            int start = i * 4;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_s);
            stringBuilder.append(String.valueOf(bin2int(bcd.substring(start, start + 4))));
            _s = stringBuilder.toString();
        }
        return Integer.parseInt(_s);
    }

    private int bin2int(String s) {
        try {
            return Integer.parseInt(s, 2);
        } catch (Exception e) {
            return 0;
        }
    }

    private String GetByteToBitString(byte[] getData, IsEncryption enc, int start, int length) {
        byte[] _data = getData;
        if (_data.length != 16) {
            throw new RuntimeException("Failed to addCard.");
        }
        int end = (start + length) - 1;
        String _val = "";
        for (int i = start; i <= end; i++) {
            byte _byte = Decryption(enc, _data[i]);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_val);
            stringBuilder.append(PadLeft(Integer.toBinaryString(_byte & 255), 8, '0'));
            _val = stringBuilder.toString();
        }
        return _val;
    }

    private double GetCredit(byte[] getData) {
        return GetByteToDouble(getData, IsEncryption.Encrypt, 1, 3, 3);
    }

    private double GetByteToDouble(byte[] getData, IsEncryption enc, int start, int inte, int dec) {
        int _len = inte + dec;
        byte[] _data = getData;
        if (_data.length != 16) {
            throw new RuntimeException("Failed to addCard.");
        }
        int end = (start + _len) - 1;
        byte[] _byte = new byte[16];
        for (int i = start; i <= end; i++) {
            _byte[i] = Decryption(enc, _data[i]);
        }
        StringBuilder sb = new StringBuilder(new String(_byte, start, inte + dec, StandardCharsets.US_ASCII));
        sb.insert(inte, ".");
        double[] _cnt = new double[1];
        if (Common.isNumDouble(new String(sb), _cnt)) {
            return _cnt[0];
        }
        throw new RuntimeException("Failed to addCard.");
    }

    private double GetUnit(byte[] getData) {
        return GetByteToDouble(getData, IsEncryption.Encrypt, 7, 3, 2);
    }

    private int GetBasicFee(byte[] getData) {
        return GetByteToInt(getData, IsEncryption.Encrypt, 12, 4);
    }

    private int GetByteToInt(byte[] getData, IsEncryption enc, int start, int length) {
        byte[] _data = getData;
        if (_data.length != 16) {
            throw new RuntimeException("Failed to addCard.");
        }
        int end = (start + length) - 1;
        byte[] _byte = new byte[16];
        for (int i = start; i <= end; i++) {
            _byte[i] = Decryption(enc, _data[i]);
        }
        int[] _cnt = new int[1];
        if (Common.isNumInt(new String(_byte, start, length, StandardCharsets.US_ASCII), _cnt)) {
            return _cnt[0];
        }
        throw new RuntimeException("Failed to addCard.");
    }

    private double GetRefund1(byte[] getData) {
        return GetByteToDouble(getData, IsEncryption.Encrypt, 0, 3, 3);
    }


    private double GetRefund2(byte[] getData) {
        return GetByteToDouble(getData, IsEncryption.Encrypt, 6, 3, 3);
    }

    private int GetUntreatedFee(byte[] getData) {
        return GetByteToInt(getData, IsEncryption.Encrypt, 7, 4);
    }


    public boolean GasChargeCard(Tag tag, double Credit, double Unit, int BasicFee, double emergencyValue, String meter, int CardHistoryNo) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            BlockDataList datalist = new BlockDataList();

            //Get Card Group
            byte[][] groupData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 2)));
            CheckDataLength(groupData);
            //Add Card Group In List
            datalist.AddReadBlockData(groupData[0], 2, false);
            byte _cardGroup = GetCardGroup(groupData[0]);
            //Get Card Status
            byte[][] statusData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)));
            CheckDataLength(statusData);
            //Add status data in blog
            datalist.AddReadBlockData(statusData[0], 3, false);
            byte _cardStatus = GetCardStatus(statusData[0]);
            if (isGasChargeCard(_cardStatus, _cardGroup)) {

                SetCredit(datalist.GetReadBlockData(3), Credit);
                SetUnit(datalist.GetReadBlockData(3), Unit);
                SetBasicFee(datalist.GetReadBlockData(3), BasicFee);

                CngModel tempCngModel = new CngModel();
                tempCngModel.ContinueFlg1 = 1;
                tempCngModel.ContinueFlg2 = 1;
                //Get Config Data
                byte[][] historyData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)));
                CheckDataLength(statusData);
                datalist.AddReadBlockData(historyData[0], 5, false);
                SetCng(datalist.GetReadBlockData(5), tempCngModel);
                //Get Config Data One
                ContinueModel tempContinueModel = new ContinueModel();
                tempContinueModel.ContinueTime = 10;
                tempContinueModel.ContinueValue = 10;
                tempContinueModel.ContinueCon = 1;
                tempContinueModel.ContinueFlg = 1;
                //Get Config Data Two
                byte[][] configData2 = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 6)));
                CheckDataLength(configData2);
                datalist.AddReadBlockData(configData2[0], 6, false);
                SetContinue1(datalist.GetReadBlockData(6), tempContinueModel);
                //Get Config Data One
                ContinueModel tempContinueModel2 = new ContinueModel();
                tempContinueModel2.ContinueTime = 24;
                tempContinueModel2.ContinueValue = 5;
                tempContinueModel2.ContinueCon = 2;
                tempContinueModel2.ContinueFlg = 1;
                SetContinue2(datalist.GetReadBlockData(6), tempContinueModel2);
                Cng2Model tempCng2Model = new Cng2Model();
                tempCng2Model.QuakeConFlg = 1;
                tempCng2Model.EmergencyConFlg = 1;
                tempCng2Model.EmergencyValueFlg = 1;
                byte[][] configData3 = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 8)));
                CheckDataLength(configData3);
                datalist.AddReadBlockData(configData3[0], 8, false);
                SetCng2(datalist.GetReadBlockData(8), tempCng2Model);
                ParModel tempParModel = new ParModel();
                tempParModel.QuakeCon = 2;
                tempParModel.EmergencyCon = 1;
                SetPar(datalist.GetReadBlockData(8), tempParModel);
                SetEmergencyValue(datalist.GetReadBlockData(8), emergencyValue);

                SetCardHistoryNo(datalist.GetReadBlockData(5), CardHistoryNo);
                SetCardStatus(datalist.GetReadBlockData(3), 21);
                //Finally write
                writeWithoutEncryption(nfc, datalist);

                if (checkWriteData(nfc, datalist, Credit, CardHistoryNo)) {
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                return false;
            }
            return false;
        } catch (Exception e3) {
            e3.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    e2.printStackTrace();
                }
            }
            return false;
        }
    }

    public boolean creditChargeCard(Tag tag, double Credit, double Unit, int BasicFee, double emergencyValue, String meter) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            BlockDataList datalist = new BlockDataList();

            //Get Card Group
            byte[][] groupData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 2)));
            CheckDataLength(groupData);
            //Add Card Group In List
            datalist.AddReadBlockData(groupData[0], 2, false);
            byte _cardGroup = GetCardGroup(groupData[0]);
            //Get Card Status
            byte[][] statusData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)));
            CheckDataLength(statusData);
            //Add status data in blog
            datalist.AddReadBlockData(statusData[0], 3, false);
            byte _cardStatus = GetCardStatus(statusData[0]);
            if (isGasChargeCard(_cardStatus, _cardGroup)) {

                SetCredit(datalist.GetReadBlockData(3), Credit);
                SetUnit(datalist.GetReadBlockData(3), Unit);
                SetBasicFee(datalist.GetReadBlockData(3), BasicFee);

                CngModel tempCngModel = new CngModel();
                tempCngModel.ContinueFlg1 = 1;
                tempCngModel.ContinueFlg2 = 1;
                //Get Config Data
                byte[][] historyData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)));
                CheckDataLength(statusData);
                datalist.AddReadBlockData(historyData[0], 5, false);
                SetCng(datalist.GetReadBlockData(5), tempCngModel);
                //Get Config Data One
                ContinueModel tempContinueModel = new ContinueModel();
                tempContinueModel.ContinueTime = 10;
                tempContinueModel.ContinueValue = 10;
                tempContinueModel.ContinueCon = 1;
                tempContinueModel.ContinueFlg = 1;
                //Get Config Data Two
                byte[][] configData2 = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 6)));
                CheckDataLength(configData2);
                datalist.AddReadBlockData(configData2[0], 6, false);
                SetContinue1(datalist.GetReadBlockData(6), tempContinueModel);
                //Get Config Data One
                ContinueModel tempContinueModel2 = new ContinueModel();
                tempContinueModel2.ContinueTime = 24;
                tempContinueModel2.ContinueValue = 5;
                tempContinueModel2.ContinueCon = 2;
                tempContinueModel2.ContinueFlg = 1;
                SetContinue2(datalist.GetReadBlockData(6), tempContinueModel2);
                Cng2Model tempCng2Model = new Cng2Model();
                tempCng2Model.QuakeConFlg = 1;
                tempCng2Model.EmergencyConFlg = 1;
                tempCng2Model.EmergencyValueFlg = 1;
                byte[][] configData3 = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 8)));
                CheckDataLength(configData3);
                datalist.AddReadBlockData(configData3[0], 8, false);
                SetCng2(datalist.GetReadBlockData(8), tempCng2Model);
                ParModel tempParModel = new ParModel();
                tempParModel.QuakeCon = 2;
                tempParModel.EmergencyCon = 1;
                SetPar(datalist.GetReadBlockData(8), tempParModel);
                SetEmergencyValue(datalist.GetReadBlockData(8), emergencyValue);

                //Finally write
                writeWithoutEncryption(nfc, datalist);
                double _credit = GetCredit(datalist.GetReadBlockData(3));
                if (_credit == Credit) {
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                return false;
            }
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            return false;
        } catch (Exception e3) {
            e3.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2) {
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    e2.printStackTrace();
                }
            }
            return false;
        }
    }

    private void CheckDataLength(byte[][] data) {
        if (this.size != data.length) {
            RuntimeException runtimeException = new RuntimeException("CheckDataLength");
        }
    }

    private boolean isGasChargeCard(byte CardStatus, byte CardGroup) {
        if (CardGroup != Keyboard.VK_F8) {
            return false;
        }
        if (CardStatus == (byte) 6 || CardStatus == Keyboard.VK_0) {
            return true;
        }
        return false;
    }


    private boolean checkWriteData(NfcF nfc, BlockDataList dataList, double credit, int historyNo) {
        try {
            //Get Card Status
            byte[][] statusData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)));
            CheckDataLength(statusData);
            byte _cardStatus = GetCardStatus(statusData[0]);

            //Get Card History
            byte[][] historyData = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 5)));
            CheckDataLength(historyData);
            int _cardHistory = GetCardHistoryNo(historyData[0]);
            double _credit = GetCredit(dataList.GetReadBlockData(3));

            if (_credit == credit && _cardStatus == 21 && _cardHistory == historyNo) {
                return true;
            } else if (_credit != credit && _cardStatus == 21 && _cardHistory == historyNo) {
                adjustStatusHistory(nfc, dataList, historyNo - 1);
                DebugLog.e("History & Status Adjust");
            } else if (_credit != credit && _cardStatus != 21 && _cardHistory == historyNo) {
                adjustHistoryNo(nfc, dataList, historyNo - 1);
                DebugLog.e("History Adjust");
                return false;
            } else if (_credit != credit && _cardStatus == 21 && _cardHistory != historyNo) {
                adjustStatus(nfc, dataList);
                DebugLog.e("Status Adjust");
                return false;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void adjustHistoryNo(NfcF nfc, BlockDataList dataList, int historyNo) {
        SetCardHistoryNo(dataList.GetReadBlockData(5), historyNo);
        try {
            writeWithoutEncryption(nfc, dataList);
        } catch (IOException e) {
            e.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void adjustStatusHistory(NfcF nfc, BlockDataList dataList, int historyNo) {

        SetCardHistoryNo(dataList.GetReadBlockData(5), historyNo);
        SetCardStatus(dataList.GetReadBlockData(3), 06);
        try {
            writeWithoutEncryption(nfc, dataList);
        } catch (IOException e) {
            e.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void adjustStatus(NfcF nfc, BlockDataList dataList) {
        //Status 21 == 15, 06 == 06, 48 == 30, Group 119 == 77, 136 == 88
        SetCardStatus(dataList.GetReadBlockData(3), 06);
        try {
            writeWithoutEncryption(nfc, dataList);
        } catch (IOException e) {
            e.printStackTrace();
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void SetCredit(byte[] setData, double credit) {
        if (ValidDouble(credit, 3, 3)) {
            SetDoubleToByte(setData, IsEncryption.Encrypt, credit, 1, 3, 3);
            return;
        }
        throw new RuntimeException("Credit Out Of Range");
    }

    private void SetRefund1(byte[] setData, double credit) {
        if (ValidDouble(credit, 3, 3)) {
            SetDoubleToByte(setData, IsEncryption.Encrypt, credit, 0, 3, 3);
            return;
        }
        throw new RuntimeException("Credit Out Of Range");
    }


    private void SetRefund2(byte[] setData, double credit) {
        if (ValidDouble(credit, 3, 3)) {
            SetDoubleToByte(setData, IsEncryption.Encrypt, credit, 6, 3, 3);
            return;
        }
        throw new RuntimeException("Credit Out Of Range");
    }


    private boolean ValidDouble(double num, int inte, int dec) {
        if (num < 0.0d) {
            return false;
        }
        if (num == 0.0d) {
            return true;
        }
        String[] _s = String.valueOf(num).split("\\.");
        if (_s[0].length() > inte) {
            return false;
        }
        if (_s.length <= 1 || _s[1].length() <= dec) {
            return true;
        }
        return false;
    }

    private void SetDoubleToByte(byte[] setData, IsEncryption enc, double val, int start, int inte, int dec) {
        int i;
        StringBuilder stringBuilder;
        int _len = inte + dec;
        int i2 = 0;
        String strFormat = "{0,number,";
        for (i = 0; i < inte; i++) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(strFormat);
            stringBuilder.append("0");
            strFormat = stringBuilder.toString();
        }
        StringBuilder strFormat2 = new StringBuilder();
        strFormat2.append(strFormat);
        strFormat2.append(".");
        strFormat = strFormat2.toString();
        for (i = 0; i < dec; i++) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(strFormat);
            stringBuilder.append("0");
            strFormat = stringBuilder.toString();
        }
        strFormat2 = new StringBuilder();
        strFormat2.append(strFormat);
        strFormat2.append("}");
        strFormat = MessageFormat.format(strFormat2.toString(), new Object[]{Double.valueOf(val)});
        if (strFormat.length() != _len + 1) {
            throw new RuntimeException("Failed to Write.");
        }
        byte[] _b = strFormat.replace(".", "").getBytes(StandardCharsets.US_ASCII);
        while (i2 < _len) {
            setData[start + i2] = Encryption(enc, _b[i2]);
            i2++;
        }
    }

    private byte Encryption(IsEncryption enc, byte input) {
        if (enc == IsEncryption.NotEncrypt) {
            return input;
        }
        int _y = hex2int(this.strCardId.substring(14, 16)) + (255 - input);
        String _z = Integer.toHexString(_y);
        if (_z.length() > 2) {
            _y = hex2int(_z.substring(_z.length() - 2, _z.length()));
        }
        return (byte) _y;
    }

    private void SetUnit(byte[] setData, double unit) {
        if (ValidDouble(unit, 3, 2)) {
            SetDoubleToByte(setData, IsEncryption.Encrypt, unit, 7, 3, 2);
            return;
        }
        throw new RuntimeException("Unit Out Of Range");
    }

    private void SetBasicFee(byte[] setData, int basicFee) {
        if (ValidInt(basicFee, 4)) {
            SetIntToByte(setData, IsEncryption.Encrypt, basicFee, 12, 4);
            return;
        }
        throw new RuntimeException("BasicFee Out Of Range");
    }

    private boolean ValidInt(int num, int len) {
        if (num < 0) {
            return false;
        }
        if (num != 0 && String.valueOf(num).length() > len) {
            return false;
        }
        return true;
    }

    private void SetIntToByte(byte[] setData, IsEncryption enc, int val, int start, int length) {
        int i = 0;
        String strFormat = "{0,number,";
        for (int i2 = 0; i2 < length; i2++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(strFormat);
            stringBuilder.append("0");
            strFormat = stringBuilder.toString();
        }
        StringBuilder strFormat2 = new StringBuilder();
        strFormat2.append(strFormat);
        strFormat2.append("}");
        strFormat = MessageFormat.format(strFormat2.toString(), new Object[]{Integer.valueOf(val)});
        if (strFormat.length() != length) {
            throw new RuntimeException("Failed to Write.");
        }
        byte[] _b = new byte[0];
        try {
            _b = strFormat.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            //LogUtil.e(e);
        }
        while (i < length) {
            setData[start + i] = Encryption(enc, _b[i]);
            i++;
        }
    }

    private void SetCng(byte[] setData, CngModel cng) {
        if (cng == null) {
            throw new RuntimeException("Cng");
        }
        String _s = "";
        switch (cng.LogDaysFlg) {
            case 0:
            case 1:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(cng.LogDaysFlg));
                _s = stringBuilder.toString();
                switch (cng.IndexValueFlg) {
                    case 0:
                    case 1:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(String.valueOf(cng.IndexValueFlg));
                        _s = stringBuilder.toString();
                        switch (cng.WeekControlFlg) {
                            case 0:
                            case 1:
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(_s);
                                stringBuilder.append(String.valueOf(cng.WeekControlFlg));
                                _s = stringBuilder.toString();
                                switch (cng.WeekStartFlg) {
                                    case 0:
                                    case 1:
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(_s);
                                        stringBuilder.append(String.valueOf(cng.WeekStartFlg));
                                        _s = stringBuilder.toString();
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(_s);
                                        stringBuilder.append("0");
                                        _s = stringBuilder.toString();
                                        switch (cng.ClockTimeFlg) {
                                            case 0:
                                            case 1:
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(_s);
                                                stringBuilder.append(String.valueOf(cng.ClockTimeFlg));
                                                _s = stringBuilder.toString();
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(_s);
                                                stringBuilder.append("0");
                                                _s = stringBuilder.toString();
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(_s);
                                                stringBuilder.append("0");
                                                _s = stringBuilder.toString();
                                                switch (cng.LogCountFlg) {
                                                    case 0:
                                                    case 1:
                                                        stringBuilder = new StringBuilder();
                                                        stringBuilder.append(_s);
                                                        stringBuilder.append(String.valueOf(cng.LogCountFlg));
                                                        _s = stringBuilder.toString();
                                                        switch (cng.LogIntervalFlg) {
                                                            case 0:
                                                            case 1:
                                                                stringBuilder = new StringBuilder();
                                                                stringBuilder.append(_s);
                                                                stringBuilder.append(String.valueOf(cng.LogIntervalFlg));
                                                                _s = stringBuilder.toString();
                                                                switch (cng.OpenCockFlg) {
                                                                    case 0:
                                                                    case 1:
                                                                        stringBuilder = new StringBuilder();
                                                                        stringBuilder.append(_s);
                                                                        stringBuilder.append(String.valueOf(cng.OpenCockFlg));
                                                                        _s = stringBuilder.toString();
                                                                        switch (cng.MaxFlowFlg) {
                                                                            case 0:
                                                                            case 1:
                                                                                stringBuilder = new StringBuilder();
                                                                                stringBuilder.append(_s);
                                                                                stringBuilder.append(String.valueOf(cng.MaxFlowFlg));
                                                                                _s = stringBuilder.toString();
                                                                                switch (cng.ContinueFlg2) {
                                                                                    case 0:
                                                                                    case 1:
                                                                                        stringBuilder = new StringBuilder();
                                                                                        stringBuilder.append(_s);
                                                                                        stringBuilder.append(String.valueOf(cng.ContinueFlg2));
                                                                                        _s = stringBuilder.toString();
                                                                                        switch (cng.ContinueFlg1) {
                                                                                            case 0:
                                                                                            case 1:
                                                                                                stringBuilder = new StringBuilder();
                                                                                                stringBuilder.append(_s);
                                                                                                stringBuilder.append(String.valueOf(cng.ContinueFlg1));
                                                                                                _s = stringBuilder.toString();
                                                                                                stringBuilder = new StringBuilder();
                                                                                                stringBuilder.append(_s);
                                                                                                stringBuilder.append("0");
                                                                                                _s = stringBuilder.toString();
                                                                                                stringBuilder = new StringBuilder();
                                                                                                stringBuilder.append(_s);
                                                                                                stringBuilder.append("0");
                                                                                                byte[] bArr = setData;
                                                                                                SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 14, 2);
                                                                                                return;
                                                                                            default:
                                                                                                throw new RuntimeException("ContinueFlg1");
                                                                                        }
                                                                                    default:
                                                                                        throw new RuntimeException("ContinueFlg2");
                                                                                }
                                                                            default:
                                                                                throw new RuntimeException("MaxFlowFlg");
                                                                        }
                                                                    default:
                                                                        throw new RuntimeException("OpenCockFlg");
                                                                }
                                                            default:
                                                                throw new RuntimeException("LogIntervalFlg");
                                                        }
                                                    default:
                                                        throw new RuntimeException("LogCountFlg");
                                                }
                                            default:
                                                throw new RuntimeException("ClockTimeFlg");
                                        }
                                    default:
                                        throw new RuntimeException("WeekStartFlg");
                                }
                            default:
                                throw new RuntimeException("WeekControlFlg");
                        }
                    default:
                        throw new RuntimeException("IndexValueFlg");
                }
            default:
                throw new RuntimeException("LogDaysFlg");
        }
    }

    private void SetContinue1(byte[] setData, ContinueModel continue1) {
        String _s = "";
        if (continue1.ContinueValue < 0 || 255 < continue1.ContinueValue) {
            throw new RuntimeException("ContinueValue1");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append(PadLeft(Integer.toBinaryString(continue1.ContinueValue), 8, '0'));
        _s = stringBuilder.toString();
        if (continue1.ContinueTime < 0 || 255 < continue1.ContinueTime) {
            throw new RuntimeException("ContinueTime1");
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append(PadLeft(Integer.toBinaryString(continue1.ContinueTime), 8, '0'));
        _s = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append("00000");
        _s = stringBuilder.toString();
        switch (continue1.ContinueFlg) {
            case 0:
            case 1:
                stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(continue1.ContinueFlg));
                _s = stringBuilder.toString();
                switch (continue1.ContinueCon) {
                    case 0:
                    case 1:
                    case 2:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(PadLeft(Integer.toBinaryString(continue1.ContinueCon), 2, '0'));
                        byte[] bArr = setData;
                        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 4, 3);
                        return;
                    default:
                        throw new RuntimeException("ContinueCon1");
                }
            default:
                throw new RuntimeException("ContinueFlg1");
        }
    }

    private void SetBitStringToByte(byte[] setData, IsEncryption enc, String val, int start, int length) {
        if (val.length() != 8 * length) {
            val = PadLeft(val, 8 * length, '0');
        }
        int j = 0;
        for (int i = 0; i < length; i++) {
            setData[start + i] = Encryption(enc, (byte) bin2int(val.substring(j, j + 8)));
            j += 8;
        }
    }

    private void SetContinue2(byte[] setData, ContinueModel continue2) {
        String _s = "";
        if (continue2.ContinueValue < 0 || 255 < continue2.ContinueValue) {
            throw new RuntimeException("ContinueValue2");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append(PadLeft(Integer.toBinaryString(continue2.ContinueValue), 8, '0'));
        _s = stringBuilder.toString();
        if (continue2.ContinueTime < 0 || 255 < continue2.ContinueTime) {
            throw new RuntimeException("ContinueTime2");
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append(PadLeft(Integer.toBinaryString(continue2.ContinueTime), 8, '0'));
        _s = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append("00000");
        _s = stringBuilder.toString();
        switch (continue2.ContinueFlg) {
            case 0:
            case 1:
                stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(continue2.ContinueFlg));
                _s = stringBuilder.toString();
                switch (continue2.ContinueCon) {
                    case 0:
                    case 1:
                    case 2:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(PadLeft(Integer.toBinaryString(continue2.ContinueCon), 2, '0'));
                        byte[] bArr = setData;
                        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 7, 3);
                        return;
                    default:
                        throw new RuntimeException("ContinueCon2");
                }
            default:
                throw new RuntimeException("ContinueFlg2");
        }
    }

    private void SetCng2(byte[] setData, Cng2Model cng2) {
        if (cng2 == null) {
            throw new RuntimeException("Cng2");
        }
        String _s = "";
        switch (cng2.FlowDetectionFlg) {
            case 0:
            case 1:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(cng2.FlowDetectionFlg));
                _s = stringBuilder.toString();
                switch (cng2.QuakeConFlg) {
                    case 0:
                    case 1:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(String.valueOf(cng2.QuakeConFlg));
                        _s = stringBuilder.toString();
                        switch (cng2.ReductionConFlg) {
                            case 0:
                            case 1:
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(_s);
                                stringBuilder.append(String.valueOf(cng2.ReductionConFlg));
                                _s = stringBuilder.toString();
                                switch (cng2.OpenCoverConFlg) {
                                    case 0:
                                    case 1:
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(_s);
                                        stringBuilder.append(String.valueOf(cng2.OpenCoverConFlg));
                                        _s = stringBuilder.toString();
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(_s);
                                        stringBuilder.append("0");
                                        _s = stringBuilder.toString();
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(_s);
                                        stringBuilder.append("0");
                                        _s = stringBuilder.toString();
                                        switch (cng2.EmergencyValueFlg) {
                                            case 0:
                                            case 1:
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(_s);
                                                stringBuilder.append(String.valueOf(cng2.EmergencyValueFlg));
                                                _s = stringBuilder.toString();
                                                switch (cng2.EmergencyConFlg) {
                                                    case 0:
                                                    case 1:
                                                        stringBuilder = new StringBuilder();
                                                        stringBuilder.append(_s);
                                                        stringBuilder.append(String.valueOf(cng2.EmergencyConFlg));
                                                        byte[] bArr = setData;
                                                        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 0, 1);
                                                        return;
                                                    default:
                                                        throw new RuntimeException("EmergencyConFlg");
                                                }
                                            default:
                                                throw new RuntimeException("EmergencyValueFlg");
                                        }
                                    default:
                                        throw new RuntimeException("OpenCoverConFlg");
                                }
                            default:
                                throw new RuntimeException("ReductionConFlg");
                        }
                    default:
                        throw new RuntimeException("QuakeConFlg");
                }
            default:
                throw new RuntimeException("FlowDetectionFlg");
        }
    }

    private void SetPar(byte[] setData, ParModel par) {
        String _s = "";
        switch (par.QuakeCon) {
            case 0:
            case 1:
            case 2:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(PadLeft(Integer.toBinaryString(par.QuakeCon), 2, '0'));
                _s = stringBuilder.toString();
                switch (par.OpenCoverCon) {
                    case 0:
                    case 1:
                    case 2:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(PadLeft(Integer.toBinaryString(par.OpenCoverCon), 2, '0'));
                        _s = stringBuilder.toString();
                        switch (par.FlowDetection) {
                            case 0:
                            case 1:
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(_s);
                                stringBuilder.append(String.valueOf(par.FlowDetection));
                                _s = stringBuilder.toString();
                                switch (par.EmergencyCon) {
                                    case 0:
                                    case 1:
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append(_s);
                                        stringBuilder.append(String.valueOf(par.EmergencyCon));
                                        _s = stringBuilder.toString();
                                        switch (par.ReductionCon) {
                                            case 0:
                                            case 1:
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(_s);
                                                stringBuilder.append(String.valueOf(par.ReductionCon));
                                                _s = stringBuilder.toString();
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(_s);
                                                stringBuilder.append("0");
                                                byte[] bArr = setData;
                                                SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 1, 1);
                                                return;
                                            default:
                                                throw new RuntimeException("ReductionCon");
                                        }
                                    default:
                                        throw new RuntimeException("EmergencyCon");
                                }
                            default:
                                throw new RuntimeException("FlowDetection");
                        }
                    default:
                        throw new RuntimeException("OpenCoverCon");
                }
            default:
                throw new RuntimeException("QuakeCon");
        }
    }

    private void SetEmergencyValue(byte[] setData, double emergencyValue) {
        if (ValidDouble(emergencyValue, 3, 3)) {
            byte[] bArr = setData;
            SetBitStringToByte(bArr, IsEncryption.Encrypt, ToBCD(Long.valueOf(Math.round(1000.0d * emergencyValue)), 6), 4, 3);
            return;
        }
        throw new RuntimeException("EmergencyValue");
    }

    private void writeWithoutEncryption(NfcF nfc, BlockDataList writeData) throws IOException {
        int j = 0;
        Iterator it = writeData.dataList.iterator();
        while (it.hasNext()) {
            ReadBlockData Data = (ReadBlockData) it.next();
            try {
                CheckWriteResult(nfc.transceive(writeWithoutEncryption(TargetIDm, targetServiceCode, Data.ReadBlock, Data.ReadData)));
                j++;
            } catch (Exception e) {
                throw e;
            }
        }
    }


    private void CheckWriteResult(byte[] res) {
        if (res[10] != (byte) 0) {
            RuntimeException runtimeException = new RuntimeException("Write Without Encryption Command Error");
        }
    }

    private boolean CheckWroteData(@NonNull BlockDataList dataList, @NonNull NfcF nfc, @NonNull CngModel tempCngModel, @NonNull ContinueModel tempContinueModel, @NonNull ContinueModel tempContinueModel2, @NonNull Cng2Model tempCng2Model, @NonNull ParModel tempParModel, double emergencyValue) {
        Exception e;

        BlockDataList type = dataList;
        CngModel cngModel = tempCngModel;
        ContinueModel continueModel = tempContinueModel;
        ContinueModel continueModel2 = tempContinueModel2;
        Cng2Model cng2Model = tempCng2Model;
        ParModel parModel = tempParModel;
        double d = emergencyValue;
        String type2 = "";
        boolean flag = false;
        int i = 0;
        while (i < type.dataList.size()) {
            String type3;
            try {
                type3 = type2;
                StringBuilder stringBuilder;
                try {
                    byte[] res = nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, ((ReadBlockData) type.dataList.get(i)).ReadBlock));
                    byte[][] data2 = parse(res);
                    CheckDataLength(data2);
                    String type4;
                    boolean flag2;
                    switch (i) {
                        case 0:
                            break;
                        case 1:
                            flag = true;
                            break;
                        case 2:
                            if (!GetCng(data2[0]).Equals(cngModel)) {
                                flag = false;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(tempCngModel.getClass().toString());
                                stringBuilder.append(" : ");
                                stringBuilder.append(cngModel);
                                type4 = stringBuilder.toString();
                                break;
                            }
                        case 3:
                            if (!GetContinue1(data2[0]).Equals(continueModel)) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(tempContinueModel.getClass().toString());
                                stringBuilder.append(" : ");
                                stringBuilder.append(continueModel);
                                flag = false;
                                type3 = stringBuilder.toString();
                            }
                            if (!GetContinue2(data2[0]).Equals(continueModel2)) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(tempContinueModel2.getClass().toString());
                                stringBuilder.append(" : ");
                                stringBuilder.append(continueModel2);
                                type4 = stringBuilder.toString();
                                flag2 = false;
                                break;
                            }
                            type4 = type3;
                            break;
                        case 4:
                            if (!GetCng2(data2[0]).Equals(cng2Model)) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(tempCng2Model.getClass().toString());
                                stringBuilder.append(" : ");
                                stringBuilder.append(cng2Model);
                                flag = false;
                                type3 = stringBuilder.toString();
                            }
                            if (!GetPar(data2[0]).Equals(parModel)) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(tempParModel.getClass().toString());
                                stringBuilder.append(" : ");
                                stringBuilder.append(parModel);
                                type3 = stringBuilder.toString();
                                flag = false;
                            }
                            if (GetEmergencyValue(data2[0]) != d) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("emergencyValue : ");
                                stringBuilder.append(d);
                                type4 = stringBuilder.toString();
                                flag2 = false;
                                break;
                            }
                            type4 = type3;
                            break;
                        default:
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("default : ");
                            stringBuilder.append(i);
                            type4 = stringBuilder.toString();
                            flag2 = false;
                            break;
                    }
                    i++;
                } catch (Exception e2) {
                    e = e2;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Exception at CheckWroteData!\nStackTrace : ");
//                    stringBuilder.append(//LogUtil.Output(e.getStackTrace()));
                    //LogUtil.i(stringBuilder.toString());
                    return false;
                }
            } catch (Exception e22) {
                type3 = type2;
                e = e22;
            }
        }
        return true;
    }

    private static void ReturnLocale() {
        try {
            Locale.setDefault(Common.getLocal(SettingData.SettingLang));
        } catch (Exception e) {
//            LogUtil.i("Occured in ReturnLocale()");
        }
    }

    private <T> String ToBCD(T num, int len) {
        long _val = Long.valueOf(num.toString()).longValue();
        String _str = "";
        for (int i = 1; i <= len; i++) {
            long _mod = _val % 10;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(PadLeft(Long.toBinaryString(_mod), 4, '0'));
            stringBuilder.append(_str);
            _str = stringBuilder.toString();
            _val = (_val - _mod) / 10;
        }
        return _str;
    }

    private String byteArraytoHexString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();
        for (byte b : byteArray) {
            sb.append(Integer.toHexString(255 & b));
        }
        return sb.toString();
    }

    private byte[] writeWithoutEncryption(byte[] idm, byte[] serviceCode, int blockNumber, byte[] data) throws TagLostException, IOException {
        if (idm == null || idm.length == 0) {
            throw new IllegalArgumentException();
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
        bout.write(0);
        bout.write(8);
        bout.write(idm);
        bout.write(1);
        bout.write(serviceCode[1]);
        bout.write(serviceCode[0]);
        bout.write(1);
        for (int i = 0; i < 1; i++) {
            bout.write(128);
            bout.write(blockNumber + i);
        }
        for (int i2 = 0; i2 < 16; i2++) {
            bout.write(data[i2]);
        }
        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length;
        return msg;
    }

    private CngModel GetCng(byte[] getData) {
        CngModel Result = new CngModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 14, 2);
        Result.LogDaysFlg = Integer.parseInt(_s.substring(0, 1));
        Result.IndexValueFlg = Integer.parseInt(_s.substring(1, 2));
        Result.WeekControlFlg = Integer.parseInt(_s.substring(2, 3));
        Result.WeekStartFlg = Integer.parseInt(_s.substring(3, 4));
        Result.ClockTimeFlg = Integer.parseInt(_s.substring(5, 6));
        Result.LogCountFlg = Integer.parseInt(_s.substring(8, 9));
        Result.LogIntervalFlg = Integer.parseInt(_s.substring(9, 10));
        Result.OpenCockFlg = Integer.parseInt(_s.substring(10, 11));
        Result.MaxFlowFlg = Integer.parseInt(_s.substring(11, 12));
        Result.ContinueFlg2 = Integer.parseInt(_s.substring(12, 13));
        Result.ContinueFlg1 = Integer.parseInt(_s.substring(13, 14));
        return Result;
    }

    private ContinueModel GetContinue1(byte[] getData) {
        ContinueModel Result = new ContinueModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 4, 3);
        Result.ContinueValue = bin2int(_s.substring(0, 8));
        Result.ContinueTime = bin2int(_s.substring(8, 16));
        Result.ContinueFlg = Integer.parseInt(_s.substring(21, 22));
        Result.ContinueCon = bin2int(_s.substring(22, 24));
        return Result;
    }

    private Cng2Model GetCng2(byte[] getData) {
        Cng2Model Result = new Cng2Model();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 0, 1);
        Result.FlowDetectionFlg = Integer.parseInt(_s.substring(0, 1));
        Result.QuakeConFlg = Integer.parseInt(_s.substring(1, 2));
        Result.ReductionConFlg = Integer.parseInt(_s.substring(2, 3));
        Result.OpenCoverConFlg = Integer.parseInt(_s.substring(3, 4));
        Result.EmergencyValueFlg = Integer.parseInt(_s.substring(6, 7));
        Result.EmergencyConFlg = Integer.parseInt(_s.substring(7, 8));
        return Result;
    }

    private ContinueModel GetContinue2(byte[] getData) {
        ContinueModel Result = new ContinueModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 7, 3);
        Result.ContinueValue = bin2int(_s.substring(0, 8));
        Result.ContinueTime = bin2int(_s.substring(8, 16));
        Result.ContinueFlg = Integer.parseInt(_s.substring(21, 22));
        Result.ContinueCon = bin2int(_s.substring(22, 24));
        return Result;
    }

    private ParModel GetPar(byte[] getData) {
        ParModel Result = new ParModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 1, 1);
        Result.QuakeCon = bin2int(_s.substring(0, 2));
        switch (Result.QuakeCon) {
            case 0:
            case 1:
            case 2:
                Result.OpenCoverCon = bin2int(_s.substring(2, 4));
                switch (Result.QuakeCon) {
                    case 0:
                    case 1:
                    case 2:
                        Result.FlowDetection = Integer.parseInt(_s.substring(4, 5));
                        Result.EmergencyCon = Integer.parseInt(_s.substring(5, 6));
                        Result.ReductionCon = Integer.parseInt(_s.substring(6, 7));
                        return Result;
                    default:
                        throw new RuntimeException("ReductionCon");
                }
            default:
                throw new RuntimeException("ReductionCon");
        }
    }

    private double GetEmergencyValue(byte[] getData) {
        return ((double) BCDTo(GetByteToBitString(getData, IsEncryption.Encrypt, 4, 3))) / 1000.0d;
    }

    //write history
    public boolean writeHistory(Tag tag, int CardHistoryNo, FirebaseDatabase mDatabase) {
        NfcF nfc = NfcF.get(tag);
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            nfc.connect();
            BlockDataList dataList = new BlockDataList();
            byte[][] data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, this.size, this.targetServiceCode, 5)));
            CheckDataLength(data);
            dataList.AddReadBlockData(data[0], 5, false);
            SetCardHistoryNo(data[0], CardHistoryNo);
            writeWithoutEncryption(nfc, dataList);

            DatabaseReference myRef = mDatabase.getReference("Version-1-1-10-" + timestamp.getTime());
            data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, this.size, this.targetServiceCode, 5)));
            CheckDataLength(data);
            if (GetCardHistoryNo(data[0]) != CardHistoryNo) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Card ID : ");
                stringBuilder.append(this.strCardId);
                stringBuilder.append(" || HistoryNo : ");
                stringBuilder.append(CardHistoryNo);
                myRef.setValue(stringBuilder);
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException ex2) {
                    }
                }
                return false;
            }
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex22) {
                    //LogUtil.i(ex22.toString());
                }
            }
            return true;
        } catch (Exception e) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Exception HistoryNo: ");
            stringBuilder2.append(e.getMessage());
            DatabaseReference myRef = mDatabase.getReference("Version-1-1-10-" + timestamp.getTime());
            myRef.setValue(stringBuilder2.toString());
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex3) {
                    //LogUtil.i(ex3.toString());
                }
            }
            return false;
        } catch (Throwable th) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex4) {

                }
            }
            return false;
        }
    }

    //write status

    public boolean writeStatus(Tag tag, FirebaseDatabase mDatabase) {
        NfcF nfc = NfcF.get(tag);
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            nfc.connect();
            BlockDataList dataList = new BlockDataList();
            byte[][] data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, this.size, this.targetServiceCode, 3)));
            CheckDataLength(data);
            dataList.AddReadBlockData(data[0], 3, false);
            SetCardStatus(data[0], 21);
            writeWithoutEncryption(nfc, dataList);

            DatabaseReference myRef = mDatabase.getReference("Version-1-1-10-" + timestamp.getTime());
            data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, this.size, this.targetServiceCode, 3)));
            CheckDataLength(data);

            if (GetCardStatus(data[0]) != Ascii.NAK) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Card ID : ");
                stringBuilder.append(this.strCardId);
                stringBuilder.append(" || Status : ");
                stringBuilder.append(data[0]);
                myRef.setValue(stringBuilder.toString());
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException ex) {
                    }
                }
                return false;
            }

            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex) {
                }
            }

            return true;
        } catch (Exception e) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Exception Status: ");
            stringBuilder2.append(e.getMessage());
            DatabaseReference myRef = mDatabase.getReference("Version-1-1-10-" + timestamp.getTime());
            myRef.setValue(stringBuilder2.toString());
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex3) {
                    //LogUtil.i(ex3.toString());
                }
            }
            return false;
        } catch (Throwable th) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex4) {

                }
            }
            return false;
        }
    }

    private void SetCardStatus(byte[] setData, int cardStatus) {
        if (!(cardStatus == 0 || cardStatus == 21 || cardStatus == 48)) {
            switch (cardStatus) {
                case 5:
                case 6:
                    break;
                default:
                    throw new RuntimeException("SetCardStatus Out Of Range");
            }
        }
        byte[] bArr = setData;
        SetHexStringToByte(bArr, IsEncryption.Encrypt, PadLeft(Integer.toHexString(cardStatus), 2, '0'), 0, 1);
    }

    private void SetCardHistoryNo(byte[] setData, int cardHistoryNo) {
        if (cardHistoryNo < 0 || SupportMenu.USER_MASK < cardHistoryNo) {
            throw new RuntimeException("CardHistoryNo");
        }
        byte[] bArr = setData;
        SetBitStringToByte(bArr, IsEncryption.Encrypt, Integer.toBinaryString(cardHistoryNo), 4, 2);
    }


    public String getHistoryNo(Tag tag) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            BlockDataList datalist = new BlockDataList();
            datalist.AddReadBlockData(parse(nfc.transceive(readWithoutEncryption(TargetIDm, this.size, this.targetServiceCode, 5)))[0], 5, true);
            String valueOf = String.valueOf(GetCardHistoryNo(datalist.GetReadBlockData(5)));
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex) {
//
                }
            }
            return valueOf;
        } catch (Exception e) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException ex2) {
//                    LogUtil.i(ex2.toString());
                }
            }
            return null;
        }
    }

    private int GetCardHistoryNo(byte[] getData) {
        return Integer.parseInt(GetByteToBitString(getData, IsEncryption.Encrypt, 4, 2), 2);
    }

    private void SetHexStringToByte(byte[] setData, IsEncryption enc, String val, int start, int length) {
        val = PadLeft(val, length * 2, '0');
        int j = 0;
        for (int i = 0; i < length; i++) {
            setData[start + i] = Encryption(enc, (byte) hex2int(val.substring(j, j + 2)));
            j += 2;
        }
    }

    private int GetErrorNo(byte[] getData) {
        return GetByteToInt(getData, IsEncryption.Encrypt, 6, 2);
    }

    private int GetOpenCount(byte[] getData) {
        return bin2int(GetByteToBitString(getData, IsEncryption.Encrypt, 1, 2));
    }

    private String GetWebApiDate(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(calendar.getTime()).replace("1970", "0001");
    }

    private Calendar GetLidTime(byte[] getData) {
        Calendar Result = Calendar.getInstance();
        String _ymd = String.format("%010d", new Object[]{BCDToLong(GetByteToBitString(getData, IsEncryption.Encrypt, 9, 5).substring(0, 40))});
        if (_ymd.equals("0000000000")) {
            Result.clear();
        } else {
            try {
                Result.setTime(new SimpleDateFormat("yyMMddHHmm").parse(_ymd));
            } catch (ParseException e) {
                throw new RuntimeException("ClockTimeFlg");
            }
        }
        return Result;
    }

    private Long BCDToLong(String bcd) {
        if (bcd.length() % 4 > 0) {
            throw new RuntimeException("BCDTo Out Of Range");
        }
        String _s = "";
        int _len = bcd.length() / 4;
        for (int i = 0; i < _len; i++) {
            int start = i * 4;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_s);
            stringBuilder.append(String.valueOf(bin2int(bcd.substring(start, start + 4))));
            _s = stringBuilder.toString();
        }
        return Long.valueOf(Long.parseLong(_s));
    }

    private double GetIndexValue(byte[] getData) {
        return ((double) BCDTo(GetByteToBitString(getData, IsEncryption.Encrypt, 0, 4))) / 1000.0d;
    }

    public static String getFormatDate(String strDate) {
        Date date = null;
        try {
            date = new Date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strDate.replace("0001", "1970").replace("T", StringUtils.SPACE)).getTime());
        } catch (ParseException e) {
        }
        SimpleDateFormat dateformat = new SimpleDateFormat(getLocalDateFormat(resources.getConfiguration().locale), resources.getConfiguration().locale);
        Calendar cal_created = Calendar.getInstance();
        cal_created.setTime(date);
        return dateformat.format(cal_created.getTime()).replace("1970", "0001");
    }

    private static String getLocalDateFormat(Locale locale) {
        String result = "";
        String language = locale.getLanguage();
        if (language.hashCode() == 3241 && language.equals("en")) {
        }
        return "MMM d, yyyy HH:mm a";
    }


    private int GetByteToHexInt(byte[] getData, IsEncryption enc, int start, int length) {
        byte[] _data = getData;
        if (_data.length != 16) {
            throw new RuntimeException("Failed to addCard.");
        }
        int end = (start + length) - 1;
        String _val = "";
        for (int i = start; i <= end; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_val);
            stringBuilder.append(PadLeft(Integer.toHexString(Decryption(enc, _data[i])), 2, '0'));
            _val = stringBuilder.toString();
        }
        return hex2int(_val);
    }

    private MaxFlowModel GetMaxFlow(byte[] getData) {
        MaxFlowModel Result = new MaxFlowModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 10, 2);
        Result.MaxFlowValue = bin2int(_s.substring(0, 8));
        Result.MaxFlowFlg = Integer.parseInt(_s.substring(13, 14));
        Result.MaxFlowCon = bin2int(_s.substring(14, 16));
        return Result;
    }

    private int GetLogDays(byte[] getData) {
        int num = GetByteToHexInt(getData, IsEncryption.Encrypt, 12, 2);
        if (num <= 368) {
            return num;
        }
        throw new RuntimeException("LogDays");
    }

    private OpenCockModel GetOpenCock(byte[] getData) {
        OpenCockModel Result = new OpenCockModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 12, 1);
        Result.OpenCockFlg = Integer.parseInt(_s.substring(5, 6));
        Result.OpenCockCon = bin2int(_s.substring(6, 8));
        return Result;
    }

    private int GetLogInterval(byte[] getData) {
        return bin2int(GetByteToBitString(getData, IsEncryption.Encrypt, 13, 1));
    }

    private int GetLogCount(byte[] getData) {
        int Result = bin2int(GetByteToBitString(getData, IsEncryption.Encrypt, 14, 2));
        if (Result <= DateTimeConstants.MINUTES_PER_DAY) {
            return Result;
        }
        throw new RuntimeException("LogCount");
    }

    private ClockTimeModel GetClockTime(byte[] getData) {
        ClockTimeModel Result = new ClockTimeModel();
        String _ymd = String.format("%010d", new Object[]{BCDToLong(GetByteToBitString(getData, IsEncryption.Encrypt, 3, 6).substring(0, 40))});
        if (_ymd.equals("0000000000")) {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            Result.ClockTime = cal;
        } else {
            try {
                Result.ClockTime.setTime(new SimpleDateFormat("yyMMddHHmm").parse(_ymd));
            } catch (ParseException e) {
                throw new RuntimeException("ClockTimeFlg");
            }
        }
//        Result.ClockTimeFlg = Integer.parseInt(_s.substring(47, 48));
//        SimpleDateFormat sdFormat = new byte[2];
        return Result;
    }

    private int GetWeekStart(byte[] getData) {
        return hex2int(GetByteToHexString(getData, IsEncryption.Encrypt, 14, 1));
    }

    private int GetWeekControl(byte[] getData) {
        int i = 1;
        String _s = GetByteToHexString(getData, IsEncryption.Encrypt, 15, 1);
        int hashCode = _s.hashCode();
        if (hashCode != 1536) {
            if (hashCode == 2218) {
            }
        } else if (_s.equals("00")) {
            i = 0;
            switch (i) {
                case 0:
                case 1:
                    return hex2int(_s);
                default:
                    throw new RuntimeException("WeekControl");
            }
        }
        i = -1;
        switch (i) {
            case 0:
            case 1:
                break;
            default:
                break;
        }
        return i;
    }

    private CntModel GetCnt(byte[] getData) {
        CntModel Result = new CntModel();
        String _s = GetByteToBitString(getData, IsEncryption.Encrypt, 2, 1);
        Result.RemoteValueCon = bin2int(_s.substring(0, 2));
        switch (Result.RemoteValueCon) {
            case 0:
            case 1:
            case 2:
                Result.SleepModeFlg = bin2int(_s.substring(3, 4));
                return Result;
            default:
                throw new RuntimeException("RemoteValueCon");
        }
    }

    private GMA_CARD_HISTORY GetCardHistory1(byte[] getData) {
        return GetCardHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 6));
    }

    private GMA_CARD_HISTORY GetCardHistory2(byte[] getData) {
        return GetCardHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 6));
    }

    private GMA_CARD_HISTORY GetCardHistory3(byte[] getData) {
        return GetCardHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 6));
    }

    private GMA_CARD_HISTORY GetCardHistory4(byte[] getData) {
        return GetCardHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 6));
    }

    private GMA_CARD_HISTORY GetCardHistory5(byte[] getData) {
        return GetCardHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 6));
    }

    private GMA_CARD_HISTORY GetCardHistory(String _s) {
        GMA_CARD_HISTORY Result = new GMA_CARD_HISTORY();
        String _ymd = String.format("%010d", new Object[]{BCDToLong(_s.substring(0, 40))});
        if (_ymd.equals("0000000000")) {
            Result.HistoryTime.clear();
        } else {
            try {
                Result.HistoryTime.setTime(new SimpleDateFormat("yyMMddHHmm").parse(_ymd));
            } catch (ParseException e) {
                throw new RuntimeException("GetCardHistory1");
            }
        }
        Result.HistoryType = bin2int(_s.substring(46, 48));
        return Result;
    }

    private GMA_ERROR_HISTORY GetErrorHistory1(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory2(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory3(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory4(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory5(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory6(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory7(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory8(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory9(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 0, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory10(byte[] getData) {
        return GetErrorHistory(GetByteToBitString(getData, IsEncryption.NotEncrypt, 8, 8));
    }

    private GMA_ERROR_HISTORY GetErrorHistory(String _s) {
        GMA_ERROR_HISTORY Result = new GMA_ERROR_HISTORY();
        String _ymd = String.format("%010d", new Object[]{BCDToLong(_s.substring(0, 40))});
        if (_ymd.equals("0000000000")) {
            Result.ErrorTime.clear();
        } else {
            try {
                Result.ErrorTime.setTime(new SimpleDateFormat("yyMMddHHmm").parse(_ymd));
            } catch (ParseException e) {
                throw new RuntimeException("GetCardHistory1");
            }
        }
        Result.ErrorGroup = bin2int(_s.substring(46, 48));
        try {
            Result.ErrorType = new String(new byte[]{(byte) bin2int(_s.substring(48, 56)), (byte) bin2int(_s.substring(56, 64))}, "US-ASCII");
        } catch (UnsupportedEncodingException e2) {
            //LogUtil.e(e2);
        }
        return Result;
    }

    @SuppressLint("WrongConstant")
    private GMA_LOG_DATA[] GetLogHour() {

        int i = 2;
        int _index = bin2int(GetByteToBitString((byte[]) LogHour.get(0), IsEncryption.NotEncrypt, 0, 2));
        if (_index > 1439) {
            throw new RuntimeException("LogHoer");
        }
        String str;
        int i2;
        int i3;
        double _latestGasValue = ((double) BCDTo(GetByteToBitString((byte[]) LogHour.get(0), IsEncryption.NotEncrypt, 2, 4))) / 1000.0d;
        String _s = GetByteToBitString((byte[]) LogHour.get(0), IsEncryption.NotEncrypt, 6, 5);
        Long _n = BCDToLong(_s.substring(0, 40));
        Boolean bLastDay = Boolean.valueOf(true);
        String _ymd = String.format("%010d", new Object[]{_n});
        Calendar _latestGasTime = Calendar.getInstance();
        if (_ymd.equals("0000000000")) {
            bLastDay = Boolean.valueOf(false);
        } else {
            try {
                _latestGasTime.setTime(new SimpleDateFormat("yyMMddHHmm").parse(_ymd));
            } catch (ParseException e) {
                str = _s;
                i2 = _index;
                throw new RuntimeException("GasTime");
            }
        }
        ArrayList sdFormat = new ArrayList();
        int _col = 0;
        int _row = 1;
        int i4 = 0;
        while (true) {
            i3 = DateTimeConstants.MINUTES_PER_DAY;
            if (i4 >= DateTimeConstants.MINUTES_PER_DAY) {
                break;
            }
            sdFormat.add(Integer.valueOf(hex2int(PadLeft(String.valueOf(((byte[]) LogHour.get(_row))[_col]), i, '0'))));
            i = _col + 1;
            if (i > 15) {
                _row++;
                _col = 0;
            } else {
                _col = i;
            }
            i4++;
            i = 2;
        }
        ArrayList<Integer> _sortedList = new ArrayList();
        for (i = _index; i >= 0; i--) {
            _sortedList.add((Integer) sdFormat.get(i));
        }
        int i5 = 1439;
        while (true) {
            i = i5;
            if (i <= _index) {
                break;
            }
            _sortedList.add((Integer) sdFormat.get(i));
            i5 = i - 1;
        }
        GMA_LOG_DATA[] _log = new GMA_LOG_DATA[DateTimeConstants.MINUTES_PER_DAY];
        Calendar _gasTime = Calendar.getInstance();
        double _gasValue = 0.0d;
        if (bLastDay.booleanValue()) {
            _gasTime = _latestGasTime;
            _gasValue = _latestGasValue;
        }
        int i6 = 0;
        while (true) {
            i4 = i6;
            if (i4 < i3) {
                ArrayList<Integer> _list;
                _log[i4] = new GMA_LOG_DATA();
                if (bLastDay.booleanValue()) {
                    str = _s;
                    _list = sdFormat;
                    _log[i4].GasTime = (Calendar) _gasTime.clone();
                    if (i4 > 0) {
                        i2 = _index;
                        _log[i4].GasValue = _gasValue - ((((double) ((Integer) _sortedList.get(i4 - 1)).intValue()) * 20.0d) / 1000.0d);
//                        _log[i4].GasValue = sdFormat;
                        sdFormat.add(_log[i4].GasValue);
                    } else {
                        i2 = _index;
                        _log[i4].GasValue = _gasValue;
                    }
//                    _log[i4].GasValue = sdFormat;
                    _gasTime.add(10, -1);
                    sdFormat.add(_gasValue);
                } else {
                    _log[i4].GasTime.clear();
                    str = _s;
                    _list = sdFormat;
                    _log[i4].GasValue = 0.0;
                    i2 = _index;
                }
                i6 = i4 + 1;
                _s = str;
                Object sdFormat2 = _list;
                _index = i2;
                i3 = DateTimeConstants.MINUTES_PER_DAY;
            } else {
//                SimpleDateFormat simpleDateFormat = sdFormat2;
                i2 = _index;
                return _log;
            }
        }
    }

    @SuppressLint("WrongConstant")
    private GMA_LOG_DATA[] GetLogDay() {
        int i = 2;
        int _index = bin2int(GetByteToBitString((byte[]) LogDay.get(0), IsEncryption.NotEncrypt, 0, 2));
        if (_index > 367) {
            throw new RuntimeException("LogDay");
        }
        String str;
        int i2;
        int i3;
        double _latestGasValue = ((double) BCDTo(GetByteToBitString((byte[]) LogDay.get(0), IsEncryption.NotEncrypt, 2, 4))) / 1000.0d;
        String _s = GetByteToBitString((byte[]) LogDay.get(0), IsEncryption.NotEncrypt, 6, 5);
        Long _n = BCDToLong(_s.substring(0, 40));
        Boolean bLastDay = Boolean.valueOf(true);
        String _ymd = String.format("%010d", new Object[]{_n});
        Calendar _latestGasTime = Calendar.getInstance();
        if (_ymd.equals("0000000000")) {
            bLastDay = Boolean.valueOf(false);
        } else {
            try {
                _latestGasTime.setTime(new SimpleDateFormat("yyMMddHHmm").parse(_ymd));
            } catch (ParseException e) {
                str = _s;
                i2 = _index;
                throw new RuntimeException("GasTime");
            }
        }
        ArrayList sdFormat = new ArrayList();
        int _col = 0;
        int _row = 1;
        int i4 = 0;
        while (true) {
            i3 = 368;
            if (i4 >= 368) {
                break;
            }
            sdFormat.add(Integer.valueOf(hex2int(PadLeft(String.valueOf(((byte[]) LogDay.get(_row))[_col]), i, '0'))));
            i = _col + 1;
            if (i > 15) {
                _row++;
                _col = 0;
            } else {
                _col = i;
            }
            i4++;
            i = 2;
        }
        ArrayList<Integer> _sortedList = new ArrayList();
        for (i = _index; i >= 0; i--) {
            _sortedList.add((Integer) sdFormat.get(i));
        }
        int i5 = 367;
        while (true) {
            i = i5;
            if (i <= _index) {
                break;
            }
            _sortedList.add((Integer) sdFormat.get(i));
            i5 = i - 1;
        }
        GMA_LOG_DATA[] _log = new GMA_LOG_DATA[368];
        Calendar _gasTime = Calendar.getInstance();
        double _gasValue = 0.0d;
        if (bLastDay.booleanValue()) {
            _gasTime = _latestGasTime;
            _gasValue = _latestGasValue;
        }
        int i6 = 0;
        while (true) {
            int i7 = i6;
            if (i7 < i3) {
                ArrayList<Integer> _list;
                _log[i7] = new GMA_LOG_DATA();
                if (bLastDay.booleanValue()) {
                    str = _s;
                    _list = sdFormat;
                    _log[i7].GasTime = (Calendar) _gasTime.clone();
                    if (i7 > 0) {
                        i2 = _index;
                        _log[i7].GasValue = _gasValue - ((((double) ((Integer) _sortedList.get(i7 - 1)).intValue()) * 100.0d) / 1000.0d);
                    } else {
                        i2 = _index;
                        _log[i7].GasValue = _gasValue;
                    }
//                    _log[i7].GasValue = sdFormat;
                    _gasTime.add(5, -1);
//                    _gasValue = sdFormat;
                } else {
                    _log[i7].GasTime.clear();
                    str = _s;
                    _list = sdFormat;
//                    _log[i7].GasValue = null;
                    i2 = _index;
                }
                i6 = i7 + 1;
                _s = str;
                Object sdFormat2 = _list;
                _index = i2;
                i3 = 368;
            } else {
//                SimpleDateFormat simpleDateFormat = sdFormat2;
                i2 = _index;
                return _log;
            }
        }
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }


    public boolean GasChargeRefundCard(Tag tag, double Credit, double Unit, int BasicFee, double emergencyValue, String meter) {
        String str = meter;
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            String _cardIdm = GetCardIdm(TargetIDm);
            if (this.strCardId.equals(_cardIdm)) {
                byte[][] data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 2)));
                CheckDataLength(data);
                byte _cardGroup = GetCardGroup(data[0]);
                BlockDataList datalist = new BlockDataList();
                datalist.AddReadBlockData(data[0], 2, false);
                data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)));
                CheckDataLength(data);
                byte _cardStatus = GetCardStatus(data[0]);
                datalist.AddReadBlockData(data[0], 3, false);
                data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 4)));
                CheckDataLength(data);
                datalist.AddReadBlockData(data[0], 4, false);
                data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)));
                CheckDataLength(data);
                datalist.AddReadBlockData(data[0], 5, false);
                if (this.isChargeCheckFailed || isGasChargeCard(_cardGroup)) {
                    byte[] req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 5);
                    byte[] res = nfc.transceive(req);
                    CheckDataLength(parse(res));

                    SetUnit(datalist.GetReadBlockData(3), Unit);
                    SetBasicFee(datalist.GetReadBlockData(3), BasicFee);
                    SetRefund1(datalist.GetReadBlockData(4), 0.0);
                    SetRefund2(datalist.GetReadBlockData(4), 0.0);
                    SetCardStatus(datalist.GetReadBlockData(3), 21);
                    SetCredit(datalist.GetReadBlockData(3), Credit);
                    CngModel tempCngModel = new CngModel();
                    tempCngModel.ContinueFlg1 = 1;
                    tempCngModel.ContinueFlg2 = 1;
                    req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 5);
                    res = nfc.transceive(req);
                    data = parse(res);
                    CheckDataLength(data);
                    datalist.AddReadBlockData(data[0], 5, false);
                    SetCng(datalist.GetReadBlockData(5), tempCngModel);
                    ContinueModel tempContinueModel = new ContinueModel();
                    tempContinueModel.ContinueTime = 10;
                    tempContinueModel.ContinueValue = 10;
                    tempContinueModel.ContinueCon = 1;
                    tempContinueModel.ContinueFlg = 1;
                    req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 6);
                    res = nfc.transceive(req);
                    byte[][] data2 = parse(res);
                    CheckDataLength(data2);
                    datalist.AddReadBlockData(data2[0], 6, false);
                    SetContinue1(datalist.GetReadBlockData(6), tempContinueModel);
                    ContinueModel tempContinueModel2 = new ContinueModel();
                    tempContinueModel2.ContinueTime = 24;
                    tempContinueModel2.ContinueValue = 5;
                    tempContinueModel2.ContinueCon = 2;
                    tempContinueModel2.ContinueFlg = 1;
                    SetContinue2(datalist.GetReadBlockData(6), tempContinueModel2);
                    Cng2Model tempCng2Model = new Cng2Model();
                    tempCng2Model.QuakeConFlg = 1;
                    tempCng2Model.EmergencyConFlg = 1;
                    tempCng2Model.EmergencyValueFlg = 1;
                    ContinueModel tempContinueModel22 = tempContinueModel2;
                    CngModel tempCngModel2 = tempCngModel;
                    byte[] req2 = readWithoutEncryption(TargetIDm, size, targetServiceCode, 8);
                    byte[] res2 = nfc.transceive(req2);
                    data2 = parse(res2);
                    CheckDataLength(data2);
                    datalist.AddReadBlockData(data2[0], 8, false);
                    SetCng2(datalist.GetReadBlockData(8), tempCng2Model);
                    ParModel tempParModel = new ParModel();
                    tempParModel.QuakeCon = 2;
                    tempParModel.EmergencyCon = 1;
                    SetPar(datalist.GetReadBlockData(8), tempParModel);
                    byte _cardGroup2 = _cardGroup;
                    SetEmergencyValue(datalist.GetReadBlockData(8), emergencyValue);
                    writeWithoutEncryption(nfc, datalist);
                    //LogUtil.i("Success to write a card.");
                    if (CheckWroteData(datalist, nfc, tempCngModel2, tempContinueModel, tempContinueModel22, tempCng2Model, tempParModel, emergencyValue)) {
                        //LogUtil.i("Success to check a written card.");
                        this.isChargeCheckFailed = false;
                        if (nfc != null) {
                            try {
                                nfc.close();
                            } catch (IOException e) {
                                //LogUtil.i(e.toString());
                            }
                        }
                        ReturnLocale();
                        return true;
                    }
                    this.isChargeCheckFailed = true;
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (IOException e2) {
                            //LogUtil.i(e2.toString());
                        }
                    }
                    ReturnLocale();
                    return false;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("At isGasChargeCard.\nisChargeCheckFailed : ");
                stringBuilder.append(this.isChargeCheckFailed);
                stringBuilder.append("\nCardId : ");
                stringBuilder.append(this.strCardId);
                stringBuilder.append("\nMeter : ");
                stringBuilder.append(str);
                stringBuilder.append("\nCardStatus : ");
                stringBuilder.append(_cardStatus);
                stringBuilder.append("\nCardGroup : ");
                stringBuilder.append(_cardGroup);
                //LogUtil.i(stringBuilder.toString());
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e22) {
                        //LogUtil.i(e22.toString());
                    }
                }
                ReturnLocale();
                return false;
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("At CardId checking.\n");
            stringBuilder2.append(this.strCardId);
            stringBuilder2.append("\n");
            stringBuilder2.append(str);
            stringBuilder2.append("\n");
            //LogUtil.i(stringBuilder2.toString());
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e222) {
                    //LogUtil.i(e222.toString());
                }
            }
            ReturnLocale();
            return false;
        } catch (Exception e3) {
            Exception e4 = e3;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Exeption at GasChargeCard.\n");
            stringBuilder3.append(this.strCardId);
            stringBuilder3.append("\n");
            stringBuilder3.append(str);
            stringBuilder3.append("\n");
//            stringBuilder3.append(//LogUtil.Output(e4.getStackTrace()));
            //LogUtil.i(stringBuilder3.toString());
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2222) {
                    //LogUtil.i(e2222.toString());
                }
            }
            ReturnLocale();
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e22222) {
                    //LogUtil.i(e22222.toString());
                }
            }
            ReturnLocale();
            return false;
        }
    }

    public boolean GasChargeRefundCard(Tag tag, double Credit, double Unit, int BasicFee, double emergencyValue) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            String _cardIdm = GetCardIdm(TargetIDm);
            if (this.strCardId.equals(_cardIdm)) {
                byte[][] data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, this.targetServiceCode, 2)));
                CheckDataLength(data);
                byte _cardGroup = GetCardGroup(data[0]);
                BlockDataList datalist = new BlockDataList();
                datalist.AddReadBlockData(data[0], 2, false);
                data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 3)));
                CheckDataLength(data);
                byte _cardStatus = GetCardStatus(data[0]);
                datalist.AddReadBlockData(data[0], 3, false);
                data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 4)));
                CheckDataLength(data);
                datalist.AddReadBlockData(data[0], 4, false);
                data = parse(nfc.transceive(readWithoutEncryption(TargetIDm, size, targetServiceCode, 5)));
                CheckDataLength(data);
                datalist.AddReadBlockData(data[0], 5, false);
                if (this.isChargeCheckFailed || isGasChargeCard(_cardGroup)) {
                    byte[] req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 5);
                    byte[] res = nfc.transceive(req);
                    CheckDataLength(parse(res));

                    SetUnit(datalist.GetReadBlockData(3), Unit);
                    SetBasicFee(datalist.GetReadBlockData(3), BasicFee);
                    SetRefund1(datalist.GetReadBlockData(4), 0.0);
                    SetRefund2(datalist.GetReadBlockData(4), 0.0);
                    SetCardStatus(datalist.GetReadBlockData(3), 21);
                    SetCredit(datalist.GetReadBlockData(3), Credit);
                    CngModel tempCngModel = new CngModel();
                    tempCngModel.ContinueFlg1 = 1;
                    tempCngModel.ContinueFlg2 = 1;
                    req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 5);
                    res = nfc.transceive(req);
                    data = parse(res);
                    CheckDataLength(data);
                    datalist.AddReadBlockData(data[0], 5, false);
                    SetCng(datalist.GetReadBlockData(5), tempCngModel);
                    ContinueModel tempContinueModel = new ContinueModel();
                    tempContinueModel.ContinueTime = 10;
                    tempContinueModel.ContinueValue = 10;
                    tempContinueModel.ContinueCon = 1;
                    tempContinueModel.ContinueFlg = 1;
                    req = readWithoutEncryption(TargetIDm, size, targetServiceCode, 6);
                    res = nfc.transceive(req);
                    byte[][] data2 = parse(res);
                    CheckDataLength(data2);
                    datalist.AddReadBlockData(data2[0], 6, false);
                    SetContinue1(datalist.GetReadBlockData(6), tempContinueModel);
                    ContinueModel tempContinueModel2 = new ContinueModel();
                    tempContinueModel2.ContinueTime = 24;
                    tempContinueModel2.ContinueValue = 5;
                    tempContinueModel2.ContinueCon = 2;
                    tempContinueModel2.ContinueFlg = 1;
                    SetContinue2(datalist.GetReadBlockData(6), tempContinueModel2);
                    Cng2Model tempCng2Model = new Cng2Model();
                    tempCng2Model.QuakeConFlg = 1;
                    tempCng2Model.EmergencyConFlg = 1;
                    tempCng2Model.EmergencyValueFlg = 1;
                    ContinueModel tempContinueModel22 = tempContinueModel2;
                    CngModel tempCngModel2 = tempCngModel;
                    byte[] req2 = readWithoutEncryption(TargetIDm, size, targetServiceCode, 8);
                    byte[] res2 = nfc.transceive(req2);
                    data2 = parse(res2);
                    CheckDataLength(data2);
                    datalist.AddReadBlockData(data2[0], 8, false);
                    SetCng2(datalist.GetReadBlockData(8), tempCng2Model);
                    ParModel tempParModel = new ParModel();
                    tempParModel.QuakeCon = 2;
                    tempParModel.EmergencyCon = 1;
                    SetPar(datalist.GetReadBlockData(8), tempParModel);
                    byte _cardGroup2 = _cardGroup;
                    SetEmergencyValue(datalist.GetReadBlockData(8), emergencyValue);
                    writeWithoutEncryption(nfc, datalist);
                    //LogUtil.i("Success to write a card.");
                    if (CheckWroteData(datalist, nfc, tempCngModel2, tempContinueModel, tempContinueModel22, tempCng2Model, tempParModel, emergencyValue)) {
                        //LogUtil.i("Success to check a written card.");
                        this.isChargeCheckFailed = false;
                        if (nfc != null) {
                            try {
                                nfc.close();
                            } catch (IOException e) {
                                //LogUtil.i(e.toString());
                            }
                        }
                        ReturnLocale();
                        return true;
                    }
                    this.isChargeCheckFailed = true;
                    if (nfc != null) {
                        try {
                            nfc.close();
                        } catch (IOException e2) {
                            //LogUtil.i(e2.toString());
                        }
                    }
                    ReturnLocale();
                    return false;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("At isGasChargeCard.\nisChargeCheckFailed : ");
                stringBuilder.append(this.isChargeCheckFailed);
                stringBuilder.append("\nCardId : ");
                stringBuilder.append(this.strCardId);
                stringBuilder.append("\nCardStatus : ");
                stringBuilder.append(_cardStatus);
                stringBuilder.append("\nCardGroup : ");
                stringBuilder.append(_cardGroup);
                //LogUtil.i(stringBuilder.toString());
                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e22) {
                        //LogUtil.i(e22.toString());
                    }
                }
                ReturnLocale();
                return false;
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("At CardId checking.\n");
            stringBuilder2.append(this.strCardId);
            stringBuilder2.append("\n");
            //LogUtil.i(stringBuilder2.toString());
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e222) {
                    //LogUtil.i(e222.toString());
                }
            }
            ReturnLocale();
            return false;
        } catch (Exception e3) {
            Exception e4 = e3;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Exeption at GasChargeCard.\n");
            stringBuilder3.append(this.strCardId);
            stringBuilder3.append("\n");
//            stringBuilder3.append(//LogUtil.Output(e4.getStackTrace()));
            //LogUtil.i(stringBuilder3.toString());
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e2222) {
                    //LogUtil.i(e2222.toString());
                }
            }
            ReturnLocale();
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e22222) {
                    //LogUtil.i(e22222.toString());
                }
            }
            ReturnLocale();
            return false;
        }
    }

    private boolean isGasChargeCard(byte CardGroup) {
        if (CardGroup != Keyboard.VK_F8) {
            return false;
        } else {
            return true;
        }


    }


    public boolean writeCardInitialize(Tag tag, String CustomerId, double unitPrice, int basePrice, double emergencyValue) {
        NfcF nfc = NfcF.get(tag);
        try {
            nfc.connect();
            BlockDataList datalist = new BlockDataList();
            //if any problem bellow code un-comment
//            int i = 0;
//            while (i < FelicaBlock.Max_block) {
//                if (!(i == 9 || i == 10)) {
//                    datalist.AddReadBlockData(null, i, false);
//                }
//                i++;
//            }
            SetVersionNo(datalist.GetReadBlockData(0), 1);
            SetCardStatus(datalist.GetReadBlockData(3), 48);
            SetCardIdm(datalist.GetReadBlockData(0), GetCardIdm(TargetIDm));
            try {
                SetCustomerId(datalist.GetReadBlockData(1), datalist.GetReadBlockData(2), CustomerId);
                SetCardGroup(datalist.GetReadBlockData(2), (byte) 119); // Gard Group 119 = Customer Card
                SetCredit(datalist.GetReadBlockData(3), 0.0d);
                SetUnit(datalist.GetReadBlockData(3), unitPrice);
                SetBasicFee(datalist.GetReadBlockData(3), basePrice);
                SetRefund1(datalist.GetReadBlockData(4), 0.0d);
                SetRefund2(datalist.GetReadBlockData(4), 0.0d);
                SetUntreatedFee(datalist.GetReadBlockData(4), 0);
                SetIndexValue(datalist.GetReadBlockData(5), 0.0d);
                SetCardHistoryNo(datalist.GetReadBlockData(5), 0);
                SetErrorNo(datalist.GetReadBlockData(5), 0);
                SetLogDays(datalist.GetReadBlockData(5), 0);
                CngModel tempCngModel = new CngModel();
                tempCngModel.ContinueFlg1 = 1;
                tempCngModel.ContinueFlg2 = 1;
                SetCng(datalist.GetReadBlockData(5), tempCngModel);
                ContinueModel tempContinueModel = new ContinueModel();
                tempContinueModel.ContinueTime = 10;
                tempContinueModel.ContinueValue = 10;
                tempContinueModel.ContinueCon = 1;
                tempContinueModel.ContinueFlg = 1;
                SetContinue1(datalist.GetReadBlockData(6), tempContinueModel);
                tempContinueModel.ContinueTime = 24;
                tempContinueModel.ContinueValue = 5;
                tempContinueModel.ContinueCon = 2;
                tempContinueModel.ContinueFlg = 1;
                SetContinue2(datalist.GetReadBlockData(6), tempContinueModel);
                SetMaxFlow(datalist.GetReadBlockData(6), new MaxFlowModel());
                SetOpenCock(datalist.GetReadBlockData(6), new OpenCockModel());
                SetLogInterval(datalist.GetReadBlockData(6), 0);
                SetLogCount(datalist.GetReadBlockData(6), 0);
                SetOpenCount(datalist.GetReadBlockData(7), 0);
                SetClockTime(datalist.GetReadBlockData(7), new ClockTimeModel());
                SetLidTime(datalist.GetReadBlockData(7), null);
                SetWeekStart(datalist.GetReadBlockData(7), 0);
                SetWeekControl(datalist.GetReadBlockData(7), 0);
                Cng2Model tempCng2Model = new Cng2Model();
                tempCng2Model.QuakeConFlg = 1;
                tempCng2Model.EmergencyConFlg = 1;
                tempCng2Model.EmergencyValueFlg = 1;
                SetCng2(datalist.GetReadBlockData(8), tempCng2Model);
                ParModel tempParModel = new ParModel();
                tempParModel.QuakeCon = 2;
                tempParModel.EmergencyCon = 1;
                SetPar(datalist.GetReadBlockData(8), tempParModel);
                SetCnt(datalist.GetReadBlockData(8), new CntModel());
                SetEmergencyValue(datalist.GetReadBlockData(8), emergencyValue);
                writeWithoutEncryption(nfc, datalist);
            } catch (Exception e2) {

                if (nfc != null) {
                    try {
                        nfc.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                return false;

            }

            return true;
        } catch (Exception e) {
            if (nfc != null) {
                try {
                    nfc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }
    }

    private void SetVersionNo(byte[] setData, int versionNo) {
        if (versionNo < 0 || 99 < versionNo) {
            throw new RuntimeException("VersionNo");
        }
        byte[] bArr = setData;
        SetBitStringToByte(bArr, IsEncryption.NotEncrypt, ToBCD(Integer.valueOf(versionNo), 2), 0, 1);
    }

    private void SetCardIdm(byte[] setData, String cardIdm) {
        if (cardIdm == null || cardIdm.length() != 16) {
            throw new RuntimeException("CardIdm");
        }
        String _val = cardIdm;
        for (int i = 0; i < 8; i++) {
            setData[8 + i] = Encryption(IsEncryption.Encrypt, (byte) hex2int(_val.substring(i * 2, (i * 2) + 2)));
        }
    }

    private void SetCustomerId(byte[] setData1, byte[] setData2, String customerId) {
        if (customerId == null) {
            customerId = "";
        }
        if (customerId.length() < 20) {
            customerId = PadLeft(customerId, 20, '0');
        } else {
            customerId = customerId.substring(customerId.length() - 20, customerId.length());
        }
        int i = 0;
        byte[] _b = new byte[0];
        try {
            _b = customerId.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            //LogUtil.e(e);
        }
        for (int i2 = 0; i2 < 16; i2++) {
            setData1[i2] = Encryption(IsEncryption.Encrypt, _b[i2]);
        }
        while (i < 4) {
            setData2[i] = Encryption(IsEncryption.Encrypt, _b[16 + i]);
            i++;
        }
    }

    private void SetCardGroup(byte[] setData, byte cardGroup) {
        byte _temp = cardGroup;
        if (_temp == (byte) 0 || _temp == Keyboard.VK_F8 || _temp == (byte) -120) {
            byte[] bArr = setData;
            SetHexStringToByte(bArr, IsEncryption.Encrypt, PadLeft(Integer.toHexString(cardGroup), 2, '0'), 15, 1);
            return;
        }
        throw new RuntimeException("CardGroup");
    }

    private void SetUntreatedFee(byte[] setData, int untreatedFee) {
        if (ValidInt(untreatedFee, 4)) {
            SetIntToByte(setData, IsEncryption.Encrypt, untreatedFee, 12, 4);
            return;
        }
        throw new RuntimeException("UntreatedFee Out Of Range");
    }

    private void SetIndexValue(byte[] setData, double indexValue) {
        if (ValidDouble(indexValue, 5, 3)) {
            byte[] bArr = setData;
            SetBitStringToByte(bArr, IsEncryption.Encrypt, ToBCD(Long.valueOf(Math.round(1000.0d * indexValue)), 8), 0, 4);
            return;
        }
        throw new RuntimeException("IndexValue");
    }

    private void SetErrorNo(byte[] setData, int errorNo) {
        if (ValidInt(errorNo, 2)) {
            SetIntToByte(setData, IsEncryption.Encrypt, errorNo, 6, 2);
            return;
        }
        throw new RuntimeException("ErrorNo");
    }

    private void SetLogDays(byte[] setData, int logDays) {
        if (logDays < 0 || 368 < logDays) {
            throw new RuntimeException("LogDays");
        }
        SetHexIntToByte(setData, IsEncryption.Encrypt, logDays, 12, 2, 4);
    }

    private void SetHexIntToByte(byte[] setData, IsEncryption enc, int val, int start, int length, int inte) {
        int i = length;
        int _len = inte;
        int i2 = 0;
        String strFormat = "{0,number,";
        for (int i3 = 0; i3 < inte; i3++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(strFormat);
            stringBuilder.append("0");
            strFormat = stringBuilder.toString();
        }
        StringBuilder strFormat2 = new StringBuilder();
        strFormat2.append(strFormat);
        strFormat2.append("}");
        strFormat = MessageFormat.format(strFormat2.toString(), new Object[]{Integer.valueOf(val)});
        if (strFormat.length() != _len) {
            throw new RuntimeException("Failed to Write.");
        }
        String _hex = PadLeft(Integer.toHexString(Integer.parseInt(strFormat)), i * 2, '0');
        int j = 0;
        while (i2 < i) {
            setData[start + i2] = Encryption(enc, (byte) hex2int(_hex.substring(j, j + 2)));
            j += 2;
            i2++;
        }
        IsEncryption isEncryption = enc;
    }

    private void SetMaxFlow(byte[] setData, MaxFlowModel maxFlow) {
        String _s = "";
        if (maxFlow.MaxFlowValue < 0 || 255 < maxFlow.MaxFlowValue) {
            throw new RuntimeException("MaxFlowValue");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append(PadLeft(Integer.toBinaryString(maxFlow.MaxFlowValue), 8, '0'));
        _s = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append("00000");
        _s = stringBuilder.toString();
        switch (maxFlow.MaxFlowFlg) {
            case 0:
            case 1:
                stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(maxFlow.MaxFlowFlg));
                _s = stringBuilder.toString();
                switch (maxFlow.MaxFlowCon) {
                    case 0:
                    case 1:
                    case 2:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(PadLeft(Integer.toBinaryString(maxFlow.MaxFlowCon), 2, '0'));
                        byte[] bArr = setData;
                        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 10, 2);
                        return;
                    default:
                        throw new RuntimeException("MaxFlowCon");
                }
            default:
                throw new RuntimeException("MaxFlowFlg");
        }
    }

    private void SetOpenCock(byte[] setData, OpenCockModel openCock) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");
        stringBuilder.append("00000");
        String _s = stringBuilder.toString();
        switch (openCock.OpenCockFlg) {
            case 0:
            case 1:
                stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(openCock.OpenCockFlg));
                _s = stringBuilder.toString();
                switch (openCock.OpenCockCon) {
                    case 0:
                    case 1:
                    case 2:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(PadLeft(Integer.toBinaryString(openCock.OpenCockCon), 2, '0'));
                        byte[] bArr = setData;
                        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 12, 1);
                        return;
                    default:
                        throw new RuntimeException("OpenCockCon");
                }
            default:
                throw new RuntimeException("OpenCockFlg");
        }
    }

    private void SetLogInterval(byte[] setData, int logInterval) {
        String _s = "";
        if (logInterval < 0 || 255 < logInterval) {
            throw new RuntimeException("LogInterval");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(_s);
        stringBuilder.append(PadLeft(Integer.toBinaryString(logInterval), 8, '0'));
        byte[] bArr = setData;
        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 13, 1);
    }


    private void SetLogCount(byte[] setData, int logCount) {
        if (logCount < 0 || DateTimeConstants.MINUTES_PER_DAY < logCount) {
            throw new RuntimeException("LogCount");
        }
        byte[] bArr = setData;
        SetBitStringToByte(bArr, IsEncryption.Encrypt, PadLeft(Integer.toBinaryString(logCount), 16, '0'), 14, 2);
    }


    private void SetOpenCount(byte[] setData, int openCount) {
        if (openCount < 0 || SupportMenu.USER_MASK < openCount) {
            throw new RuntimeException("OpenCount");
        }
        byte[] bArr = setData;
        SetBitStringToByte(bArr, IsEncryption.Encrypt, Integer.toBinaryString(openCount), 1, 2);
    }

    private void SetClockTime(byte[] setData, ClockTimeModel clockTime) {
        String _s = "";
        Calendar cal = Calendar.getInstance();
        cal.clear();
        if (clockTime.ClockTime.compareTo(cal) != 0) {
            long _ymd = Long.parseLong(new SimpleDateFormat("yyMMddHHmm").format(clockTime.ClockTime.getTime()));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_s);
            stringBuilder.append(ToBCD(Long.valueOf(_ymd), 10));
            _s = stringBuilder.toString();
        } else {
            for (int i = 0; i < 40; i++) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(_s);
                stringBuilder2.append("0");
                _s = stringBuilder2.toString();
            }
        }
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(_s);
        stringBuilder3.append("0000000");
        _s = stringBuilder3.toString();
        switch (clockTime.ClockTimeFlg) {
            case 0:
            case 1:
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append(_s);
                stringBuilder3.append(String.valueOf(clockTime.ClockTimeFlg));
                byte[] bArr = setData;
                SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder3.toString(), 3, 6);
                return;
            default:
                throw new RuntimeException("ClockTimeFlg");
        }
    }


    private void SetLidTime(byte[] setData, Calendar lidTime) {
        String _s = "";
        if (lidTime != null) {
            long _ymd = Long.parseLong(new SimpleDateFormat("yyMMddHHmm").format(lidTime.getTime()));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_s);
            stringBuilder.append(ToBCD(Long.valueOf(_ymd), 10));
            _s = stringBuilder.toString();
        } else {
            for (int i = 0; i < 40; i++) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(_s);
                stringBuilder2.append("0");
                _s = stringBuilder2.toString();
            }
        }
        SetBitStringToByte(setData, IsEncryption.Encrypt, _s, 9, 5);
    }


    private void SetWeekStart(byte[] setData, int weekStart) {
        String _s = "";
        switch (weekStart) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(String.valueOf(weekStart));
                byte[] bArr = setData;
                SetHexStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 14, 1);
                return;
            default:
                throw new RuntimeException("WeekStart");
        }
    }

    private void SetWeekControl(byte[] setData, int weekControl) {
        String _s = "";
        if (weekControl == 0 || weekControl == Keyboard.VK_OEM_ATTN) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(_s);
            stringBuilder.append(Integer.toHexString(weekControl));
            byte[] bArr = setData;
            SetHexStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 15, 1);
            return;
        }
        throw new RuntimeException("WeekControl");
    }

    private void SetCnt(byte[] setData, CntModel cnt) {
        String _s = "";
        switch (cnt.RemoteValueCon) {
            case 0:
            case 1:
            case 2:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append(PadLeft(Integer.toBinaryString(cnt.RemoteValueCon), 2, '0'));
                _s = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(_s);
                stringBuilder.append("0");
                _s = stringBuilder.toString();
                switch (cnt.SleepModeFlg) {
                    case 0:
                    case 1:
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append(String.valueOf(cnt.SleepModeFlg));
                        _s = stringBuilder.toString();
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(_s);
                        stringBuilder.append("0000");
                        byte[] bArr = setData;
                        SetBitStringToByte(bArr, IsEncryption.Encrypt, stringBuilder.toString(), 2, 1);
                        return;
                    default:
                        throw new RuntimeException("SleepModeFlg");
                }
            default:
                throw new RuntimeException("RemoteValueCon");
        }
    }


}