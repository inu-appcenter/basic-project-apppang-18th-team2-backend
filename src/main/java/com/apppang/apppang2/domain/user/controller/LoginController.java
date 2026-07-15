package com.apppang.apppang2.domain.user.controller;

import com.apppang.apppang2.domain.user.dto.LoginRequest;
import com.apppang.apppang2.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class LoginController {

    private final AuthService authService;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {

        Map<String, String> tokens = authService.login(request);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        //쿠키 수명은 초 단위로 저장
        long maxAgeSeconds = refreshTokenExpirationMs/1000;

        //refreshToken cookie에 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();

        //프론트엔드 바디에 내려줄 Access Token
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

}
