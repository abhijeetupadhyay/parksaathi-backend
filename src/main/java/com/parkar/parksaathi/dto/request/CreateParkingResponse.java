package com.parkar.parksaathi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateParkingResponse {
    private Long parkingId;
    private String message;
    private LocalDateTime createdAt;

}
