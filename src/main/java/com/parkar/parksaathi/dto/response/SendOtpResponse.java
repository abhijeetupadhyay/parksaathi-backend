package com.parkar.parksaathi.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SendOtpResponse {
    String message;
    Integer expiresInSeconds;
    String userStatus;
}
