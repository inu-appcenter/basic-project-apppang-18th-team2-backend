package com.apppang.appgang2.domain.user.service;

import com.apppang.appgang2.domain.user.dto.LoginRequest;
import com.apppang.appgang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtil jwtUtil;

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
