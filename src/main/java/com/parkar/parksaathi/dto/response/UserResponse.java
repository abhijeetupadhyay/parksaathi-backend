package com.parkar.parksaathi.dto.response;

import com.parkar.parksaathi.enums.UserStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String aadhaar;
    private UserStatus status;
}
