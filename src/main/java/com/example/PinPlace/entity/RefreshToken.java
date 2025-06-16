package com.example.PinPlace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userToken;

    @Column(unique = true, nullable = false)
    private String token;
    private boolean expired;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String deviceInfo;

    public void expire() {
        this.expired = true;
    }

    public boolean isExpired() {
        return expired || LocalDateTime.now().isAfter(expiresAt);
    }
}
