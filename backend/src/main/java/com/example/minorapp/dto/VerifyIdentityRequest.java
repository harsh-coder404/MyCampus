package com.example.minorapp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public class VerifyIdentityRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String rollNumber;
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRollNumber() {
        return rollNumber;
    }
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}

