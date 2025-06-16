package com.example.PinPlace.controller;

import com.example.PinPlace.dto.LoginRequestDTO;
import com.example.PinPlace.dto.RefreshTokenDTO;
import com.example.PinPlace.dto.TokenRefreshRequestDTO;
import com.example.PinPlace.entity.RefreshToken;
import com.example.PinPlace.entity.Role;
import com.example.PinPlace.entity.User;
import com.example.PinPlace.repository.RefreshTokenRepository;
import com.example.PinPlace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // 테스트용 프로파일 사용 시
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String loginId = "testuser";
    private final String password = "password123";

    @BeforeEach
    void setupUser() {
        if (!userRepository.existsByLoginId(loginId)) {
            User user = User.builder()
                    .loginId(loginId)
                    .password(passwordEncoder.encode(password))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    void 로그인_성공시_토큰_반환() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO(loginId, password, "JUnit");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        RefreshTokenDTO tokenDTO = objectMapper.readValue(responseContent, RefreshTokenDTO.class);

        // 이후 토큰 재발급 테스트에 사용
        testRefreshToken(tokenDTO.getRefreshToken());
    }

    @Test
    void 액세스토큰으로_보호된_API_접근_성공() throws Exception {
        // 로그인해서 토큰 받기
        LoginRequestDTO loginRequest = new LoginRequestDTO(loginId, password, "JUnit");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RefreshTokenDTO tokenDTO = objectMapper.readValue(responseContent, RefreshTokenDTO.class);
        String accessToken = tokenDTO.getAccessToken();

        // 보호된 API 호출 시도 (예: /user/profile)
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 액세스토큰으로_보호된_API_접근_성공2() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void 만료되거나_잘못된_액세스토큰_요청시_접근거부() throws Exception {
        String invalidToken = "Bearer 잘못된토큰";

        mockMvc.perform(get("/user/profile")
                        .header("Authorization", invalidToken))
                .andExpect(status().isForbidden()); // 또는 401
    }

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 만료된_리프레시토큰_재발급_거부() throws Exception {
        String expiredToken = "expired_refresh_token_sample";

        Instant expiredInstant = Instant.now().minus(1, ChronoUnit.DAYS);
        LocalDateTime expiredDateTime = LocalDateTime.ofInstant(expiredInstant, ZoneId.systemDefault());


        RefreshToken expiredRefreshToken = RefreshToken.builder()
                .token(expiredToken)
                .userToken(userRepository.findByLoginId(loginId).orElseThrow())
                .expiresAt(expiredDateTime)
                .build();

        refreshTokenRepository.save(expiredRefreshToken);

        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(expiredToken);

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertTrue(ex instanceof RuntimeException);
                    assertEquals("Refresh token expired", ex.getMessage());
                });
    }


    @Test
    void 재사용된_리프레시토큰_재발급_거부() throws Exception {
        // 1. 로그인 후 토큰 발급
        LoginRequestDTO loginRequest = new LoginRequestDTO(loginId, password, "JUnit");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RefreshTokenDTO tokenDTO = objectMapper.readValue(responseContent, RefreshTokenDTO.class);
        String refreshToken = tokenDTO.getRefreshToken();

        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(refreshToken);

        // 2. 첫번째 재발급 요청 - 성공
        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk());

        // 3. 같은 리프레시 토큰으로 두번째 재발급 요청 - 실패
        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그아웃_후_리프레시토큰_재발급_거부() throws Exception {
        // 로그인 후 토큰 받기
        LoginRequestDTO loginRequest = new LoginRequestDTO(loginId, password, "JUnit");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RefreshTokenDTO tokenDTO = objectMapper.readValue(responseContent, RefreshTokenDTO.class);
        String refreshToken = tokenDTO.getRefreshToken();

        // 로그아웃 요청 (리프레시 토큰 무효화 로직 필요)
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        // 무효화된 리프레시 토큰으로 재발급 요청 시도 - 실패 예상
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(refreshToken);

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 일반사용자가_관리자_페이지_접근시_403() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO(loginId, password, "JUnit");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RefreshTokenDTO tokenDTO = objectMapper.readValue(responseContent, RefreshTokenDTO.class);
        String accessToken = tokenDTO.getAccessToken();

        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자가_관리자_페이지_접근_성공() throws Exception {
        // 관리자 계정 세팅 (DB에 admin 유저가 있어야 함)
        String adminLoginId = "admin";
        String adminPassword = "adminpass";

        if (!userRepository.existsByLoginId(adminLoginId)) {
            User adminUser = User.builder()
                    .loginId(adminLoginId)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminUser);
        }

        LoginRequestDTO loginRequest = new LoginRequestDTO(adminLoginId, adminPassword, "JUnit");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        RefreshTokenDTO tokenDTO = objectMapper.readValue(responseContent, RefreshTokenDTO.class);
        String accessToken = tokenDTO.getAccessToken();

        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private void testRefreshToken(String refreshToken) throws Exception {
        TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(refreshToken);

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken)); // 리프레시 토큰 유지 (또는 새 토큰 반환)
    }
}
