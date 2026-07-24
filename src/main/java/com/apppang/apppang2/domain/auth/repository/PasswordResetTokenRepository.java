package com.apppang.apppang2.domain.auth.repository;

import com.apppang.apppang2.domain.auth.entity.PasswordResetToken;
import com.apppang.apppang2.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    //토큰 문자열로 엔티티 찾기
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
