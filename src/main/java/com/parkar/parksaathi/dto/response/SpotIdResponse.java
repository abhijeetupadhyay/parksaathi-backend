package com.parkar.parksaathi.dto.response;

import lombok.Data;

@Data
public class SpotIdResponse {
    private Long spotId;
    public SpotIdResponse(Long spotId) { this.spotId = spotId; }
}