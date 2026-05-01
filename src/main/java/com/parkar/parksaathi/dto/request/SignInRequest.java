package com.parkar.parksaathi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;
}
