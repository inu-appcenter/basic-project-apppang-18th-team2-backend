package com.apppang.appgang2.domain.user.service;

import com.apppang.appgang2.domain.user.dto.LoginRequest;
import com.apppang.appgang2.domain.user.dto.SignupRequest;
import com.apppang.appgang2.domain.user.entity.User;
import com.apppang.appgang2.domain.user.repository.UserRepository;
import com.apppang.appgang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    //컨트롤러의 최종 응답을 위해 DB에 저장된 후 발급된 userId(Long)를 반환
    @Transactional
    public Long signup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        //이메일 중복 검사
        Boolean isExist = userRepository.existsByEmail(email);

        //중복일 경우 예외 처리
        if (isExist) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        //받은 DTO를 User 엔티티 형태로 옮겨담는 과정
        User user = User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))   //비밀번호 암호화
                .name(signupRequest.getName())
                .phone(signupRequest.getPhone())
                .agreeRequiredTerms(signupRequest.isAgreeRequiredTerms())
                .agreeMarketing(signupRequest.isAgreeMarketing())
                .build();

        //엔티티를 실제 DB에 저장
        User savedUser = userRepository.save(user);

        //저장 후 DB에서 발급된 기본키(유저아이디)를 꺼내서 컨트롤러로 반환
        return savedUser.getId();
    }

    public Map<String, String> login(LoginRequest request){

        //임시로 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(1L, request.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(1L);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

}
