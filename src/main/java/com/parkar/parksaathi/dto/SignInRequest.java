package com.parkar.parksaathi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;
}
