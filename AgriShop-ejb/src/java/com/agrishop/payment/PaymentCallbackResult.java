package com.agrishop.payment;

import java.io.Serializable;

public class PaymentCallbackResult implements Serializable {
    private boolean success;
    private String orderCode;
    private String transactionRef;
    private String responseCode;
    private String message;

    public PaymentCallbackResult() {
    }

    public PaymentCallbackResult(boolean success, String orderCode, String transactionRef, String responseCode, String message) {
        this.success = success;
        this.orderCode = orderCode;
        this.transactionRef = transactionRef;
        this.responseCode = responseCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
