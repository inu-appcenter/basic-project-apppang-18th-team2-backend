package com.apppang.appgang2.domain.user.repository;

import com.apppang.appgang2.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    //이메일로 가입 여부 검사
    Boolean existsByEmail(String email);
}
