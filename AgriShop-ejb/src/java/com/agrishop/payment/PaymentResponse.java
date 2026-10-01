package com.agrishop.payment;

import java.io.Serializable;

public class PaymentResponse implements Serializable {
    private boolean redirectRequired;
    private String paymentUrl;
    private String transactionRef;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(boolean redirectRequired, String paymentUrl, String transactionRef, String message) {
        this.redirectRequired = redirectRequired;
        this.paymentUrl = paymentUrl;
        this.transactionRef = transactionRef;
        this.message = message;
    }

    public boolean isRedirectRequired() {
        return redirectRequired;
    }

    public void setRedirectRequired(boolean redirectRequired) {
        this.redirectRequired = redirectRequired;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
