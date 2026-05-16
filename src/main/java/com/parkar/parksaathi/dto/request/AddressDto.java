package com.parkar.parksaathi.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressDto {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private BigDecimal latitude;
    private BigDecimal longitude;
}