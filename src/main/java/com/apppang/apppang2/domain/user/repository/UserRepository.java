package com.apppang.apppang2.domain.user.repository;

import com.apppang.apppang2.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //이메일로 가입 여부 검사
    Boolean existsByEmail(String email);

    //이름과 전화번호가 일치하는 유저 찾기
    Optional<User> findByNameAndPhone(String name, String phone);
}
