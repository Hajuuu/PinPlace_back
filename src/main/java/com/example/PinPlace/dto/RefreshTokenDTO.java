package com.example.PinPlace.dto;

import com.example.PinPlace.entity.RefreshToken;
import com.example.PinPlace.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDTO {
    private String accessToken;
    private String refreshToken;

    public RefreshToken toEntity(User user, LocalDateTime expiresAt, String deviceInfo) {
        return RefreshToken.builder()
                .userToken(user)
                .token(this.refreshToken)
                .expired(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .build();
    }

}
