package com.gurucool.common.exception;

public class PaymentException extends RuntimeException {
    private final String razorpayErrorCode;

    public PaymentException(String message, String razorpayErrorCode) {
        super(message);
        this.razorpayErrorCode = razorpayErrorCode;
    }

    public PaymentException(String message) {
        super(message);
        this.razorpayErrorCode = null;
    }

    public String getRazorpayErrorCode() { return razorpayErrorCode; }
}
