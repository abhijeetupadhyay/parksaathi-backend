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
    private Long spotId;
    private String message;
    private LocalDateTime createdAt;
    
    public static CreateParkingResponse success(Long spotId) {
        return CreateParkingResponse.builder()
                .spotId(spotId)
                .message("Parking spot created successfully")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
