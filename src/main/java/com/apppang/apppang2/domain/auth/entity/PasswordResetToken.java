package com.apppang.apppang2.domain.auth.entity;

import com.apppang.apppang2.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "passwordResetTokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passwordResetToken_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    //어떤 유저의 비밀번호를 바꿀 것인지 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;       //토큰 만료 시간

    @Builder
    public PasswordResetToken(String token, User user, LocalDateTime expiryDate){
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    //만료 시간이 지났는지 확인하는 메서드
    public boolean isExpired(){
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

}
