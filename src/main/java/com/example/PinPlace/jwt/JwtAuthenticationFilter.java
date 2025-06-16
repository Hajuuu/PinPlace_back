package com.example.PinPlace.jwt;

import com.example.PinPlace.dto.LoginRequestDTO;
import com.example.PinPlace.entity.User;
import com.example.PinPlace.repository.UserRepository;
import com.example.PinPlace.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("/login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            try {
                LoginRequestDTO loginRequestDTO = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDTO.class);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getLoginId(),
                        loginRequestDTO.getPassword()
                );
                Authentication authResult = authenticationManager.authenticate(authToken);
                CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
                User user = userDetails.getUser();

                String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
                String refreshToken = jwtTokenProvider.createRefreshToken();

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                Map<String, String> tokens = Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                );
                new ObjectMapper().writeValue(response.getWriter(), tokens);
            } catch (IOException | AuthenticationException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(e.getMessage());
            }
        } else {
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    CustomUserDetails userDetails = new CustomUserDetails(user);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
