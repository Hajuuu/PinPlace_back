package com.example.PinPlace.service;

import com.example.PinPlace.dto.LoginRequestDTO;
import com.example.PinPlace.dto.RefreshTokenDTO;
import com.example.PinPlace.dto.TokenRefreshRequestDTO;
import com.example.PinPlace.entity.RefreshToken;
import com.example.PinPlace.entity.User;
import com.example.PinPlace.exception.RefreshTokenExpiredException;
import com.example.PinPlace.jwt.JwtTokenProvider;
import com.example.PinPlace.repository.RefreshTokenRepository;
import com.example.PinPlace.repository.UserRepository;
import com.example.PinPlace.security.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    public RefreshTokenDTO login(LoginRequestDTO loginRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getLoginId(), loginRequestDTO.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken();
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiryDate();
        String deviceInfo = loginRequestDTO.getDeviceInfo();

        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(accessToken, refreshToken);

        RefreshToken token = refreshTokenDTO.toEntity(user, expiresAt, deviceInfo);
        refreshTokenRepository.save(token);
        return refreshTokenDTO;
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        token.expire();
        refreshTokenRepository.save(token);
    }

    public RefreshTokenDTO refreshAccessToken(TokenRefreshRequestDTO request) {
        String refreshToken = request.getRefreshToken();

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (token.isExpired() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        token.expire();
        refreshTokenRepository.save(token);
        
        User user = token.getUserToken();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());

        return new RefreshTokenDTO(newAccessToken, refreshToken);
    }
}
