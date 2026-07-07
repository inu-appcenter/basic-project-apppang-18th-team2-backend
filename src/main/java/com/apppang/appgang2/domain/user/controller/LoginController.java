package com.apppang.appgang2.domain.user.controller;

import com.apppang.appgang2.domain.user.dto.LoginRequest;
import com.apppang.appgang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RequiredArgsConstructor
@RestController
public class LoginController {
    private final JwtUtil jwtUtil;

    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        /* DB 연동 후 로직 수정
         * 1.실제 사용자 정보 조회
         * 2.비밀번호 검증
         * 3.인증 성공시에만 아래 토큰 발급 로직
         */



        //user가 null이 아니라면
        String accessToken = jwtUtil.generateAccessToken(1L, request.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken();

        //refreshToken cookie에 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(60*60*24*14)
                .build();

        //accessToken 로컬 스토리지에 저장
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);


    }

}
