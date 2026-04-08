package com.peer.dto;

public class OtpResponse {
    private String message;
    private boolean otpRequired;

    public OtpResponse() {
    }

    public OtpResponse(String message, boolean otpRequired) {
        this.message = message;
        this.otpRequired = otpRequired;
    }

    public String getMessage() { return message; }
    public boolean isOtpRequired() { return otpRequired; }

    public void setMessage(String message) { this.message = message; }
    public void setOtpRequired(boolean otpRequired) { this.otpRequired = otpRequired; }
}
