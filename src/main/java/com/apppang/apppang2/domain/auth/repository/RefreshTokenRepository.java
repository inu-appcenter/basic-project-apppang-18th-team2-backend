package com.apppang.apppang2.domain.auth.repository;

import com.apppang.apppang2.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Transactional
    void deleteByUserId(Long userId);
}
