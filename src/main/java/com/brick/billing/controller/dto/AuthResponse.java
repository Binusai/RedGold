package com.brick.billing.controller.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private String redirectUrl;
    
    public AuthResponse(boolean success, String message, String redirectUrl) {
        this.success = success;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
}
