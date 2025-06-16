package com.example.PinPlace.controller;

import com.example.PinPlace.dto.LoginRequestDTO;
import com.example.PinPlace.dto.LogoutRequestDTO;
import com.example.PinPlace.dto.RefreshTokenDTO;
import com.example.PinPlace.dto.TokenRefreshRequestDTO;
import com.example.PinPlace.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<RefreshTokenDTO> login(@RequestBody LoginRequestDTO request) {
        RefreshTokenDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<RefreshTokenDTO> refresh(@RequestBody TokenRefreshRequestDTO request) {
        RefreshTokenDTO response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }
}
