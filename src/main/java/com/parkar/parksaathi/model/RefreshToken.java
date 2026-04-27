package com.parkar.parksaathi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RefreshToken")
public class RefreshToken {

    @Id
    private String token;

    private Long userId;

    private boolean revoked;

    @TimeToLive
    private Long timeToLive; // in seconds
}
