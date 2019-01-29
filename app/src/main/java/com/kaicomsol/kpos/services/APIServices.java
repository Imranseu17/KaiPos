package com.kaicomsol.kpos.services;

import com.google.gson.JsonObject;
import com.kaicomsol.kpos.models.CardData;
import com.kaicomsol.kpos.models.CustomerData;
import com.kaicomsol.kpos.models.CustomerInfo;
import com.kaicomsol.kpos.models.Emergency;
import com.kaicomsol.kpos.models.Invoices;
import com.kaicomsol.kpos.models.Login;
import com.kaicomsol.kpos.models.Meter;
import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.PaymentID;
import com.kaicomsol.kpos.models.Receipt;
import com.kaicomsol.kpos.models.Refund;
import com.kaicomsol.kpos.models.SalesHistory;
import com.kaicomsol.kpos.models.SubData;
import com.kaicomsol.kpos.models.Success;
import com.kaicomsol.kpos.models.Transaction;
import com.kaicomsol.kpos.models.UpdateResponse;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIServices {

    @POST("/auth/login")
    Call<Login> getLogin(@HeaderMap Map<String, String> headers,
                         @Body JsonObject locationPost);

    @GET("/api/v1/pos/activatePosDevice/")
    Call<ResponseBody> activatePosDevice(@HeaderMap Map<String, String> headers,
                                  @Query("token") String token,
                                  @Query("deviceId") String deviceId);

    @GET("/api/v1/payment/dues/{cardNo}")
    Call<Invoices> getInvoices(@HeaderMap Map<String, String> headers, @Path("cardNo") String cardNo);


    @POST("/api/v1/payment/authorizePayment")
    Call<Payment> addPayment(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);


    @POST("/api/v1/payment/capturePayment")
    Call<PaymentID> capturePayment(@HeaderMap Map<String, String> headers, @Query("paymentId") String paymentId);


    @GET("/api/v1/payment/receipt")
    Call<Receipt> receiptPayment(@HeaderMap Map<String, String> headers, @Query("paymentId") String paymentId);

    @POST("/api/v1/pos/card/readCard")
    Call<ResponseBody> readCard(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);

    @GET("/api/v2/customerAccount/customerAccounts")
    Call<CustomerData> findCustomer(@HeaderMap Map<String, String> headers,
                                    @Query("accountNo") String accountNo,
                                    @Query("customerCode") String customerCode,
                                    @Query("meterSerial") String meterSerial,
                                    @Query("zone") String zone,
                                    @Query("area") String area,
                                    @Query("address") String address,
                                    @Query("apartment") String apartment,
                                    @Query("pageNumber") int pageNumber,
                                    @Query("pageSize") int pageSize);

    @POST("/api/v1/payment/cancelAuthorizedPayment")
    Call<PaymentID> cancelPayment(@HeaderMap Map<String, String> headers, @Query("paymentId") String paymentId);

    @GET("/api/v1/customerAccounts/{accountNo}")
    Call<CustomerInfo> getCustomerInfo(@HeaderMap Map<String, String> headers, @Path("accountNo") String accountNo);

    @GET("/api/v1/customerAccounts/meter/{accountNo}")
    Call<Meter> getMeterInfo(@HeaderMap Map<String, String> headers, @Path("accountNo") String accountNo);

    @GET("/api/v1/customer-payment/get-by-date")
    Call<List<Transaction>> getTransitionInfo(@HeaderMap Map<String, String> headers, @Query("accountNumber") String accountNumber, @Query("customerCode") String customerCode);

    @GET("/api/v1/customerMeters/{meterSerial}/subscriptions")
    Call<SubData> getSubscriptionInfo(@HeaderMap Map<String, String> headers, @Path("meterSerial") String meterSerial);

    @POST("/api/v1/pos/sales/getPosSales")
    Call<SalesHistory> getSalesHistory(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);

    @POST("/api/v1/workOrder/issueGasRefund")
    Call<Refund> getIssueRefund(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);

    @POST("/api/v1/workOrder/resolveRefund")
    Call<UpdateResponse> updateRefund(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);

    @GET("/api/v1/customerMeters/{meterSerial}")
    Call<CardData> getMeter(@HeaderMap Map<String, String> headers, @Path("meterSerial") String meterSerial);

    @GET("/api/v1/pos/settings/{meterType}/getMeterEmergencyValue")
    Call<Emergency> getEmergencyValue(@HeaderMap Map<String, String> headers, @Path("meterType") String meterType);

    @POST("/api/v1/customerMeters/card/add")
    Call<ResponseBody> addCard(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);

    @POST("/api/v1/pos/card/activateCard")
    Call<ResponseBody> activeCard(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);

    @DELETE("/api/v1/customerMeters/card/{cardIdm}")
    Call<ResponseBody> deleteCard(@HeaderMap Map<String, String> headers, @Path("cardIdm") String cardIdm);

    @POST("/api/v1/pos/card/lostCard")
    Call<ResponseBody> lostCard(@HeaderMap Map<String, String> headers, @Body JsonObject jsonObject);


}
