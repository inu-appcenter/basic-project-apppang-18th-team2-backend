package com.apppang.apppang2.domain.auth.controller;

import com.apppang.apppang2.domain.auth.dto.request.LoginRequest;
import com.apppang.apppang2.domain.auth.dto.response.LoginResponse;
import com.apppang.apppang2.domain.auth.service.AuthService;
import com.apppang.apppang2.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class LoginController {

    private final AuthService authService;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    //로그인 API
    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        //서비스 로직 호출
        LoginResponse loginResponse = authService.login(request);

        //쿠키 수명은 초 단위로 저장
        long maxAgeSeconds = refreshTokenExpirationMs/1000;

        //refreshToken cookie에 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)           // JavaScript 접근 차단
                .path("/")                // 모든 경로에서 쿠키 전송
                .maxAge(maxAgeSeconds)    // 만료 시간 설정
                //.secure(true)            // 운영 환경 배포 시 활성화
                .sameSite("Lax")          // CSRF 공격 방지
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("로그인 성공", loginResponse));
    }

}
