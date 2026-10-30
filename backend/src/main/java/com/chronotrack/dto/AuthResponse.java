package com.chronotrack.dto;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String fullName;
    private String email;
    private String timezone;
    private String preferredTimeFormat;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long id, String fullName, String email, String timezone, String preferredTimeFormat) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.timezone = timezone;
        this.preferredTimeFormat = preferredTimeFormat;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPreferredTimeFormat() {
        return preferredTimeFormat;
    }

    public void setPreferredTimeFormat(String preferredTimeFormat) {
        this.preferredTimeFormat = preferredTimeFormat;
    }
}
