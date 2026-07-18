package com.apppang.apppang2.domain.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refreshTokens")
public class RefreshToken{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refresh_token", nullable = false, length = 512)
    private String refreshToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Builder
    public RefreshToken(Long userId, String refreshToken, LocalDateTime expiredAt){
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = expiredAt;
    }

}
