package com.apppang.apppang2.domain.auth.repository;

import com.apppang.apppang2.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Transactional
    void deleteByUserId(Long userId);

    //토큰 재발급 시 클라이언트가 보낸 Refresh Token이 DB에 저장되어 있는지 검증
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
